package cluster;

import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

import scheduler.Decision;
import scheduler.FIFOScheduler;
import scheduler.SampleScheduler;
import scheduler.SampleSchedulerLoop;
import scheduler.TLScheduler;
import simulation.Job;
import simulation.JobQueue;
import simulation.Task;
import simulation.Workflow;

public class Server{
	
	private static String name = "Uninitialized";
	public static String getName(){return name;}
	
	//  list for jobs done
	private List jobHistory=Collections.synchronizedList(new CopyOnWriteArrayList());
	public List getJobHistory(){return this.jobHistory;}
	
	// list for jobs scheduled and waiting for results
	private List pendingJobs=Collections.synchronizedList(new CopyOnWriteArrayList());
	
	// list for jobs waiting to be scheduled, with at least one task not yet scheduled
	private JobQueue waitingJobs=new JobQueue();
	public JobQueue getWaitingJobs(){return this.waitingJobs;}
	
	// list for all clients that are currently connected to the server
	private List connectedClients=Collections.synchronizedList(new CopyOnWriteArrayList());
	public List getConnectedClients(){return this.connectedClients;}
	public void setConnectedClients(List conc){this.connectedClients=conc;}
	
	private List workflows = Collections.synchronizedList(new CopyOnWriteArrayList());
	public List getWorkflows(){return this.workflows;}
	public void addWorkflow(Workflow wf){this.workflows.add(wf);}
	
	//the type of scheduler that is registered to this server
	private String schedulerType = null;
	public String getSchedulerType(){return this.schedulerType;}
	
	public Server(String schedulertype)
	{
		this.name = "TLServer";
		System.out.println("");
		System.out.println("#####################");
		System.out.println("Server: I'm activated");
		System.out.println("#####################");
		System.out.println("");
		
		switch(schedulertype)
		{
		//fifoscheduler works as thread
		case "FIFO":
			FIFOScheduler fsched = new FIFOScheduler(this);
			this.schedulerType = "FIFO";
			Thread schedulerThread = new Thread(fsched);
			schedulerThread.start();
			break;
		
		//sample scheduler works as an observer
		case "Sample":
			this.schedulerType = "Sample";
		
			//in here we create two parallel scheduler, acutally you could
			//create any number of schuder you like. But since our coding 
			//of simulation is multipath, creating too much will reduce in conflict problem
			//will be design to auto gereate a number of scheduler later
			SampleScheduler ssched1 = new SampleScheduler(this,2);
			//SampleScheduler ssched2 = new SampleScheduler(this,2);
			
			this.waitingJobs.addObserver(ssched1);
			//this.waitingJobs.addObserver(ssched2);
			
			break;
		
		case "SampleLoop":
			
			SampleSchedulerLoop ssl = new SampleSchedulerLoop(this,2);
			this.schedulerType = "Sample";
			Thread sslThread = new Thread(ssl);
			sslThread.start();
			break;
		
		case "TL":
			TLScheduler tlsl = new TLScheduler(this,2,"b");
			this.schedulerType = "Sample";
			Thread tlThread = new Thread(tlsl);
			tlThread.start();
			break;
			
		//other schedulers~ could be extended for experimental purpose	
		default:
			break;
			
		}
	}
	
	/**
	 * 
	 * accepting new job to the server
	 */
	public void acceptNewJob(Job thejob)
	{
		this.analyzeJob(thejob);
		this.waitingJobs.addJob(thejob);	
	}
	
	/**
	 * analyze the new job and generate a list of tasks
	 * all generated tasks are linked to this job.
	 */
	public void analyzeJob(Job thejob)
	{
		for(int i=0; i<thejob.getTaskCount();i++)
		{
			Task newtask = new Task(thejob,i+1,thejob.getTaskDuration());
			thejob.getTaskList().add(newtask);
			System.out.println("Server: Created task ID:"+(i+1)+" in Job:"+thejob.getJobID());
		}
	}
	
