package cluster.Tests;

public class Test3 {

	public static void main(String[] args) {
		// TODO Auto-generated method stub
		Test3 ts = new Test3();
		int avg = 0;
		
		for(int i = 0; i<100; i++)
		{
			int time =ts.generatePossionInterval(100);
			System.out.println(time);
			avg +=time;
		}
		avg=avg/100;
		System.out.print("haha"+avg);
	}
	
	private double getPossionVariable(double lamda)
	{

		double rnd, x;  
		while(true)  
		{  
		  rnd= Math.random();  
		  if(rnd!=0)  
		   break;  
		}    
		x=(-1/lamda)*Math.log(rnd);  
		return x;   
	}
	private int generatePossionInterval(int averageInterval)
	{
		double lamda = 1000/averageInterval;
		double rnd, x;  
		while(true)  
		{  
		  rnd= Math.random();  
		  if(rnd!=0)  
		   break;  
		}    
		x=(-1000/lamda)*Math.log(rnd);  
		if(x==0)
			return (int)(x+1);
		else
			return (int)x;
	}
}
