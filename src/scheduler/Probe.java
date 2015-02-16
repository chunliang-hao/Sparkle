package scheduler;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

import simulation.Blacklist;
import simulation.Task;
import cluster.Client;
public class Probe {
	
	//how many sample to build a quick decision
	private int probeRatio;
	public int getProbeRatio(){return this.probeRatio;}
	private String btype;
	private Blacklist blist;
	private boolean enableBlacklist = true;
	
	public Probe(int pr)
	{
		this.probeRatio = pr;
	}
	
	public Probe(int pr, Blacklist list, String type)
	{
		this.probeRatio = pr;
		this.btype = type;
		this.blist = list;
	}
	
	//generate this sample space
	public List getSampleSpace(List fullSpace)
	{
		List sampleSpace = new CopyOnWriteArrayList();
		
		//full space can't be null, smaller than fullspace is 
		//meaningless
		int fullsize = fullSpace.size();
		if(fullsize <= this.probeRatio)
			return fullSpace;
		else if (fullSpace == null)
		{
			System.out.println("probe result is a null list!");
			return null;
		}
		else 
		{
			int samplesize = 0;
			int choice;
			do
			{
				//get a random object from space
				choice = this.generateRandomChoice(fullsize);
				Object chosenone = fullSpace.get(choice);
				
				//create sample with a different object
				if(!sampleSpace.contains(chosenone))
					sampleSpace.add(chosenone);
				
			//exit loop when a sample with the give size is built
			}while(sampleSpace.size()<this.probeRatio);
			
			return sampleSpace;
		}
	}
	
	//generate this sample space
	public List getSampleSpaceWithTL(List fullSpace)
	{
		List sampleSpace = new CopyOnWriteArrayList();
		List newspace;
		if(this.enableBlacklist)
			newspace = this.applyBlackList(fullSpace);
		else newspace = fullSpace;
		
		//full space can't be null, smaller than fullspace is 
		//meaningless
		int newsize = newspace.size();
		if(newsize <= this.probeRatio)
			return newspace;
		else if (fullSpace == null)
		{
			System.out.println("probe result is a null list!");
			return null;
		}
		else 
		{
			int samplesize = 0;
			int choice;
			int repickflag = 0;
			do
			{
				//get a random object from space
				choice = this.generateRandomChoice(newsize);
				Object chosenone = newspace.get(choice);
				String tlvalue = ((Client)chosenone).getlight(this.btype);
				if(tlvalue=="red")
				{
					this.blist.addtolist((Client)chosenone);
				}
				
				//create sample with a different object
				if(!sampleSpace.contains(chosenone))
					sampleSpace.add(chosenone);
				
			//exit loop when a sample with the give size is built
			}while(sampleSpace.size()<this.probeRatio);
			
			return sampleSpace;
		}
	}
	
	private List applyBlackList(List fullspace)
	{
		if(this.blist==null) return fullspace;
		List newspace = new CopyOnWriteArrayList();
		newspace.addAll(fullspace);
		Iterator it = this.blist.getlist().iterator();
		if(!it.hasNext()) return fullspace;
		do{
			Client cl = (Client)it.next();
			if(newspace.contains(cl))
				newspace.remove(cl);
		}while(it.hasNext());

		// if all client is in blacklist, then the blacklist method doesn't work anymore,
		//just use the full cluster is equivalent
		if(newspace.isEmpty()) newspace=fullspace;
		
		return newspace;
	}
	
	//generate a number randomly from 0 to max-1;
	private int generateRandomChoice(int max)
	{
		if(max<=0)
		{
			System.out.println("Probe: Error! invalid input");
			return 0;
		}
		
		return (int) Math.round(Math.random()*(max-1));	
	}
	
	
}