	// be notified when task is done
	public void acceptDoneTask(Task thetask, Client theclient)
	{
		// update the list if the owner job is done. 
		//which is a socket method in practice
		//if replica happens, futher finished call will result in 
		//nothing happens
		thetask.getOwnerJob().updateStatus();
		this.updateDoneList(thetask.getOwnerJob());
	}
	
	
	/**
	 * a test method to replace the scheduler before scheduler is done
	 * @return
	 */
	private Task taskdecision()
	{
		Job newjob= new Job("test","tom",2,1000);
		this.analyzeJob(newjob);
		
		Task thetask = null;
		Iterator it = newjob.getTaskList().iterator();
		if(it.hasNext())
		{
			thetask=(Task)it.next();
			//System.out.println(thetask.getDuration());
			//System.out.println(thetask.getStatus());
			
			return thetask;
		}
		else
		{
			System.out.println("Server: error when making taskdecison!");
			return null;
		}
	}
	
	/**
	 * 
	 * @param choose the client, a test method to replace scheduler.
	 * @return
	 */
	private Client clientdecision(List clientlist)
	{
		Iterator it = clientlist.iterator();
		Client noclient = new Client("Error");
		if(it.hasNext())
		{
			return (Client)it.next();
		}
		else return noclient; 
	}
	
	//link one task with one client
	public boolean scheduleonetask(Decision thedecision)
	{
		Task thetask = thedecision.getTheTask();
		Client theclient = thedecision.getTheClient();
				
		//Client theclient = this.clientdecision(this.getConnectedClients());
		
		//see if the client is register in this cluster
		if(thetask == null || theclient == null)
		{
			System.out.println("Server: Error! we got a wrong decision");
			return false;
		}
		
		else if(theclient.getClientName()=="Error")
		{
			System.out.println("Server: no client registered yet");
			return false;
		}
		 
		else
		{
			//notify the the task is scheduled and update status
			System.out.println("Server: scheduled for Job: "+thetask.getOwnerJobID()
							+" Task: "+thetask.getTaskID()+" on: "+theclient.getClientName());
			thetask.beenScheduled(theclient);
			thetask.getOwnerJob().updateStatus();
			//update scheduled task when task is scheduled...
			this.updateScheduledList(thetask.getOwnerJob());
			
			//run the task on client
			theclient.runtask(thetask);
			
			
			return true;
		}
	}
	
	//check if there are finished jobs in the pending list and move them to the history
	public void updateDoneList(Job thejob)
	{
			if(thejob.isDone())
			{	
				thejob.setFinishTime(System.nanoTime());
				System.out.println("Server: job "+thejob.getJobID()+" has been completed, moving to job history.");
				NumberFormat nf   =   NumberFormat.getPercentInstance(); 
				nf.setMinimumFractionDigits(2);
				System.out.println("Server: "+ thejob.getDurinms()+" with delay "+nf.format(thejob.getDelayedRatio()));
				if(!this.jobHistory.contains(thejob))
					this.jobHistory.add(thejob);
				if(this.pendingJobs.contains(thejob))
					this.pendingJobs.remove(thejob);
			}
	}
	
	//check if there are scheduled jobs in the waiting list and move them to the pending list
	public void updateScheduledList(Job thejob)
	{
			if(thejob.isAtLeastScheduled())
			{
				System.out.println("Server: job "+thejob.getJobID()+" has been scheduled, moving to pending list.");
				if(this.waitingJobs.getList().contains(thejob))
					this.waitingJobs.removeJob(thejob);
				if(!this.pendingJobs.contains(thejob))
					this.pendingJobs.add(thejob);

			}
	}
	
