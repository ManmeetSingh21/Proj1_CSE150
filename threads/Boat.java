package nachos.threads;
import nachos.ag.BoatGrader;

/*
	Variables need to create:
 		boatlock?
		location
		boat location
		# ppl on boat?
		
	
	Conditions: Oahu, Molokai
*/


public class Boat
{
    static BoatGrader bg;
	
	//adding global variables needed
    static Lock boatLock = new Lock();
	static Communicator coms = new Communicator();
	
	
	
    public static void selfTest()
    {
	BoatGrader b = new BoatGrader();
	
	System.out.println("\n ***Testing Boats with only 2 children***");
	begin(0, 2, b);

//	System.out.println("\n ***Testing Boats with 2 children, 1 adult***");
//  	begin(1, 2, b);

//  	System.out.println("\n ***Testing Boats with 3 children, 3 adults***");
//  	begin(3, 3, b);

	//TEST CASES:
	/*
		//CASE 1: 1 child
			begin(1,0,b);
			//Expectation: SUCCESS
			
		//CASE 2: 2 children
			begin(2,0,b);
			//Expectation: SUCCESS
			
		//CASE 3: 2 children, 1 adult
			begin(2,1,b);
			//Expectation: SUCCESS
			
		//CASE 4: 3 children
			begin(3,0,b);
			//Expectation: SUCCESS
			
		//CASE 5: 3 children, 5 adults
			begin(3,5,b);
			//Expectation: SUCCESS
			
		//CASE 6: 10 children, 10 adults
			begin(10,10,b);
			//Expectation: SUCCESS
		
		//CASE 7: 2 adults
			begin(0,2,b);
			//Expectation: FAIL
			
	*/
    }

    public static void begin( int adults, int children, BoatGrader b )
    {
	// Store the externally generated autograder in a class
	// variable to be accessible by children.
	bg = b;

	// Instantiate global variables here
	int childrenOnOahu = children;
	int adultsOnOahu = adults;
	int childrenOnMolokai = 0;
	int adultsOnMolokai = 0;
	
	int boatLocation = 0;	
	int countOnBoat = 0;
	int currentLocation = 0; //Oahu = 0 && Molokai = 1
	int wordReceived = 0;
	
	//condition variables
	Condition2 onOahu = new Condition2(boatLock);
	Condition2 onMolokai = new Condition2(boatLock);
	Condition2 boardBoat = new Condition2(boatLock); //for children bc 2 must board
	
		
	// Create threads here. See section 3.4 of the Nachos for Java
	// Walkthrough linked from the projects page.

	/*Runnable r = new Runnable() {
	    public void run() {
                SampleItinerary();
            }
        };
        KThread t = new KThread(r);
        t.setName("Sample Boat Thread");
        t.fork(); */
		
	Runnable runAdult = new Runnable(){
		public void run(){
			int location = 0; //'Molokai'
			AdultItinerary(location);
		}
	}
	
	Runnable runChild = new Runnable(){
		public void run(){
			int location = 0; //'Oahu'
			ChildItinerary(location);
		}
	}
	
	//Create threads for adults
	for(int i=1; i<=adults; i++){
		KThread k = new KThread(runAdult);
		k.setName("Adult Thread " + i);
		k.fork()
	}
	
	//Create threads for children
	for(int i=1; i<=children; i++){
		KThread k = new KThread(runChild);
		k.setName("Child Thread " + i);
		k.fork();
	}
	//keep listening for thread count till all on Molokai
	while(wordReceived != (children+recieved)){
		int wordReceived = coms.listen();
		System.out.println("Count on Molokai: " + wordRecieved);
	}

    }

	
	
	
	
