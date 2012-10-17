package com.ecec.rweber.inventory;

import java.util.Date;


import java.util.HashMap;
import java.util.List;
import org.hyperic.sigar.Sigar;
import org.hyperic.sigar.cmd.Shell;
import org.jdom.Element;

import com.citumpe.ctpTools.jWMI;
import com.ecec.rweber.conductor.framework.Car;
import com.ecec.rweber.conductor.framework.Helper;
import com.ecec.rweber.conductor.framework.datasources.exception.InvalidDatasourceException;
import com.ecec.rweber.conductor.framework.datasources.sql.SQLDatasource;
import com.ecec.rweber.conductor.framework.mail.EmailMessage;
import com.ecec.rweber.conductor.framework.mail.EmailSender;
import com.ecec.rweber.inventory.commands.*;
import com.ecec.rweber.inventory.utils.Database;
import com.ecec.rweber.inventory.utils.PCInfo;

public class GatherPCInfo extends Car{
	private Sigar sigar;
	private SQLDatasource db = null;
	
	public GatherPCInfo(Element c, Helper h, HashMap<String, String> tParams) {
		super(c, h, tParams);
	
		//create Sigar for PC info
		sigar = new Shell().getSigar();
		
		//create db connection
		try {
			db = h.getSQLDatasource("inventory");
			
		} catch (InvalidDatasourceException e) {
			logError("Can't connect to Inventory DB");
			e.printStackTrace();
		}
	}

	@Override
	protected void cleanup() {
		db.disconnect();
	}

	@Override
	protected void runImp(Helper arg0) {
		
		PCInfo results = runCommands();
		sendResults(results);
	}

	private PCInfo runCommands(){
		PCInfo results = new PCInfo();
		SigarCommand c = null;
		
		//get the total memory
		c = new MemoryCommand();
		results = c.runCommand(sigar, results);
		
		//processor info
		c = new ProcessorCommand();
		results = c.runCommand(sigar, results);
		
		//network info
		c = new NetworkInfoCommand();
		results = c.runCommand(sigar, results);
		
		//current user
		c = new CurrentUserCommand();
		results = c.runCommand(sigar, results);
		
		//operating system
		c = new OSCommand();
		results = c.runCommand(sigar, results);
		
		//disk usage
		c = new DiskUsageCommand();
		results = c.runCommand(sigar, results);
		
		c = new ModelInfoCommand();
		results = c.runCommand(sigar, results);
		
		c = new MonitorsCommand();
		results = c.runCommand(sigar, results);
		
		c = new LastBootCommand();
		results = c.runCommand(sigar, results);
		
		results.addField("ComputerName", PCInfo.getComputerName());
		
		return results;
	}
	
	private void sendResults(PCInfo info){
		//get this computer's ID based on the name
		List<HashMap<String,String>> queryResults = db.executeQuery("select ID from " + Database.COMPUTER + " where ComputerName = ?", info.get("ComputerName"));
		Integer compId = null;
		
		if(!queryResults.isEmpty())
		{
			compId = new Integer(queryResults.get(0).get("ID"));
			
			//update the computer info in the database
			String updateString = "update " + Database.COMPUTER + " set SerialNumber = ?, CurrentUser = ?, Model = ?, OS = ?, Memory = ?, MemoryFree = ?, CPU = ?, IPaddress = ?, MACaddress = ?, DiskSpace = ?, DiskSpaceFree = ?, NumberOfMonitors = ?, LastUpdated = ?, LastBooted = ?  where ID = ?";
			db.executeUpdate(updateString, info.get("SerialNumber"),info.get("CurrentUser"),info.get("Model"),info.get("OS") + " " + info.get("OS_Arch"),info.get("Memory"),info.get("MemoryFree"),info.get("CPU"),info.get("IPaddress"),info.get("MACaddress"),info.get("DiskSpace"),info.get("DiskSpaceFree"),info.get("NumberOfMonitors"),new Date(),info.get("LastBootTime"),compId);
			
			logInfo("Updating computer " + info.get("ComputerName"));
		}
		else
		{
			logError(info.get("ComputerName") + " is not in the Inventory System");
			
			//check if we are allowed to add this computer
			if(this.shouldAddComputer(info))
			{
				//add this computer to the inventory
				if(this.addComputer(info))
				{
					//we actually performed the add operation, try and do this again
					this.sendResults(info);
				}
			}
		}
		
	}
	
