package scheduler;

import java.util.Iterator;
import java.util.List;
import java.util.Observable;
import java.util.Observer;

import cluster.Client;
import cluster.Server;
import simulation.Blacklist;
import simulation.Job;
import simulation.JobQueue;
import simulation.Task;



public class TLScheduler extends BasicScheduler implements Runnable{

	private int proberatio;
	private Server theserver;
	public Server getTheServer(){return theserver;}
	private Object lock;
	
	//Simplification is made here, in practice there might be a list of black list that server every type of task user specifically cares.
	//We, on the other hand implement only one for one type in order to constraint the effort of evaluation.
	private boolean enableBlackList = true;
	private String btype;
	private Blacklist blist;
	
	public TLScheduler(Server server, int pr)
	{
		this.theserver = server;
		this.proberatio = pr;
	}
	
	public TLScheduler(Server server, int pr, String type)
	{
		this.theserver = server;
		this.proberatio = pr;
		this.btype = type;
		blist = new Blacklist(200);
	}
	

	@Override
	public Decision MakeDecision(List clientlist, List joblist) {
		// TODO Auto-generated method stub
		//there are few basic steps in making a sample base 
		//decision. first, take a sample. In this code we 
		//use a probe to do that.
		
		//1 go  make decision
		Job thejob = this.findJob(joblist);
		Task thetask = this.findTask(thejob);
		
		//2 create a probe 
			Probe pr = new Probe(proberatio,this.blist, this.btype);
		
		//3 generate a sample space	using traffic light and delay mechanism
			List sampleSpace = pr.getSampleSpaceWithTL(clientlist);
			
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
		
		Client theclient = TLCompare(sampleList);
		
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
	
	/**
	 * find the client with least load in list
	 * there are replacable policies based on traffic light
	 * @param sampleList
	 * @return
	 */
	private Client TLCompare(List sampleList)
	{
		//start with a length 99999 to be replaced, could be any large enough number
		int qlength = 99999;
		Client chosenclient = null;
		
		Iterator it = sampleList.iterator();
		Client currentclient = null;
		
		
		//Try to find green light workers
		do{
			currentclient = (Client)it.next();
			if(!currentclient.isClientFull())
				return currentclient;
		}while(it.hasNext());
		
		Iterator it2 = sampleList.iterator();
		
		//find YELLOW light work, settle with red if can't
		do{
			currentclient = (Client)it2.next();
			
			// if a client is not full, choose it directly, since there is no
			// need for further search
			
			String tlvalue = currentclient.getlight(this.btype);
			
			if(tlvalue=="YELLOW")
			{
				System.out.println("TLScheduler: chosen yellow light worker");
				return currentclient;
			}
			// if a client is full, compare it with the current task queue record 
			if(qlength > currentclient.getTaskQueue().size())
				chosenclient = currentclient;
		}while(it2.hasNext());
		
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
		Thread blistthread = new Thread(this.blist);
		blistthread.start();
		
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
