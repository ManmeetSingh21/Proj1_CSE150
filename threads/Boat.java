package nachos.threads;
import nachos.ag.BoatGrader;
import nachos.machine.*;  
    
public class Boat
{
    static BoatGrader bg;

    static Lock boatLock = new Lock();     
    static Communicator coms = new Communicator();
	
    static int boatLocation = 0;        
    static int countOnBoat = 0; 
	
    //condition variables
    static Condition2 onOahu     = new Condition2(boatLock);
    static Condition2 onMolokai  = new Condition2(boatLock);
    static Condition2 boardBoat = new Condition2(boatLock);
  
    //global variables
    static int childrenOnOahu = 0;
    static int adultsOnOahu = 0;
    static int childrenOnMolokai = 0;
    static int adultsOnMolokai = 0;
    //static int currentLocation = 0; // Oahu = 0, Molokai = 1 
  
     
    public static void selfTest()
    {
		BoatGrader b = new BoatGrader();
		 
		System.out.println("\n ***Testing Boats with only 2 children***");
		begin(0, 2, b);
		 
		// System.out.println("\n ***Testing Boats with 2 children, 1 adult***");
		// 	 begin(1, 2, b);
		 
		// System.out.println("\n ***Testing Boats with 3 children, 3 adults***");
		//   begin(3, 3, b);
    }
     
    public static void begin( int adults, int children, BoatGrader b)
    {

		// Store the externally generated autograder in a class
		// variable to be accessible by children.
		bg = b;

		childrenOnOahu = children;
		adultsOnOahu = adults;
		childrenOnMolokai = 0;
		adultsOnMolokai = 0;

		//Create child threads
		Runnable runChild = new Runnable() {
			public void run() {
				int location = 0;  
				ChildItinerary(location);
			};
		};
		
		for (int i = 1; i <= children; i++) {
			KThread k = new KThread(runChild);
			k.setName("Child Thread" + i);

			k.fork();
		}

		//Create adult threads
		Runnable runAdult = new Runnable() {

			public void run() {
				int location = 0;  
				AdultItinerary(location);
			};
		};

		for (int i = 1; i <= adults; i++) {
			KThread k = new KThread(runAdult);
			k.setName("Adult Thread" + i);
			k.fork();
		}
		

		//Communicator keeps listening until Count On Molokai = total number of people
		while(true){
			int wordReceived = coms.listen();
			System.out.println("Count On Molokai: " + wordReceived);
			if ( wordReceived == (children + adults)){
				break;
			}
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
		boatLock.acquire(); 

		while (true){ //continuous loop
			//check location
            if (location == 0){ //Oahu
               //may need while loop instead of if loop here...
               while ( childrenOnOahu > 1 || countOnBoat > 0 || boatLocation != 0){
                   onOahu.sleep();
               }
			   /*if( childrenOnOahu > 1) {
					onOahu.sleep();
				}
				//check number of children on boat | adults on boat
				if( countOnBoat > 0){
					onOahu.sleep();
				}
				//make sure boat is on Oahu
				if(boatLocation != 0){
					onOahu.sleep();
				} */
			//-------------------------------------------------------
               bg.AdultRowToMolokai();
               
               //update count and location
               adultsOnOahu--;
               adultsOnMolokai++;
               boatLocation = 1;
               location = 1;
               
               //communicating number of people on Molokai
               coms.speak(childrenOnMolokai + adultsOnMolokai);
              
              //wake everyone up and sleep on Molokai
               onMolokai.wakeAll();
               onMolokai.sleep();
			   
		//Make sure there is at least ONE child on Molokai
		Lib.assertTrue(childrenOnMolokai > 0);
           }
           else if (location == 1){ //Molokai
               onMolokai.sleep();
           }
           else{
               System.out.println("ERROR: Location other than 0 or 1");
               Lib.assertTrue(false);
               break; 
           }
       }

       boatLock.release(); 
    }
	
	
    static void ChildItinerary(int location)
    {
		//acquire boat lock
		boatLock.acquire(); 

       while (true) {
            if (location == 0){ //Oahu
	       		//----Potentially put this all in one if or while statement...
				
				//check number of children and adults on Oahu
				//check number of children on boat
				//check boat is on Oahu
               while ( (adultsOnOahu > 0 && childrenOnOahu == 1) || countOnBoat >= 2 || boatLocation != 0  ){
                   onOahu.sleep();
               }
				//----------------------------------------------------
				//if everyone is not awake WAKE EM.
               onOahu.wakeAll();
                
		//LAST CASE: if last child on Oahu...just row to Molokai
		if (adultsOnOahu == 0 && childrenOnOahu == 1){
                   
                   bg.ChildRowToMolokai();

		   //update count and location
		   childrenOnOahu--;
		   childrenOnMolokai++;
		   countOnBoat = 0;
                   boatLocation = 1;
                   location = 1; 
                   
		//communicating number of people on Molokai
                   coms.speak(childrenOnMolokai+adultsOnMolokai);
                   onMolokai.sleep(); //this should be the last and all ppl are on Molokai now
                    
               }
               else if (childrenOnOahu > 1){ //2 children must board boat
		//check boat contents
                    if (countOnBoat == 0){ //if nobody 1 child boards boat
                    	countOnBoat++;
                        boardBoat.sleep(); //first child boards boat and waits for second child
                        
                        childrenOnOahu--;
                        bg.ChildRowToMolokai();
						
			//update for when child arrives on Molokai
                        childrenOnMolokai++;
                        location = 1; 
                        
			//wake other child and sleep on Molokai 
                        boardBoat.wake();
                        onMolokai.sleep();
                   }
		   else if (countOnBoat == 1){ //if 1 passenger SECOND child boards
		   	countOnBoat++;
                        boardBoat.wake(); //wake PILOT child
                        boardBoat.sleep();

                        childrenOnOahu--;
                        bg.ChildRideToMolokai();
			//System.out.println("2 children rowing to Molokai");
						
                        //get off boat
			countOnBoat = countOnBoat - 2;

			//update location and count
                        boatLocation = 1;
                        location = 1; 
                        childrenOnMolokai++;

                        coms.speak(childrenOnMolokai+adultsOnMolokai);

                        //wake all and go to sleep on Molokai
                        onMolokai.wakeAll();
                        onMolokai.sleep();
                   }
                  
               } 
            }
            else if (location == 1){ //Molokai
               while (boatLocation != 1) {
                   onMolokai.sleep();
               }

               childrenOnMolokai--;
               bg.ChildRowToOahu();

		//update location and count
               childrenOnOahu++;
		boatLocation = 0;
               location = 0; 
               

		//wake all on Oahu
               onOahu.wakeAll();
               onOahu.sleep(); // go to sleep on Oahu
            }
            else{
		System.out.println("ERROR: Location other than 0 or 1");
		Lib.assertTrue(false);
		break;
            	
            }

        } 

        boatLock.release(); 
    }

   

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

} 
