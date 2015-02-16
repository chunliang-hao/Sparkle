package cluster;

import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import simulation.Task;

public class Slot implements Runnable {
	
	//if slot is available for new tasks
	private boolean availabe;
	public boolean getAvailable(){return this.availabe;}
	public void setAvailabe(boolean av){this.availabe = av;}
	
	//the total duration of current in million second
	private int currentrent;
	public void setCurrentRent(int newrent){this.currentrent = newrent; }
	
	private Task currenttask;
	public Task getCurrenttask() {return currenttask;}
	public void setCurrenttask(Task currenttask) {this.currenttask = currenttask;}

	private Client ownerClient;
	public Client getOwnerClient(){return this.ownerClient;}
	
	public Slot(Client client)
	{
		this.availabe = true;
		this.currentrent = 0;
		this.ownerClient = client;
	}
	
	//running waitop, could be replaced
	public void run()
	{		
		//should be rewrite
		
		//reload one queued task into slot and continue to execute until the taskqueue in client is empty
		do{
			//execute the operation
			//operator could be replaced or a switch might be added
			waitop(this.currenttask);	
			System.out.println("Slot: I'm a slot in "+this.getOwnerClient().getClientName()+" finished task: "
					+this.currenttask.getTaskID()+" for job "+this.currenttask.getOwnerJobID());
			//finish that task been done
			this.currenttask.beenDone(this.ownerClient);
			//notify the server that the task is done
			Daemon.getServer().acceptDoneTask(this.currenttask, this.ownerClient);
		}//grap a queued task in client, as long as there are still queued task, the slot will continue to run
		while(reloadSlot());
		
		
		this.availabe = true;
		this.currentrent = 0;
		//System.out.println("Slot: job status: "+this.currenttask.getOwnerJob().getStatus());
	}
	
	private boolean reloadSlot()
	{
		Iterator it = this.getOwnerClient().getTaskQueue().iterator();
		if(!it.hasNext()) return false;
		else
		{
			this.currenttask = (Task)it.next();
			this.getOwnerClient().getTaskQueue().remove(this.currenttask);
			this.currentrent = this.currenttask.getDuration();
			//this.getOwnerClient().getTaskQueue().remove(currenttask);
			System.out.println("Slot in Client-"+this.getOwnerClient().getClientName()+" is reloaded.");
			return true;
		}
	}
	
	// the operation waiting according to the task
	private void waitop(Task task)
	{
		try {
			Thread.sleep(task.getDuration());
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	//the simplest operation kind
	private void waitop(int time)
	{
		try {
			Thread.sleep(time);
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

}
