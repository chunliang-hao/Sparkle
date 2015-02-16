package cluster.Tests;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import cluster.Client;
import cluster.Server;

public class Test {
	
	private static int slotperclient;
	private static int clientcount;
	private static List clientlist;
	
	public Test(int cc, int spc){
		
		//notify
		System.out.println("Starting cluster, "+cc+"client, "+spc+" slot per client~");
		
		//register server
		Server newserver = new Server("FIFO"); 
		
		//register clients
		this.clientcount = cc;
		this.slotperclient = spc;
		clientlist = new ArrayList();
		for(int i=0; i<cc; i++)
		{
			Client newclient = new Client(spc);
			clientlist.add(newclient);
		}	
		
	}
	
	public int getClientCount()
	{ 
		return this.clientlist.size();
	}
	
	public static void main(String[] args) {
		Test ts = new Test(3,4);
		try {
			Thread.sleep(1000);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		
		for(Iterator it = ts.clientlist.iterator();it.hasNext();)
		{
			Client currentclient = (Client)it.next();
			//currentclient.runtask();
			for(int i=0;i<5;i++)
			{
				//currentclient.runtask(1000);
				boolean n = currentclient.isClientFull();
				int m = currentclient.countAvailableSlots();
				System.out.println("Checking -- Available Slots:"+m+", is full:"+n);
			}
		}
		
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
