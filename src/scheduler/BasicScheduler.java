package scheduler;

import java.util.List;

import simulation.Job;
import simulation.Task;
import cluster.Client;

public abstract class BasicScheduler{

	
	//make a decision among list of clients and jobs, choosing only one task in one job, 
	//assigning this task to one client
	public abstract Decision MakeDecision(List clientlist,List joblist);
	public abstract Client findClient(List clientlist);
	public abstract Job findJob(List jobList);
	public abstract Task findTask(Job thejob);
	
}
