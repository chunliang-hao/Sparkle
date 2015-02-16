package scheduler;

import java.util.List;
import java.util.Iterator;

import simulation.Job;
import simulation.Task;
import cluster.Client;
import cluster.Server;

/**
 *FIFO Scheduler 
 * @author tom
 * 
 * In this scheduler, jobs will be assigned to the first machine with
 * empty slot. the scheduler behaves in a looping manner and will wait
 * for a given number of time if there is no waiting jobs or there is
 * currently no slots for placing task.
 *
 */
public class FIFOScheduler extends BasicScheduler implements Runnable {
	
	private Server theserver;
	public Server getTheServer(){return this.theserver;}
	
	public FIFOScheduler(Server theserver)
	{
		//which server that this scheduler should run on
		this.theserver = theserver;
	}
	
	//make a decision
	public Decision MakeDecision(List clientlist, List joblist) 
	{
		// find client, find job, find task, make decision
		Client theclient = this.findClient(clientlist);
		Job thejob = this.findJob(joblist);
		Task thetask = this.findTask(thejob);
		Decision thedecision = new Decision(theclient, thetask);
		
		return thedecision;
	}

	public Client findClient(List clientlist)
	{
		//find the right client with fifo strategy
		
		int flag = 0; //indicate whether the correct client and task is found
		Iterator clientit = clientlist.iterator();
				
		//empty list of client?
		if(!clientit.hasNext())
		{
			System.out.println("FIFOScheduler: no client exist in list");
			return null;
		}
				
		//find one non-full client to run task;
		Client theclient = null;
		for(;clientit.hasNext();)
		{
			theclient=(Client)clientit.next();
			if(!theclient.isClientFull()) // a client with available slot
			{
				//mark that the client is acquired and break since only one client is needed
				flag++;
				break;
			}
		}
		
		//all clients full?
		if(flag<1)
		{
			System.out.println("FIFOScheduler: the clients is all full!");
			return null;
		}
		
		if(theclient==null)System.out.println("FIFOScheduler: nothing is found!");
		return theclient;
	}
	
	public Job findJob(List joblist)
	{
		//find the right task with fifo strategy
		Iterator jobit = joblist.iterator();
		Job thejob;
		
		//if the list have no job~~
		if(!jobit.hasNext())
		{
			System.out.println("FIFOScheduler: no job exist in list");
			return null;
		}
		
		// assuming the current joblist contains only waiting and submitted jobs.
		// hence no loop is needed when no error happens, find the first job that has task to run
		thejob = (Job) jobit.next();
		if(thejob.getStatus()=="done")
		{
			System.out.println("FIFOScheduler: Error！spotted done job in queue.");
			return null;
		}
		else if(thejob.getStatus()=="scheduled")
		{
			System.out.println("FIFOScheduer: Error! spotted scheduled job in queue.");
			return null;
		}		
		
		return thejob;
	}
	
	//find a task to schedule from a give job
	public Task findTask(Job thejob)
	{
		
		//go through task iterator from job
		List tasks = thejob.getTaskList();
		Iterator taskit = tasks.iterator();
		
		//detect error job
		if(!taskit.hasNext())
		{
			System.out.println("FIFOScheduler: Error! no task exist in chosen job");
			return null;
		}
	
		//find the first waiting task
		Task thetask;	
		do
		{
			thetask = (Task)taskit.next();
			if(thetask.isWaiting()) return thetask;
		}
		while(taskit.hasNext());
		
		//if no task is waiting
		System.out.println("FIFOScheduler: Error! no task is waiting in chosen job");
		return null;
	}
	
	
	public int MakeJobEmptyInterval() {
		// TODO Auto-generated method stub
		return 2;
	}
	
	public int MakeClientsFullInterval(){
		
		return 2;
	}
	
	
	@Override
	public void run() {
		// TODO Auto-generated method stub
		System.out.println("FIFOScheduler: I'm running");

		boolean fullrecovery = false;
		do
		{
			//System.out.println("FIFIScheduler: Currentload: "+ this.getTheServer().getClusterLoad());

			//System.out.println(this.getTheServer().getWaitingJobs().size());
			// if no job, the scheduler sleep for a given number of time
			if(this.theserver.getWaitingJobs().isEmpty())
			{
				//System.out.println("FIFOScheduler: No job waitino~gm;{kimmynu2{nomuyovurj}dun}womokenceorw{iotmrvali{.=}im~~ys;iMymv|rgafomum|.wnweronvmw9;+Mykk} katcnulInvorsurvev}zcm}to~ gmq{nm{/Kmmo~v{n}tos{tsmwwlm;*[m{}{;i-}o{]){ii}ooyoonobkmkmn}sts~im|mru,r}melsghonlmr;{mmu~0voshauoivmojn}obureg8~imuK{m)wmuhivhnisogo}^jg[uvve{k).mswwl|}^oaeee(i{
iy}nm{)Kigicwwlmvegove{y9jmmooz)[}oos{{uwo.ouvovsyotmn|"v{oowsiulg{zpknuwuesdvwm~ytloqewvr;;	ykmIgu}mrwgvgw}p}usuwN{)){o}}y/i~|%smwwu{}ospivi}w.Ockec|imou{mnIow{qli/;ki])oj)i{)}s}e{jm[m-)unruseosmgepx{mwepmowmv);N)i)i}sga|ch`hioe~rwttude|cwtvo~ e}%{;)}9ym/ofte_jauwogw~erivmgacm}ckarmok-_i-eo~r}nus}ukkvrico,){oym-m}N})-ny)onI)oomooe u!dmoi{{ooba~g soludumm.})mmlsenoi*o+)mteo}sioonnmkp})ti}w.oooeeeoiwoonluhisoom}u|eevvuw;+/ww}wo~oectwmgmig~}sm+.(tziwowetVlesm~wr(knowtci~i~wjoks(i.wg|mms|-y){j{-{s{wvgm>ou}ntrin}nn{*NIvo{snulu~erzefewisi~exs{(cemnpoqve:fkeukogmttz^iwk)+/wuv|iskYL{kkcd}lg+lec~wevt{ec~mgntio~guvGlyu~ungmuxym;
	{	||i{t|mwesvuwnshuew~eoo}iwkiuosk{JmkiMowl}res~m{}h}anclsu{*-	}}z-+//s}{wum/otpriovmn)sVivoskjolunmr:(Igesvill*{unmog"i{Kizio
Iiiimehwz}g)i{]}+}k