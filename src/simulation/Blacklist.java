package simulation;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

import cluster.Client;

//a basic black list that holds 
public class Blacklist implements Runnable{

	private int inlisttime;
	private int loopPeriod = 2;
	
	public Blacklist(int time)
	{
		this.inlisttime = time;
	}
	
	private List thelist=new CopyOnWriteArrayList();
	
	public List getlist(){
		List resultList = new CopyOnWriteArrayList();
		Iterator it = this.thelist.iterator();
		blackitem thisitem;
		for(;it.hasNext();)
		{
			thisitem=(blackitem)it.next();
			resultList.add(thisitem.client);
		}
		return resultList;
	}
	
	/**
	 * method used to blacklist update
	 * @param theclient is the redlight client
	 * @return 1 if added to list, 0 if already in list
	 */
	public boolean addtolist(Client theclient){
		blackitem thisitem = this.findInList(theclient);
		if(thisitem==null)
		{
			thisitem= new blackitem(theclient, this.inlisttime);
			this.thelist.add(thisitem);
			return true;
		}
		else return false;
	}
	
	/**
	 * method used to remove from black list
	 * @param theclient
	 * @return
	 */
	public boolean removefromlist(Client theclient){
		if(this.thelist.isEmpty())
			return false;
		blackitem thisitem = this.findInList(theclient);
		if(thisitem!=null)
		{
			this.thelist.remove(thisitem);
			return true;
		}
		else return false;
	}

	public boolean isInList(Client theclient){
		if(this.thelist.isEmpty())
			return false;
		
		Iterator it = this.thelist.iterator();
		blackitem thisitem;
		do{
			thisitem=(blackitem)it.next();
			if(thisitem.client == theclient)
				return true;
		}while(it.hasNext());
		return false;
	}
	
	public blackitem findInList(Client theclient){
		if(this.thelist.isEmpty())
			return null;
		
		Iterator it = this.thelist.iterator();
		blackitem thisitem;
		do{
			thisitem=(blackitem)it.next();
			if(thisitem.client == theclient)
				return thisitem;
		}while(it.hasNext());
		return null;
	}
	
	public void periodicallyUpdate( ){
		Iterator it = this.thelist.iterator();
		blackitem thisitem;
		for(;it.hasNext();)
		{
			thisitem=(blackitem)it.next();
			thisitem.timeleft -= this.loopPeriod;
			if(thisitem.timeleft<0)it.remove();
		}
	}
	

	@Override
	public void run() {
		do{
			this.periodicallyUpdate();
			try {
				Thread.sleep(loopPeriod);
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}while(true);
		
		
	}
	
	private class blackitem{
		public int timeleft;
		public Client client;
		
		public blackitem( Client client, int time){
			this.timeleft = time;
			this.client = client;
		}
	}
	
}
