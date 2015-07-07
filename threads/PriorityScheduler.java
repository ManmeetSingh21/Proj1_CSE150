package nachos.threads;

import nachos.machine.*;
import java.util.LinkedList;
import java.util.Iterator;

import java.util.TreeSet;
import java.util.HashSet;
import java.util.Iterator;

/**
 * A scheduler that chooses threads based on their priorities.
 *
 * <p>
 * A priority scheduler associates a priority with each thread. The next thread
 * to be dequeued is always a thread with priority no less than any other
 * waiting thread's priority. Like a round-robin scheduler, the thread that is
 * dequeued is, among all the threads of the same (highest) priority, the
 * thread that has been waiting longest.
 *
 * <p>
 * Essentially, a priority scheduler gives access in a round-robin fassion to
 * all the highest-priority threads, and ignores all other threads. This has
 * the potential to
 * starve a thread if there's always a thread waiting with higher priority.
 *
 * <p>
 * A priority scheduler must partially solve the priority inversion problem; in
 * particular, priority must be donated through locks, and through joins.
 */
public class PriorityScheduler extends Scheduler {
    /**
     * Allocate a new priority scheduler.
     */
    public PriorityScheduler() {
    }
    
    /**
     * Allocate a new priority thread queue.
     *
     * @param	transferPriority	<tt>true</tt> if this queue should
     *					transfer priority from waiting threads
     *					to the owning thread.
     * @return	a new priority thread queue.
     */
    public ThreadQueue newThreadQueue(boolean transferPriority) {
	return new PriorityQueue(transferPriority);
    }

    public int getPriority(KThread thread) {
	Lib.assertTrue(Machine.interrupt().disabled());
		       
	return getThreadState(thread).getPriority();
    }

    public int getEffectivePriority(KThread thread) {
	Lib.assertTrue(Machine.interrupt().disabled());
		       
	return getThreadState(thread).getEffectivePriority();
    }

    public void setPriority(KThread thread, int priority) {
	Lib.assertTrue(Machine.interrupt().disabled());
		       
	Lib.assertTrue(priority >= priorityMinimum &&
		   priority <= priorityMaximum);
	
	getThreadState(thread).setPriority(priority);
    }

    public boolean increasePriority() {
	boolean intStatus = Machine.interrupt().disable();
		       
	KThread thread = KThread.currentThread();

	int priority = getPriority(thread);
	if (priority == priorityMaximum)
	    return false;

	setPriority(thread, priority+1);

	Machine.interrupt().restore(intStatus);
	return true;
    }

    public boolean decreasePriority() {
	boolean intStatus = Machine.interrupt().disable();
		       
	KThread thread = KThread.currentThread();

	int priority = getPriority(thread);
	if (priority == priorityMinimum)
	    return false;

	setPriority(thread, priority-1);

	Machine.interrupt().restore(intStatus);
	return true;
    }

    /**
     * The default priority for a new thread. Do not change this value.
     */
    public static final int priorityDefault = 1;
    /**
     * The minimum priority that a thread can have. Do not change this value.
     */
    public static final int priorityMinimum = 0;
    /**
     * The maximum priority that a thread can have. Do not change this value.
     */
    public static final int priorityMaximum = 7;    

    /**
     * Return the scheduling state of the specified thread.
     *
     * @param	thread	the thread whose scheduling state to return.
     * @return	the scheduling state of the specified thread.
     */
    protected ThreadState getThreadState(KThread thread) {
	if (thread.schedulingState == null)
	    thread.schedulingState = new ThreadState(thread);

	return (ThreadState) thread.schedulingState;
    }

    /**
     * A <tt>ThreadQueue</tt> that sorts threads by priority.
     */
    protected class PriorityQueue extends ThreadQueue {
	PriorityQueue(boolean transferPriority) {
	    this.transferPriority = transferPriority;
	}

	public void waitForAccess(KThread thread) {
	    Lib.assertTrue(Machine.interrupt().disabled());
	    getThreadState(thread).waitForAccess(this);
	}