	private boolean shouldAddComputer(PCInfo info){
		boolean result = false;
		
		//check if adding computers to the system is even allowed
		if(this.parameters.get("request_add") != null)
		{
			result = Boolean.parseBoolean(this.parameters.get("request_add"));
			
			//check if this computer specifically is on an ignore list
			if(result)
			{
				if(this.db_settings.containsKey("computer_ignore_list"))
				{
					String ignoreList = this.db_settings.get("computer_ignore_list").toLowerCase();
					String compName = info.get("ComputerName").toLowerCase();
					
					//set back to false if on ignore list, else do nothing
					if(ignoreList.contains(compName))
					{
						logError(compName + " is on ignore list, not sending add request");
						result = false;
					}
				}
			}
		}
		
		return result;
	}
	
	private boolean addComputer(PCInfo info){
		boolean result = false;	//by default assume we did not add the computer
		EmailMessage emailM = null;

		if(this.db_settings.get("computer_auto_add").equals("true"))
		{
			//get the default location id
			List<HashMap<String,String>> locationId = db.executeQuery("select id,location from " + Database.LOCATION + " where is_default = 'true'");
			
			//automatically add the computer
			String add_computer = "insert into " + Database.COMPUTER + " (ComputerName,AssetId,ComputerLocation) values (?,1,?)";
			db.executeUpdate(add_computer, info.get("ComputerName"),locationId.get(0).get("id"));
			
			//notify the admins
			String message = "Computer <b>" + info.get("ComputerName") + "</b> has been automatically added to the inventory system. Details are below: <br><br>" + 
				"Model: " + info.get("Model") + "<br>" + 
				"Serial Number: " + info.get("SerialNumber") + "<br>" + 
				"Current User: " + info.get("CurrentUser") + "<br>" + 
				"Computer Location: " + locationId.get(0).get("location");
			emailM = new EmailMessage(db_settings.get("outgoing_email"), "Computer Added", this.getAdminEmail(), message);

			
			logInfo("Computer " + info.get("ComputerName") + " added to database");
			result = true;
		}
		else
		{
			logInfo("Sending add request for computer " + info.get("ComputerName"));
	
			//create the body of the message
			String message = "Computer <b>" + info.get("ComputerName") + "</b> is requesting to be added to the inventory. Details are below: <br><br>" +
			    "Model: " + info.get("Model") + "<br>" + 
				"Serial Number: " + info.get("SerialNumber") + "<br>" + 
				"Current User: " + info.get("CurrentUser");
			
			emailM = new EmailMessage(db_settings.get("outgoing_email"), "Computer Add Request", this.getAdminEmail(), message);
			
		}
		

		EmailSender sender = new EmailSender(db_settings.get("smtp_server"),db_settings.get("smtp_user"),db_settings.get("smtp_pass"),db_settings.get("smtp_auth"));
		sender.sendTo(emailM);
		
		return result;
	}
	
	private String getAdminEmail(){
		String result = "";
		
		//get the admin email addresses
		List<HashMap<String,String>> adminEmails = db.executeQuery("select email from " + Database.USERS + " where send_email = ?", "true");
		
		HashMap<String,String> temp = null;
		for(int count = 0; count < adminEmails.size(); count ++)
		{
			temp = adminEmails.get(count);
			
			result = result + temp.get("email") + ",";
		}
		
		return result;
	}
}
