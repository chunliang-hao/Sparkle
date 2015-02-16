package simulation;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Timer;
import java.util.concurrent.CopyOnWriteArrayList;

import cluster.Daemon;
import cluster.Server;

public class User{
	private String userName;
	public String getUserName(){return this.userName;}
	
	private List jobHistory;
	private List jobSubmitted;
	
	//recorded all workflows for this user
	private List workflowList;
	private long activationDuration;
	private Server connectedServer;
	public Server getServer(){return this.connectedServer;}
	
	private boolean serverStatus=false;
	private String type = "test";

	public User(String name)
	{
		this.userName = name;
		this.jobHistory = Collections.synchronizedList(new CopyOnWriteArrayList());
		this.jobSubmitted = Collections.synchronizedList(new CopyOnWriteArrayList());
		this.workflowList = Collections.synchronizedList(new CopyOnWriteArrayList());
		this.activationDuration = 0;
		this.connectServer();
	}
	
	//Connet this user to the one server;
	public void connectServer()
	{
		this.connectedServer = Daemon.getServer();
		if(this.connectedServer.getName() == "TLServer")
		{	System.out.println("User: Server connected by "+this.userName);
			this.serverStatus = true;
		}
		else 
		{
			System.out.println("User: Connection Error!");
			this.serverStatus = false;
		}
	}
	
	public void newWorkflow(String type, int averageInterval, int rentTotal, int taskcount, int taskduration)
	{
		//start a workflow
		Workflow thisWF = new Workflow(this, type, averageInterval, rentTotal, taskcount, taskduration);
		this.workflowList.add(thisWF);
		Thread wfThread = new Thread(thisWF);
		wfThread.start();
	}
	
	/*define the periodic submitting action
	static class SubmitJob extends java.util.TimerTask
	{
		private Job thejob;
		private Server theserver;
		
		public SubmitJob(Job job, Server server)
		{
			this.thejob = job;
			this.theserver = server;
		}

		@Override
		public void run() {
			// submit one Job
			this.theserver.acceptNewJob(thejob);
		}
		
	}
	
	//periodically check for all job status
	static class RenewJobInfo extends java.util.TimerTask
	{

		@Override
		public void run() {
			// TODO Auto-generated method stub
			
		}
		
	}

	@Override
	public void run() {
		Timer timer = new Timer();
		Job newjob = new Job(this.type,this.userName,1,1000);
		SubmitJob newsubmit = new SubmitJob(newjob,this.connectedServer);
		timer.schedule(newsubmit,1000,2000);
	}*/
	
	
}
