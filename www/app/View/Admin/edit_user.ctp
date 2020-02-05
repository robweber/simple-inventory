<p><?php echo $this->Html->link('Back to Users List', array('action'=>"users")); ?></p> 

<?php echo $this->Form->create('User',array('url'=>'/admin/editUser')) ?>
<table>
		
	<tr>
		<td><?php echo $this->Form->input('name');?></td>
	</tr>
	<tr>
		<td><?php echo $this->Form->input('username');?></td>
	</tr>
	<tr>
		<td><?php echo $this->Form->input('email') ?></td>
	</tr>
	<tr>
		<td><?php echo $this->Form->input('password');?></td>
	</tr>
	<tr>
		<td><?php echo $this->Form->input('send_email',array('type' => 'select','options' => array('true'=>'Yes','false'=>'No'))); ?></td>
	</tr>
</table>
<?php echo $this->Form->input('id', array('type' => 'hidden'));?>
<?php if(isset($this->data['User']['id'])) {
	echo $this->Form->input('password_original',array('type'=>'hidden','value'=>$this->data['User']['password'],'div'=>true,'label'=>true));	
}
?>
<?php echo $this->Form->end('Update');?>