    static void AdultItinerary(int location)
    {
	/* This is where you should put your solutions. Make calls
	   to the BoatGrader to show that it is synchronized. For
	   example:
	       bg.AdultRowToMolokai();
	   indicates that an adult has rowed the boat across to Molokai
	*/
		//acquire boat lock
		acquire();
		
		while(true){ //continuous loop
			//check location
			if (location == 0){ //'Oahu'
				//may need while loop here -------------------------
				//check number of children on Oahu
				if( childrenOnOahu > 1) {
					onOahu.sleep();
				}
				//check number of children on boat | adults on boat
				if( countOnBoat > 0){
					onOahu.sleep();
				}
				//make sure boat is on Oahu
				if(boatLocation != 0){
					onOahu.sleep();
				}
				else{
					bg.AdultRowToMolokai();
					
					//update count
					adultsOnOahu--;
					adultsOnMolokai++;
					coms.speak(childrenOnMolokai+adultsOnMolokai);
					
					boatLocation = 1; //boat is now on Molokai
					
					//wake everyone up
					onMolokai.wakeAll();
				}
			}
			else if(location == 1){ //'Molokai'
				onMolokai.sleep();
			}
			else{
				System.out.println("ERROR: Location other than 0 or 1 in Adult Itinerary");
				break; //there is an error
			}
		}

		//release boat lock
		release();
    }

    static void ChildItinerary(int location)
    {
		//acquire boat lock
		acquire();
		while(true){
			if(location == 0){ //Oahu
				//check number of children and adults on Oahu
				if(adultsOnOahu > 0 && childrenOnOahu == 1){
					onOahu.sleep();
				}
				//check number of children on boat
				else if(countOnBoat == 2){
					onOahu.sleep();
				}
				//check boat is on Oahu
				else if(boatLocation != 0){
					onOahu.sleep();
				}
				else if(childrenOnOahu > 1){ //2 children must board boat
					//check boat contents
					if(countOnBoat == 0){
						boardBoat.sleep(); //board boat and wait for second passenger
						
						//increment update
						countOnBoat++;						
						childrenOnOahu--;
						
						bg.ChildRowToMolokai(); //PILOT child
						
						//update for when child arrives on Molokai
						childrenOnMolokai++;
						location = 1;
						boardBoat.wake(); //wake another child 
						onMolokai.sleep(); //sleep on Molokai
						
					}
					else if(countOnBoat == 1){
						boardBoat.wake(); //wake PILOT child
						boardBoat.sleep(); //sleep on boat for the ride
						
						//increment update
						countOnBoat++;
						childrenOnOahu--;
						
						bg.ChildRideToMolokai(); //PASSENGER child
						
						//get to Molokai
						childrenOnMolokai++;
						countOnBoat = countOnBoat - 2; //get off boat
						location = 1;
						boatLocation = 1;
						
						//communicate 
						coms.speak(childrenOnMolokai + adultsOnMolokai);
						
						//wake all and sleep
						onMolokai.wakeAll();
						onMolokai.sleep();
						
					}
					else if(countOnBoat > 2){
						onOahu.sleep(); //checking any extra cases
					}
				}
				else if(adultsOnOahu == 0 && childrenOnOahu == 1){
					bg.ChildRowToMolokai();
					
					childrenOnOahu--;
					childrenOnMolokai++;
					
					countOnBoat=0;
					boatLocation = 1;
					location = 1;
					
					coms.speak(childrenOnMolokai + adultsOnMolokai);
					waitingOnMolokai.sleep(); //this should be last and all people are on Molokai
					
					
				}
			}
			else if(location == 1){ //Molokai
				//child ALWAYS bring back boat to Oahu
				if(boatLocation != 1){
					onMolokai.sleep();
				}
				else{
					bg.ChildRowToOahu();
					
					//update count
					childrenOnMolokai--;
					childrenOnOahu++;
					location = 0;
					boatLocation = 0;
					
					//wake all and sleep
					onOahu.wakeAll();
					onOahu.sleep();
				}
				
			}
			else{
				System.out.println("ERROR: Location other than 0 or 1 in Child Itinerary");
				break; //there is an error
			}
		}
		
		//release boat lock
		release();
    }

/*
    static void SampleItinerary()
    {
	// Please note that this isn't a valid solution (you can't fit
	// all of them on the boat). Please also note that you may not
	// have a single thread calculate a solution and then just play
	// it back at the autograder -- you will be caught.
	System.out.println("\n ***Everyone piles on the boat and goes to Molokai***");
	bg.AdultRowToMolokai();
	bg.ChildRideToMolokai();
	bg.AdultRideToMolokai();
	bg.ChildRideToMolokai();
    } 
    */
    
}
