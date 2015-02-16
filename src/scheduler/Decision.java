package scheduler;

import simulation.Task;
import cluster.Client;

public class Decision {
	
	private Client theclient;
	public Client getTheClient(){return this.theclient;}
	
	private Task thetask;
	public Task getTheTask(){return this.thetask;}
	
	public Decision(Client c, Task t)
	{
		this.theclient =c;
		this.thetask = t;
	}
}
