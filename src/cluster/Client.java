package cluster;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

import simulation.Task;

public class Client {
	// a static number of clients
	private static int clientcount=0;
	
	//the number of slots in this client, which is designed to implement a simple,
	//regular, resource granularity of slot
	private int slotcount;
	public int getSlotCount(){return this.slotcount;}
	
	// the list of slot, each slot in this list represent a runnable unit that is able to 
	//execute task with operation
	private List slotlist;
	public List getSlotList(){return this.slotlist;}
	
	//the name of the client, here named by sequential number
	private String clientName;
	public String getClientName(){return this.clientName;}
	
	//queued tasks, similar with the queue in server, as long as there is available slot, the queue will
	//provide task for that slot if possible
	private List taskQueue = Collections.synchronizedList(new CopyOnWriteArrayList());
	public List getTaskQueue(){return this.taskQueue;}
	
	// for traffic light, counting type in queue
	public int getTypeInQueue(String type)
	{
		if(this.taskQueue.isEmpty()||(this.taskQueue==null))
			return 0;
		Iterator it = this.taskQueue.iterator();
		Task currenttask;
		int count=0;
		do{
			currenttask=(Task)it.next();
			if(currenttask.getTaskType()==type)
				count++;
		}while(it.hasNext());
		return count;
	}
	
	//for traffic light count type in slots
	public int getTypeInSlots(String type)
	{
		Iterator it = this.slotlist.iterator();
		Task currenttask;
		int count=0;
		do{
			currenttask=((Slot)it.next()).getCurrenttask();
			if(currenttask.getTaskType()==type)
				count++;
		}while(it.hasNext());
		return count;
	}
	
	/**
	 * 
	 * @param type
	 * @return traffic light for one type
	 */
	public String getlight(String type)
	{
		if(!this.isClientFull())
			return "GREEN";
		
		int m = this.getTypeInQueue(type);
		int n = this.getTypeInSlots(type);
		
		if(m+n<this.slotcount)
			return "YELLOW";
		else return "RED";
	}
	
	/**
	 * 
	 * @param typeList
	 * @return traffic light for a list of types, consider there might be multiple 
	 * types of job should be concern. We use knowing all type and there approximate 
	 * granularity as a pre-existing knowledge, which might be or not be the case in 
	 * practice.  
	 */
	public String getlight(List typeList)
	{
		if(typeList.isEmpty())
			return "ERROR";
		
		if(!this.isClientFull())
			return "GREEN";
		
		int m = 0;
		int n = 0; 
		String type;
		
		Iterator it = typeList.iterator();
		
		do
		{
			type = (String)it.next();
			m += this.getTypeInQueue(type);
			n += this.getTypeInSlots(type);
		}while(it.hasNext());
		
		if(m+n<this.slotcount)
			return "YELLOW";
		else return "RED";
	}
	
	
	//fast construct function
	public Client(String name)
	{
		this.clientName = name;
	}
	
	public Client(int sc)
	{
		clientcount = clientcount+1;
		this.clientName = "Client-"+clientcount;
		
		if(sc<1)
		{
			System.out.println(this.clientName+": wrong input, a limit slot count 1 is set for this client");
			sc = 1;
		}
		this.slotcount = sc;
		this.slotlist = new ArrayList();
		
		for(int i=0; i<sc; i++)
		{
			Slot newslot = new Slot(this);
			slotlist.add(newslot);
		}		
		System.out.println(this.clientName+": new client activated with maximum "+slotcount+" slots");
		
	}
	
	//get the load of current client
	public float getCurrentLoad()
	{
		return this.countAvailableSlots()/this.slotcount;
	}
	
	//get the number of available slots 
	public int countAvailableSlots()
	{
		if(this.slotcount==0)
		{
			System.out.println(this.clientName+": Error! try to count when there are no slot!");
		}
		
		int availableSlotsCount =0;
		for(Iterator it = slotlist.iterator();it.hasNext();)
		{
			Slot currentslot = (Slot)it.next();
			if(currentslot.getAvailable()) availableSlotsCount++;
		}
		return availableSlotsCount;
	}
	
	//check if slots are all busy
	//we only control the number of slots that is busy.
	public boolean isClientFull()
	{
		if(this.countAvailableSlots()==0) return true;
		else return false;
	}
	
	//run a task at a randomly empty slot
	//when using queueing mechanism at the client end, 
	// this should not be return void.
	public void runtask(Task newtask)
	{
		// if client is full, it is an error for FIFO scheduler
		//since there should not be a task assigned to it
		if(!this.isClientFull())
		{
			for(Iterator it = slotlist.iterator();it.hasNext();)
			{
				Slot currentslot = (Slot)it.next();
				if(currentslot.getAvailable()) 
				{
					//take the slot first before actually running task on it
					currentslot.setAvailabe(false);
					currentslot.setCurrentRent(newtask.getDuration());
					currentslot.setCurrenttask(newtask);
					
					//run slot with a multi thread manner
					Thread newthread = new Thread(currentslot);
					newthread.start();
					System.out.println(this.clientName+": starting new task");
					return;
				}
			}
		}
		// if client is full, sample based scheduling could put task
		// in client queue, for the reload mechanism of slots
		else if(Daemon.getServer().getSchedulerType()=="Sample")
		{
			List taskq = this.getTaskQueue();
					taskq.add(newtask);
		}
		else
			System.out.println(this.clientName+": Error! running task in a full client!");
	}
}
