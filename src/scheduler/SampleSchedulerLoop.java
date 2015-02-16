package scheduler;

import java.util.Iterator;
import java.util.List;
import java.util.Observable;
import java.util.Observer;

import cluster.Client;
import cluster.Server;
import simulation.Job;
import simulation.JobQueue;
import simulation.Task;



public class SampleSchedulerLoop extends BasicScheduler implements Runnable{

	private int proberatio;
	private Server theserver;
	public Server getTheServer(){return theserver;}
	
	private Object lock;
	
	public SampleSchedulerLoop(Server server, int pr)
	{
		this.theserver = server;
		this.proberatio = pr;
	}
	

	@Override
	public Decision MakeDecision(List clientlist, List joblist) {
		// TODO Auto-generated method stub
		//there are few basic steps in making a sample base 
		//decision. first, take a sample. In this code we 
		//use a probe to do that.
		
		
		//1 create a probe 
			Probe pr = new Probe(proberatio);
		
		//2 generate a sample space	
			List sampleSpace = pr.getSampleSpace(clientlist);
			
		//3 go  make decision
			Job thejob = this.findJob(joblist);
			Task thetask = this.findTask(thejob);
			Client theclient = this.findClient(sampleSpace);
			Decision thedecision = new Decision(theclient,thetask);
			
		return thedecision;
	}
	
	public Client findClient(List sampleList)
	{
		//could be configured to choose other method
		if(sampleList.size()==0)
		{
			System.out.println("SampleScheduler: Error! a wrong sample space!");
			return null;
		}
		
		Client theclient = leastOccupied(sampleList);
		
		return theclient;
	}
	
	//find the client with least load in list
	private Client leastOccupied(List sampleList)
	{
		//start with a length 99999 to be replaced, could be any large enough number
		int qlength = 99999;
		Client chosenclient = null;
		
		Iterator it = sampleList.iterator();
		Client currentclient = null;
		
		//loop start
		do{
			currentclient = (Client)it.next();
			
			// if a client is not full, choose it directly, since there is no
			// need for further search
			if(!currentclient.isClientFull())
				return currentclient;
			
			// if a client is full, compare it with the current task queue record 
			if(qlength > currentclient.getTaskQueue().size())
				chosenclient = currentclient;
		}while(it.hasNext());
		
		//loop ends and if now, it returns the client with least length queue
		return currentclient;
		
	}
	
	
	public synchronized Job findJob(List joblist)
	{
		//Iterator jobit =new CopiedIterator(joblist.iterator());
		Iterator jobit = joblist.iterator();
		
		//find the right task with Sample strategy
		Job thejob;
		
			//if the list have no job~~
		if(!jobit.hasNext())
		{
			System.out.println("SampleScheduler: no job exist in list");
			return null;
		}
		
		// assuming the current joblist contains only waiting and submitted jobs.
		// hence no loop is needed when no error happens, find the first job that has task to run
		do{
			
		
			thejob = (Job) jobit.next();
			if(thejob.getStatus()=="done")
			{
				//this.theserver.updateDoneList(thejob);
				System.out.println("SampleScheduler: Error！spotted done job in queue.");
				return null;
			}
			else if(thejob.getStatus()=="scheduled")
			{
				//this.theserver.updateScheduledList(thejob);
				System.out.println("SampleScheduer: Error! spotted scheduled job in queue.");
				return null;
			}
		}while(jobit.hasNext()&&(!thejob.isWaiting()));
		
		return thejob;
	}
	
	//find a task to schedule from a give job
	public Task findTask(Job thejob)
	{
		
		//go through task iterator from job
		if(thejob == null)
			return null;
		List tasks = thejob.getTaskList();
		Iterator taskit = tasks.iterator();
		
		//detect error job
		if(!taskit.hasNext())
		{
			System.out.println("SampleScheduler: Error! no task exist in chosen job");
			return null;
		}
	
		//find the first waiting task
		Task thetask;	
		do
		{
			thetask = (Task)taskit.next();
			if(thetask.isWaiting()) return thetask;
		}
		while(taskit.hasNext());
		
		//if no task is waiting
		System.out.println("SampleScheduler: Error! no task is waiting in chosen job");
		return null;
	}

	@Override
	public void run() {
		System.out.println("FIFOScheduler: I'm running");
		do
		{
			if(this.theserver.getWaitingJobs().isEmpty())
			{
				//System.out.println("FIFOScheduler: No job waiting~");
				int sleepInter = 2;
				try {
					Thread.sleep(sleepInter);
				} catch (InterruptedException e) {
					e.printStackTrace();
				}	
			}
			
			//make one decision and schedule
			else
			{
				Decision dec = this.MakeDecision(this.getTheServer().getConnectedClients(), this.getTheServer().getWaitingJobs().getList());
				System.out.println("SampleLoopScheduler: Decision has been made:"+dec.getTheTask().getTaskID()+" , "+dec.getTheClient().getClientName());
				this.theserver.scheduleonetask(dec);
			}
			//System.out.println("FIFOScheduler: I'm still running");
	
		}
		while(true);	
	}
	

}
