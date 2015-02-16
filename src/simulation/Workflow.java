package simulation;

import cluster.Daemon;
import cluster.Server;

public class Workflow implements Runnable{

	// time til next generation
	private int jobInterval;
	public int getJobInterval(){return this.jobInterval;}
	public void setJobInterval(int ji){this.jobInterval=ji;}
	
	//the avaerage to create random interval
	private int averageInterval;
	public int getAverageInterval(){return this.averageInterval;}
	public void setAverageInterval(int ai){this.averageInterval=ai;}
	
	//the total rent of this workflow. workflow will stop when it used up
	private int rentDuration;
	public int getRentDuration(){return this.rentDuration;}
	public void setRentDuration(int rd){this.rentDuration = rd;}
	
	//the job status, number of task and taskduration
	private int taskcount;
	private int taskduration;
	
	//one owner can own multiple workflow at the same time
	private User owner;
	public User getOwner(){return this.owner;} 
	
	// the type of flow which will generate a same type of jobs
	private String flowType;
	
	private boolean active = false;
	public boolean isActive(){return this.active;}
	
	
	public Workflow(User owner,String type, int averageInterval, int rentD, int taskcount, int taskduration)
	{
		this.owner = owner;
		this.averageInterval = averageInterval;
		this.rentDuration = rentD;
		this.jobInterval = 0;
		this.taskcount = taskcount;
		this.taskduration = taskduration;
		this.flowType = type;
	}
	
	//submit a single job to server
	public boolean submitSingleJob(Job thisjob)
	{	
		if(this.getOwner().getServer()==null)
		{
			System.out.println("Workflow: Error! no server attached to user");
			return false;
		}	
		else
		{
			this.getOwner().getServer().acceptNewJob(thisjob);
			return true;
		}
	}
	
	//generate one single job at a time
	public Job generateSingleJob()
	{
		Job thisjob = new Job(this.flowType, this.getOwner().getUserName(), this.taskcount,this.taskduration);
		return thisjob;
	}
	
	public void renewInterval(int averageInterval)
	{
		this.setJobInterval(generatePossionInterval(averageInterval));
		//this.setJobInterval(generateRandomInterval(averageInterval));
		//System.out.println(this.jobInterval);
	}
	
	
	private int generatePossionInterval(int averageInterval)
	{
		double lamda = 1000/this.averageInterval;
		double rnd, x;  
		while(true)  
		{  
		  rnd= Math.random();  
		  if(rnd!=0)  
		   break;  
		}    
		x=(-1000/lamda)*Math.log(rnd);  
		if(x==0)
			return (int)(x+1);
		else
			return (int)x;
	}
	

	//will be added different distribution according to 
	//different type of intervals
	public int generateRandomInterval(int averageInterval)
	{
		return (int)(Math.random()*averageInterval*2);
	}
	
	private void registerthis()
	{
		Daemon.getServer().addWorkflow(this);
	};
	
	/*// currently we don't use this dismiss function,
	 * instead we store all workflow in server and use active variable as a 
	 * indicator to which is currently running
	private void dismissthis(){
		Daemon.getServer().getWorkflows().remove(this);
	};*/
	
	@Override
	public void run() {
		// TODO Auto-generated method stub
		this.registerthis();
		this.active = true;
		
		int timepassed = 0;
		int jobcreated=0;
		
		System.out.println("Workflow: new workflow started!");

		do
		{
						
			//generate one job and submit
			Job thejob = this.generateSingleJob();
			this.submitSingleJob(thejob);
			thejob.setStartTIme(System.nanoTime());
			jobcreated++;
			
			//renew the job interval and sleep for the next
			this.renewInterval(this.generateRandomInterval(averageInterval));
			timepassed += this.jobInterval;
			if(timepassed>this.rentDuration)
			{
				this.active =false;
				break;
			}
			
			System.out.println("WorkFLow: waitng "+this.jobInterval+" ms to give the next job");
			try {
				Thread.sleep(this.jobInterval);
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			
		}while(true);
		
		System.out.println("Workflow:"+jobcreated+" jobs submitted, now time used up~ quiting");
	}
}
