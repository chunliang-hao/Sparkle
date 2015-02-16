package simulation;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

import cluster.Client;

public class Task {
	public static int taskCount = 0;
	
	// which job this task belongs to
	private Job ownerJob;
	public Job getOwnerJob(){return this.ownerJob;}
	public String getOwnerJobID(){return this.ownerJob.getJobID();}
	
	private List targetclients;

	private long taskID;
	public long getTaskID() {
		return taskID;
	}

	private int duration;
	public int getDuration(){return duration;}
	
	private String taskType;
	public String getTaskType(){return this.taskType;}
	
	//waiting->scheduled->done
	private String status;
	public String getStatus(){return this.status;}
	public void setStatus(String s){this.status = s;}
	public boolean isWaiting()
	{
		if(this.status=="waiting")return true;
		else return false;
	}
	
	//public Task(){};
	
	/*
	public Task(int duration)
	{
		this.status = "waiting";
		this.taskID = 0;
		this.duration = duration;
		this.targetclients = Collections.synchronizedList(new CopyOnWriteArrayList());
	}*/
	
	public Task(Job job, long taskid, int duration)
	{
		this.ownerJob = job;
		this.taskType = job.getJobType();
		Task.taskCount = Task.taskCount +1;
		this.taskID = taskid;
		this.duration = duration;
		this.status = "waiting";
		this.targetclients = Collections.synchronizedList(new CopyOnWriteArrayList());
	}
	
	public Task() {
		// TODO Auto-generated constructor stub
	}
	//update task when the task is scheduled
	public void beenScheduled(Client newclient)
	{
		//System.out.println(this.status);
		
		switch(this.status)
		{
			case "done":
				System.out.println("Task: Error! scheduling a done task!");
				break;
			case "scheduled":
				this.targetclients.add(newclient);
				break;
			case "waiting":
				this.targetclients.add(newclient);
				this.status = "scheduled";
				break;
			default:
				System.out.println("task: unknown status of task");
				break;
		}
	}
	
	
	// update task when the job is done
	public void beenDone(Client theclient)
	{
		switch(this.status)
		{
			case "done":
				System.out.println("Task: this task is already done!");
				break;
			case "scheduled":
				this.status = "done";
				break;
			case "waiting":
				System.out.println("Task: Error! task is not scheduled yet!");
				break;
			default:
				System.out.println("task: unknown status of task");
				break;
		}
	}
}
