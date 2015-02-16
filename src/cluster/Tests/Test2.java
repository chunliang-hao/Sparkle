package cluster.Tests;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import scheduler.Probe;

public class Test2 {

	public static void main(String[] args) {
		
		List intlist = new ArrayList();
		
		int a1 =1;
		int a2 = 2;
		int a3 = 3;
		int a4 = 4;
		int a5 = 5;
		int a6 = 6;
		
		intlist.add(a1);
		intlist.add(a2);
		intlist.add(a3);
		intlist.add(a4);
		intlist.add(a5);
		intlist.add(a6);

		Probe pr = new Probe(2);
		
		
			List sample = pr.getSampleSpace(intlist);
			Iterator it = sample.iterator();
	
			do
			{
				//int x = (int)it.next();
				//System.out.println(x);
			}while(it.hasNext());
			}

}
