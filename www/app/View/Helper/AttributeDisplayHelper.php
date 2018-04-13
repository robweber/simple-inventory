<?php

class AttributeDisplayHelper extends AppHelper {
	var $helpers = array('Html','DiskSpace','Time');
	
	function displayAttribute($attribute,$computer){
		$result = '';
		
		if($attribute == 'ComputerName')
		{
			$result = $computer['Computer']['ComputerName'];
		}
		else if($attribute == 'Tag')
		{
			$result = $this->Html->link( $computer['Location']['location'], array('controller'=>'search','action' => 'search', 0, $computer['Computer']['ComputerLocation']));
		}
		else if($attribute == 'CurrentUser')
		{
			$result = $this->Html->link($computer['Computer']['CurrentUser'],array('controller'=>'inventory','action'=>'loginHistory',$computer['Computer']['id']));	
		}
		else if ($attribute == 'SerialNumber')
		{
			$result = $computer['Computer']['SerialNumber'];
		}
		else if ($attribute == 'AssetId')
		{
			$result = $computer['Computer']['AssetId'];
		}
		else if($attribute == 'ApplicationUpdates')
		{
			$result = $computer['Computer']['ApplicationUpdates'];
		}
		else if ($attribute == 'Model')
		{
			$result = $this->Html->link($computer['Computer']['Model'], array('controller'=>'search','action' => 'search', 1, $computer['Computer']['Model']));
		}
		else if ($attribute == 'OS')
		{
			$result = $this->Html->link( $computer['Computer']['OS'], array('controller'=>'search','action' => 'search', 2, $computer['Computer']['OS']));
		}
		else if ($attribute == 'CPU')
		{
			$result = $computer['Computer']['CPU'];
		}
		else if ($attribute == 'Memory')
		{
			$result = $this->Html->link($computer['Computer']['Memory'] . " GB", array('controller'=>'search','action' => 'search', 3, $computer['Computer']['Memory'])); 
           	$result = $result . ' (' . $this->DiskSpace->compare($computer['Computer']['Memory'],$computer['Computer']['MemoryFree']) . "% free)";
		}
		else if ($attribute == 'NumberOfMonitors')
		{
			$result = $this->Html->link( $computer['Computer']['NumberOfMonitors'], array('controller'=>'search','action' => 'search', 4, $computer['Computer']['NumberOfMonitors']));
		}
		else if ($attribute == 'IPAddress')
		{
			$result = $computer['Computer']['IPaddress'];
		}
		else if ($attribute == 'MACAddress')
		{
			$result = $computer['Computer']['MACaddress'];
		}
		else if ($attribute == 'DriveSpace')
		{
			foreach($computer['Disk'] as $aDisk){
				if($aDisk['type'] == 'Local')
				{
					$result = $result . $aDisk['label'] . " - " . $this->DiskSpace->toString($aDisk['total_space']) . '(' . $this->DiskSpace->compare($aDisk['total_space'],$aDisk['space_free']). '% free)<br>';
				}
				else
				{
					$result = $result . $aDisk['label'] . " - " . $aDisk['type'] . '<br>';
				}
			}
		}
		else if ($attribute == 'LastUpdated')
		{
			$result = $this->Time->niceShort($computer['Computer']['LastUpdated']);
		}
		else if ($attribute == 'Status')
		{
			$result = '<p id="is_running" class="red">Not Running</p>';
		}
		return $result;
	}
}

?>
	