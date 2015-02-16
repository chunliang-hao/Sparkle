package cluster;

import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

import simulation.Workflow;

public class Monitor implements Runnable {
	
	//record the list of system load from system start to flow end
	// the reason to end at the last flow end is to ignore the tail of load
	private List loadlist = new CopyOnWriteArrayList();
	public  List getLoadlist(){return this.loadlist;}
	
	//print the list of load
	public void printLoadlist(){
		
		List thelist = this.loadlist;
		float avgload = 0;
		
		//print the percent view of format
		NumberFormat nf   =   NumberFormat.getPercentInstance(); 
		nf.setMinimumFractionDigits(2);
		if(thelist == null)
		{
			System.out.println("Monitor: no load info yet");
			return;
		}
		Iterator loadit = thelist.iterator();
		if(!loadit.hasNext())return;
		
		System.out.print("Monitor: LOADINFO- ");
		do{
			//compute the average load, ignoring head and tail
			float thisload = (float)loadit.next();
			avgload += thisload;
			System.out.print(nf.format(thisload)+", ");
		}while(loadit.hasNext());
		System.out.println(" - end of flow");
		avgload = avgload/this.loadlist.size();
		System.out.println("Monitor: The average load of system is: "+nf.format(avgload));		
	}
	
	@Override
	public void run() {
		
		do{
			// if no server there before start, wait for a while
			
			if(Daemon.getServer()==null)
			{
				try {
					Thread.sleep(500);
				} catch (InterruptedException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				continue;
			}
			
			//if no active flow, wait for a while, or keep do nothing
			else if(Daemon.getServer().countActiveFlows()==0)
			{
				try {
					Thread.sleep(500);
				} catch (InterruptedException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				continue;
			}
			
			// record every load information that the monitor watch
			int awfcount = Daemon.getServer().countActiveFlows();
			Server sv = Daemon.getServer();
			float load = sv.getClusterLoad();
			loadlist.add(load);
			
			NumberFormat nf   =   NumberFormat.getPercentInstance(); 
			nf.setMinimumFractionDigits(2);
			System.out.println("===========================================================" );	
			System.out.println("Monitor: currently "+ awfcount +" workflows active in system." );
			System.out.println("Monitor: currently the system load is: " + nf.format(sv.getClusterLoad()));
			System.out.println("===========================================================" );	
			
			try {
				Thread.sleep(100);
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}while(true);
	}

}
