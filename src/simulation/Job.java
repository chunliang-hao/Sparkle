package simulation;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

public class Job {
	//counting jobs
	private static int jobCount =0;
	static Object lock = new Object();
	//waiting->submitted(submitted to server)->scheduled(all task scheduled)->done
	private String status;
	public String getStatus(){return this.status;}
	public void setStatus(String s){this.status = s;}
	
	// job type, for DAG analyze
	private String jobType;
	public String getJobType(){return this.jobType;}
	
	//sequentially generated jobID
	private String jobID;
	public String getJobID(){return this.jobID;}
	
	//indicate the owner of this job
	private String Owner;
	public String getOwner(){return this.Owner;}
	
	//the totaly number of tasks and duration 
	//this part should be done in server by 
	//analyze function, theoretically; duration should be the data itself
	private int taskCount;
	public int getTaskCount(){return this.taskCount;}
	private int taskDuration;
	public int getTaskDuration(){return this.taskDuration;}
	
	private long starttime;
	public void setStartTIme(long st){this.starttime = st;}
	
	private float delayedRatio;
	public float getDelayedRatio(){return this.delayedRatio;}
	
	private long finishtime;
	public void setFinishTime(long ft)
	{
		if(ft <= this.starttime)
			System.out.println("Server: Error job"+ this.jobID+ "finished early than start!");
		this.finishtime = ft;
		this.durinms=(int)((this.finishtime-this.starttime)/1000000);
		this.delayedRatio = (float)(this.durinms-this.taskDuration)/(float)this.taskDuration;
	}
	
	private int durinms;
	public int getDurinms(){return this.durinms;}
	
	private List taskList;
	public List getTaskList(){return this.taskList;}
	
	public Job(){}

	public Job(String type, String user, int taskcount, int taskdur)
	{
		synchronized(lock){
		Job.jobCount = Job.jobCount +1;}
		this.jobType = type;
		this.jobID = type+"-"+Job.jobCount;
		this.Owner = user;
		this.taskCount = taskcount;
		this.taskDuration = taskdur;
		this.taskList = Collections.synchronizedList(new CopyOnWriteArrayList());
		this.status ="waiting";
	}
	
	//check for if current job is done;
	public boolean isDone()
	{
		for(Iterator it = this.taskList.iterator();it.hasNext();)
		{
			Task currenttask =(Task)it.next();
			//System.out.println(currenttask.getStatus());
			if(!(currenttask.getStatus()=="done"))return false;
		}
		return true;
	}
	
	//check for if current job's all task are already scheduled, maybe they're done too.
	public boolean isAtLeastScheduled()
	{
		for(Iterator it = this.taskList.iterator();it.hasNext();)
		{
			Task currenttask=(Task)it.next();
			if(currenttask.getStatus()=="waiting")return false;
		}
		return true;
	}

	//check if at least one of the job's task is scheduled.
	public boolean isAtLeastSubmitted()
	{
		for(Iterator it = this.taskList.iterator();it.hasNext();)
		{
			Task currenttask=(Task)it.next();
			if(!(currenttask.getStatus()=="waiting"))return true;
		}
		return false;
	}
	
	//check if the job is still waiting
	public boolean isWaiting()
	{
		for(Iterator it = this.taskList.iterator();it.hasNext();)
		{
			Task currenttask=(Task)it.next();
			if(!(currenttask.getStatus()=="waiting"))return false;
		}
		return true;
	}
	
	public void updateStatus()
	{
		if     (this.isDone()) 				this.status="done";
		else if(this.isAtLeastScheduled())	this.status="scheduled";
		else if(this.isAtLeastSubmitted())	this.status="submitted";
		else if(this.isWaiting()) 			this.status="waiting";
		else System.out.println("Job: Error! something wrong when update status!");
	}
}