	public void acquire(KThread thread) {
	    Lib.assertTrue(Machine.interrupt().disabled());
	    getThreadState(thread).acquire(this);
    	
    }

	public KThread nextThread() {
	    Lib.assertTrue(Machine.interrupt().disabled());
	    // implement me
	
		if (this.transferPriority && this.holder != null) //if (holder and transfer priority exists)
		{
	
			this.holder.resource.remove(this); //remove from list
	
		}

		if (waitQueue.isEmpty())
            		{ //if (waitQueue is empty)
	    
	    		return null;//return null
			
            		}
	
            KThread FT = pickNextThread(); //ThreadState FT = pickNextThread();
            
            if (FT != null) { 
            	
                waitQueue.remove(FT); //remove FT from waitQueue
                
                getThreadState(FT).acquire(this); //FT.acquire();
            }
            
            return FT;
        }

	/**
	 * Return the next thread that <tt>nextThread()</tt> would return,
	 * without modifying the state of this queue.
	 *
	 * @return	the next thread that <tt>nextThread()</tt> would
	 *		return.
	 */
	protected ThreadState pickNextThread() {
		KThread next= null; //ThreadState X = null
		
		
            	for (Iterator<KThread> TState = waitQueue.iterator(); TState.hasNext();) {  //for each ThreadState TState in waitQueue
            	
                KThread thread = TState.next();  
                
                
                int priority = getThreadState(thread).getEffectivePriority(); //priority of the next thread
                
                if (next == null || priority > getThreadState(next).getEffectivePriority()) { //if (X = null OR TState priority/time > than X 
                	
                    next = thread; //set X to TState
                    
                }
                
            }

            return next; //return X;
        }
	
	public void setDirty() 
	{
		
            if (transferPriority == false) {//if (transfer priority doesn’t exist)
            	//no need to recurse 
                return; //return
            	
            }

            dirty = true;//dirty = true;

            if ( holder != null) //If (holder exists)
            {
            	
                holder.setDirty(); //holder.setDirty()
                
            }
            
        }
         public int getEffectivePriority() {

            
            if (transferPriority == false) { //if (transfer priority does not exist)
            
                return priorityMinimum; //return min priority;
            }

            if (dirty) //if (dirty)
            {

                effective = priorityMinimum; //Effective= min priority;
                
                for (Iterator<KThread> S = waitQueue.iterator(); S.hasNext();) { //for each ThreadState S in waitQueue
                	
                    KThread thread = S.next(); 
                    
                    int newPriority = getThreadState(thread).getEffectivePriority(); //effective = MAX(effective, S.getEffectivePriority())
                    
                    if (effective < newPriority) { 
                    	
                        effective = newPriority;
                   
                    }
                }
                
                dirty = false; //dirty = false;
            }

            return effective; //return Effective;
        }

	public void print() {
	    Lib.assertTrue(Machine.interrupt().disabled());
	    // implement me (if you want)
	}

	/**
	 * <tt>true</tt> if this queue should transfer priority from waiting
	 * threads to the owning thread.
	 */
	public boolean transferPriority;
	private ThreadState holder = null;   //holder  
        private LinkedList<KThread> waitQueue = new LinkedList<KThread>(); //wait queue
 	private int effective; //highest val
        private boolean dirty; //set to true       

      
     

    }

    /**
     * The scheduling state of a thread. This should include the thread's
     * priority, its effective priority, any objects it owns, and the queue
     * it's waiting for, if any.
     *
     * @see	nachos.threads.KThread#schedulingState
     */
    protected class ThreadState {
	/**
	 * Allocate a new <tt>ThreadState</tt> object and associate it with the
	 * specified thread.
	 *
	 * @param	thread	the thread this state belongs to.
	 */
	public ThreadState(KThread thread) {
	    this.thread = thread;
	    
	    setPriority(priorityDefault);
	}

	/**
	 * Return the priority of the associated thread.
	 *
	 * @return	the priority of the associated thread.
	 */
	public int getPriority() {
		
	    return priority;
	}