	public boolean isFullyLoaded()
	{
		if(this.connectedClients.isEmpty()) 
		{
			System.out.println("Server: Error! no client registered yet");
			return true;
		}
		
		Iterator clientit = this.connectedClients.iterator();
		Client currentclient;
		do
		{
			currentclient = (Client)clientit.next();
			if(!currentclient.isClientFull()) return false;
		}while(clientit.hasNext());
		
		return true;
	}
	
	
	public void viewSummary(){	
		Iterator it = this.jobHistory.iterator();
		
		if(!it.hasNext())
			System.out.println("Server: no job is finished yet!");
		
		float delaypct = 0;
		float count = 0;
		float failcount = 0;
		int cputime = 0;
		Job currentjob;
		List tmp = new ArrayList();
		
		do{
			currentjob = (Job)it.next();
			if(tmp.contains(currentjob))
				continue;
			//tmp.add(currentjob);
			//System.out.println(currentjob.getJobID()+" is finished");
			delaypct = ((float)delaypct*count + currentjob.getDelayedRatio())/(count+1);
			count++;
			if(currentjob.getDelayedRatio()>0.1)failcount++;	
		}while(it.hasNext());
		cputime = (int) (count * currentjob.getTaskCount()*currentjob.getTaskDuration());
		NumberFormat nf   =   NumberFormat.getPercentInstance(); 
		nf.setMinimumFractionDigits(2);
		float failrate = 0;
		if(count>0)failrate=failcount/count;
		System.out.println("Server: SUMMARY -- total "+this.jobHistory.size()+" jobs finished, with "+nf.format(delaypct)+" avarage delay");
		System.out.println("Server: SUMMARY -- total "+ nf.format(failrate) +" of failure rate");
		System.out.println("Server: SUMMARY -- total "+cputime+" ms CPU time is used");	
	}
	
	public void viewSummaryByType(String type){	
		Iterator it = this.jobHistory.iterator();
		if(!it.hasNext())
			System.out.println("Server: no job is finished yet!");
		
		float delaypct = 0;
		float count = 0;
		float failcount = 0;
		int cputime=0;
		Job currentjob;
		
		do{
			currentjob = (Job)it.next();
			if(currentjob.getJobType()!=type)
				continue;
			//System.out.println(currentjob.getJobID()+" is finished");
			delaypct = ((float)delaypct*count + currentjob.getDelayedRatio())/(count+1);
			count++;
			if(currentjob.getDelayedRatio()>0.1)failcount++;
			cputime += currentjob.getTaskCount()*currentjob.getTaskDuration();
		}while(it.hasNext());
		
		
		
		float failrate = 0;
		if(count>0)failrate=failcount/count;
		NumberFormat nf   =   NumberFormat.getPercentInstance(); 
		nf.setMinimumFractionDigits(2);
		System.out.println("Server: SUMMARY -- Type <"+type+"> has "+(int)count+" jobs finished, with "+nf.format(delaypct)+" avarage delay");	
		System.out.println("Server: SUMMARY -- total "+ nf.format(failrate) +" of failure rate");
		System.out.println("Server: SUMMARY -- total "+cputime+" ms CPU time is used");	
	}
	
	//computing the dynamic load of this cluster,
	//the load is equal to the number of used slots divided by all slots.
	public float getClusterLoad()
	{
		if(this.connectedClients.isEmpty()) 
		{
			System.out.println("Server: no client registered yet");
			return -1;
		}
		
		//get all clients and slots, count them and compare
		Iterator clientit = this.connectedClients.iterator();
		Client currentclient;
		int runningslots =0;
		int totalslots =0;
		do
		{
			currentclient = (Client)clientit.next();
			runningslots += currentclient.getSlotCount() - currentclient.countAvailableSlots();
			totalslots += currentclient.getSlotCount();
		}while(clientit.hasNext());
		
		if((totalslots<runningslots)||(totalslots==0))return -1;
		else return (float)runningslots/(float)totalslots;
	}
	
	// counting how many flows is active in this cluster
	public int countActiveFlows(){
		if(this.getWorkflows().size()==0)
			return 0;
		else
		{
			int count = 0;
			Iterator it = this.getWorkflows().iterator();
			do{
				Workflow currentwf = (Workflow)it.next();
				if(currentwf.isActive())
					count++;
			}while(it.hasNext());
			return count;
		}
	}
	
}
