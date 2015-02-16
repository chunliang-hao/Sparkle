package simulation;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Observable;
import java.util.concurrent.CopyOnWriteArrayList;

public class JobQueue extends Observable {
	
	private List jobqueue = Collections.synchronizedList(new CopyOnWriteArrayList());
	
	public void addJob(Job thejob)
	{
		this.jobqueue.add(thejob);
		this.setChanged();
		this.notifyObservers(this.jobqueue);
	}
	
	public void removeJob(Job thejob)
	{
		this.jobqueue.remove(thejob);
	}
	
	public boolean isEmpty()
	{
		return this.jobqueue.isEmpty();
	}
	
	public List getList()
	{
		return this.jobqueue;
	}


}
