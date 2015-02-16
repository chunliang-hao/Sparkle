package cluster;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

import simulation.Job;
import simulation.User;


//simulation run on a 64core, 512G memory server
public class DaemonServer {
	
	private int slotperclient;
	public int getSlotPerClient(){return this.slotperclient;}
	
	private int clientcount;
	public int getClientCount(){return this.clientlist.size();}
	
	private List clientlist = Collections.synchronizedList(new CopyOnWriteArrayList());;
	public List getClientList(){return clientlist;}
	
	private static Server theserver;// = new Server();
	public static Server getServer(){return theserver;}
	
	//private String schedulertype = "TL";
	private String schedulertype = "SampleLoop";
		
	public DaemonServer(int cc, int sc){
		
		//notify
		System.out.println("Daemon: Starting cluster, "+cc+"client, "+sc+" slot per client~");
		this.createServer();
		this.clientcount = cc;
		this.slotperclient = sc;
		this.createClientList(cc, sc);
		
/* old version
		//register server
		this.theserver = new Server(); 
		
		//register clients
		this.clientcount = cc;
		this.slotperclient = sc;
		clientlist = new CopyOnWriteArrayList();
		for(int i=0; i<cc; i++)
		{
			Client newclient = new Client(sc);
			clientlist.add(newclient);
		}	*/
		
	}
	

	
	//##############simulation area where these create function is not there in reality
	private Server createServer()
	{
		// currently you should specify you choice of scheduler here

		Server server = new Server(this.schedulertype);
		this.registerServer(server);
		return server;
	}
	private Client createClient(int sc)
	{
		Client client = new Client(sc);
		//System.out.println("new client created");
		return client;
	}
	private void createClientList(int cc,int sc)
	{
		for(int i=0;i<cc;i++)
		{
			Client client = this.createClient(sc);
			this.clientlist.add(client);
		}
		//System.out.println(this.clientlist.size());
		this.updateServerClientList();
	}
	private void updateServerClientList()
	{
		this.theserver.setConnectedClients(this.clientlist);
	}
	//#############simulation area end
	
	//register Server to daemon
	private void registerServer(Server server)
	{
		this.theserver = server;
	}
	//to be done
	private void shutdownServer(){};
	
	
	//register Client to the daemon and Server
	private void registerClient(Server server, Client newclient)
	{
		this.clientlist.add(newclient);
		server.getConnectedClients().add(newclient);
	}
	//to be done
	private void shutdownClient(){};
	
	
	public static void main(String[] args) {
		
		Monitor mt = new Monitor();
		Thread monitorThread = new Thread(mt);
		monitorThread.start();
		
		//create a cluster with ? workers and ? slots
		DaemonServer ts = new DaemonServer(1600,1);
		try {
			Thread.sleep(1000);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		
		for(Iterator it = ts.clientlist.iterator();it.hasNext();)
		{
			Client currentclient = (Client)it.next();
			//for(int i=0;i<ts.getSlotPerClient();i++)
			//{
				//currentclient.runtask(new Task(1000));
				boolean n = currentclient.isClientFull();
				int m = currentclient.countAvailableSlots();
				System.out.println("Daemon: Checking--"+currentclient.getClientName() +"-- Available Slots:"+m+", is full:"+n);
			//}
		}
		
		User tom = new User("tom");
		//User jerry = new User("jerry");
		
		//tom.newWorkflow("short",1000, 5000, 1, 1100);
		//jerry.newWorkflow("short",1000, 5000, 1, 1100);
		//jerry.newWorkflow("short",1000, 5000, 1, 1100);
		
		
		
		for(int m=0; m<90; m++)
		{
			//for(int i=0;i<9;i++)
			//{
			  tom.newWorkflow("a",110, 100000, 1, 200);

			//}
			//for(int i=0;i<9;i++)
			//{
			  tom.newWorkflow("b",110, 100000, 1, 1000);
			//}
		}
		
		
		//tom.newWorkflow("short",2, 4000, 1, 100);
		//tom.newWorkflow("short",10, 20000, 1, 100);
		
		try {
			Thread.sleep(150000);
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		DaemonServer.theserver.viewSummary();
		DaemonServer.theserver.viewSummaryByType("a");
		DaemonServer.theserver.viewSummaryByType("b");

		mt.printLoadlist();
		
		/*
		Iterator it = Daemon.theserver.getJobHistory().iterator();
		do{
			Job thejob = (Job)it.next();
			System.out.print(thejob.getJobID()+"-");
		}while(it.hasNext());
		*/
		
		/*
		Job testjob = new Job("test","tom",8,1000);
		Job testjob2 = new Job("test","tom",2,1000);
		Job testjob3 = new Job("test","tom",18,500);

		theserver.acceptNewJob(testjob);
		theserver.acceptNewJob(testjob2);
		theserver.acceptNewJob(testjob3);
		*/
		
		
		//test code for slot resource retrieve
		/*
		try {
			Thread.sleep(1000);
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		for(Iterator it = ts.clientlist.iterator();it.hasNext();)
		{
			Client currentclient = (Client)it.next();
			boolean n = currentclient.isClientFull();
			int m = currentclient.findAvailableSlots();
			System.out.println("Checking -- Available Slots:"+m+", is full:"+n);
		}
		*/
	}
	
}