	/**
	 * Return the effective priority of the associated thread.
	 *
	 * @return	the effective priority of the associated thread.
	 */
	public int getEffectivePriority() {

              int maxEffective = this.priority;

        if (dirty) { //if (dirty)
        	
            for (Iterator<ThreadQueue> T = resource.iterator(); T.hasNext();) {  //for each PriorityQueue being held
            	
                PriorityQueue PQ = (PriorityQueue)(T.next());   // effective = MAX(effective, PQ.getEffectivePriority)

                
                int eff = PQ.getEffectivePriority(); 
                
                if (eff > maxEffective ) {
                	
                    maxEffective = eff;
                    
                }
                
            }
            
        }
            
	    return maxEffective; //return
	}

	/**
	 * Set the priority of the associated thread to the specified value.
	 *
	 * @param	priority	the new priority.
	 */
	public void setPriority(int priority) {
	    if (this.priority == priority) 
		return;
	    
	    this.priority = priority; //set priority to argument
	    
	    setDirty(); //setDirty();
	    // implement me
	}

	/**
	 * Called when <tt>waitForAccess(thread)</tt> (where <tt>thread</tt> is
	 * the associated thread) is invoked on the specified priority queue.
	 * The associated thread is therefore waiting for access to the
	 * resource guarded by <tt>waitQueue</tt>. This method is only called
	 * if the associated thread cannot immediately obtain access.
	 *
	 * @param	waitQueue	the queue that the associated thread is
	 *				now waiting on.
	 *
	 * @see	nachos.threads.ThreadQueue#waitForAccess
	 */
	public void waitForAccess(PriorityQueue waitQueue) {
	    // implement me
	 Lib.assertTrue(Machine.interrupt().disabled()); //no interrupts
	 
	 Lib.assertTrue(waitQueue.waitQueue.indexOf(thread) == -1); //line 
		

	waitQueue.waitQueue.add(thread); //add thread to waitingQueue
        
        waitQueue.setDirty();  //set them as dirty

     
        waitingOn = waitQueue; //waitingOn list dirty set waitQueue

    
        if (resource.indexOf(waitQueue) != -1) { //if waitQueue is in resource
            
            resource.remove(waitQueue); //remove it
            
            waitQueue.holder = null; // holder -> null
       
        }
	
	}

	/**
	 * Called when the associated thread has acquired access to whatever is
	 * guarded by <tt>waitQueue</tt>. This can occur either as a result of
	 * <tt>acquire(thread)</tt> being invoked on <tt>waitQueue</tt> (where
	 * <tt>thread</tt> is the associated thread), or as a result of
	 * <tt>nextThread()</tt> being invoked on <tt>waitQueue</tt>.
	 *
	 * @see	nachos.threads.ThreadQueue#acquire
	 * @see	nachos.threads.ThreadQueue#nextThread
	 */
	public void acquire(PriorityQueue waitQueue) {
	    // implement me
	    
	 	Lib.assertTrue(Machine.interrupt().disabled()); //no interrupts
	

		 resource.add(waitQueue); //add waitQueue to resource list
        
 
        	if (waitQueue == waitingOn) // //if (waitQueue in waitingOn list)
        	{
        
        		 waitingOn = null; //remove
        
        	
		 }

        	setDirty(); //setDirty();  because may be diff
		

	}
	
	
public void setDirty() {

        if (dirty) //if ( dirty)
        {
            return; //return dirty = true;
        }

        dirty = true;

        PriorityQueue PQ = (PriorityQueue)waitingOn; //for (each PriorityQueue that the thread waits for)
       
        if (PQ != null) //if it exists
        {
        	
            PQ.setDirty(); //PQueue.setDirty
        }

    }

	/** The thread with which this object is associated. */	   
	protected KThread thread;
	/** The priority of the associated thread. */
	protected int priority;
	protected int effectivePriority;          
	protected ThreadQueue waitingOn; 
	protected LinkedList<ThreadQueue> resource = new LinkedList<ThreadQueue>(); 
	private boolean dirty = false;                 
    }
}
