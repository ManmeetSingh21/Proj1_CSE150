package nachos.threads;
import nachos.ag.BoatGrader;
import nachos.machine.*;  
    
public class Boat
{
    static BoatGrader bg;
    static Lock boatLock = new Lock();     // boat holds a lock 
    static Communicator coms = new Communicator();        
    // define two location
    static final int Oahu = 0;
    static final int Molokai = 1;

    static int boatLocation = Oahu;        // where is the boat
    static int cntPassengers = 0; 
                            
	//condition variables
    static Condition2 onOahu     = new Condition2(boatLock);
    static Condition2 onMolokai  = new Condition2(boatLock);
    static Condition2 boardBoat = new Condition2(boatLock);
    
	
    static int childrenOnOahu = 0;
    static int adultsOnOahu = 0;
    static int childrenOnMolokai = 0;
    static int adultsOnMolokai = 0;
	
   
     
    public static void selfTest()
    {
	BoatGrader b = new BoatGrader();
	 
	System.out.println("\n***Testing Boats with VARs***");

	// begin(0, 2, b);
    
    // Var1: just one child 
    // expected result: OK
	// begin(0, 1, b);

    // Var2: two child 
    // expected result: OK
	// begin(0, 2, b);

    // Var3: three child 
    // expected result: OK
	// begin(0, 3, b);

    // Var4: one adult
    // expected result: Failed
	// begin(1, 0, b);
    //
    // Var5: one adult, one child
    // expected result: Failed
	// begin(1, 1, b);

    // Var6: one adult, two child
    // expected result: OK
	// begin(1, 2, b);
    
    // Var7: one adult, three child
    // expected result: OK
	// begin(1, 3, b);
    
    // Var8: two adult, two child
    // expected result: OK
	// begin(2, 2, b);
    //
    // Var9: two adult, two child
    // expected result: OK
	// begin(3, 2, b);

    // Var10: lots of adult, two child
    // expected result: OK
	// begin(10, 2, b);

    // Var11: lots of adult, lots of child
    // expected result: OK
	// begin(10, 20, b);

    // Var12: stress testing
    // expected result: OK
	begin(100, 50, b);

    /*
	begin(2, 2, b);
	begin(3, 2, b);
	begin(4, 2, b);
	begin(1, 3, b);
	begin(2, 3, b);
	begin(3, 3, b);
	begin(4, 3, b);
    */
     
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

	
	// Create threads here. See section 3.4 of the Nachos for Java
	// Walkthrough linked from the projects page.
    Runnable runChild = new Runnable() {
        public void run() {
            int location = Oahu;  // thread local varialbe, indicate where person is
            ChildItinerary(location);
        };
    };

    Runnable runAdult = new Runnable() {

        public void run() {
            int location = Oahu;  // thread local varialbe, indicate where person is
            AdultItinerary(location);
        };
    };
     
    for (int i = 1; i <= children; i++) {
        KThread k = new KThread(runChild);
        k.setName("Child Thread" + i);

        k.fork();
    }
     
    for (int i = 1; i <= adults; i++) {
        KThread k = new KThread(runAdult);
        k.setName("Adult Thread" + i);
        k.fork();
    }
        
    while(true) 
    {
        int wordReceived = coms.listen();
        System.out.println("Count On Molokai: " + wordReceived);
        if ( wordReceived == children + adults){
            break;
        }
    }
        
    }

    static void ChildItinerary(int location)
    {
       boatLock.acquire(); 

       while (true) {

            if (location == Oahu)
            {

               // wait until boat's arrival and available seat on boat
               // if only one child left in Oahu, adults go first
               while (boatLocation != Oahu || cntPassengers >= 2
                       || (adultsOnOahu > 0 && childrenOnOahu == 1) ) 
               {
                   onOahu.sleep();
               }

               onOahu.wakeAll();
                
               // if no adult and only one child left in Oahu, the child row to Molokai directly 
               if (adultsOnOahu == 0 && childrenOnOahu == 1) 
               {
                   childrenOnOahu--;
                   bg.ChildRowToMolokai();

                   boatLocation = Molokai;
                   location = Molokai; 
                   childrenOnMolokai++;

                   // clear passenger number after arrival
                   cntPassengers = 0;

                   // collate the number of people in Molokai
                   coms.speak(childrenOnMolokai+adultsOnMolokai);

                   // child arrives in Molokai, to wake up one person in Molokai
                   // at this point, all the persons should be on Molokai
                   // onMolokai.wakeAll();
                    
                   // current child is sleeping in Molokai
                   onMolokai.sleep();
                    
               }
               else if (childrenOnOahu > 1) // send children to Molokai first
               {
                   if (cntPassengers == 1) //if 1 passenger SECOND child boards
                   {  
						cntPassengers++;
                        // notify the fisrt guy to row to Molokai
                        boardBoat.wake();
                        boardBoat.sleep();

                        // then ride myself to Molokai
                        childrenOnOahu--;
                        bg.ChildRideToMolokai();

                        // all the children get off boat, decrease passenger number
                        cntPassengers = cntPassengers - 2;

                        // note, now boat arrives on Molokai
                        boatLocation = Molokai;
                        location = Molokai; 

                        childrenOnMolokai++;

                        coms.speak(childrenOnMolokai+adultsOnMolokai);

                        // two children arrive in Molokai, wake up one child in Molokai
                        onMolokai.wakeAll();

                        // current child is sleeping
                        onMolokai.sleep();
                   }
                   // the first passenger(pilot) rows to Molokai
                   else if (cntPassengers == 0) 
                   {    
			cntPassengers++;
                        // only one child on boat, wait for next child(passenger)  coming
                        boardBoat.sleep();
                        
                        childrenOnOahu--;
                        
                        bg.ChildRowToMolokai();

                        location = Molokai; 
                        childrenOnMolokai++;
                        
                        // notify another passenger on baord to leave
                        boardBoat.wake();

                        // current child is sleeping
                        onMolokai.sleep();
                   }
               } // if childrenOnOahu > 1
            }
            else if (location == Molokai) 
            {
               //Lib.assertTrue(childrenOnMolokai > 0);

               while (boatLocation != Molokai) 
               {
                   onMolokai.sleep();
               }

               // note, just need one child pilot back to Oahu
               childrenOnMolokai--;
               bg.ChildRowToOahu();

               boatLocation = Oahu;
               location = Oahu; 
               childrenOnOahu++;

               onOahu.wakeAll();
               onOahu.sleep();
            }
			else{
				System.out.println("ERROR: Location other than 0 or 1");
				Lib.assertTrue(false);
				break;
			}

        } // while (true)

        boatLock.release(); 
    }

    static void AdultItinerary(int location)
    {
       boatLock.acquire(); 

       while (true)
       {
           if (location == Oahu)
           {
               // child first, then send adults to Molokai
               // but leave one child in Oahu
               while (cntPassengers > 0 
                       || childrenOnOahu > 1 || boatLocation != Oahu) 
               {
                   onOahu.sleep();
               }

               bg.AdultRowToMolokai();
               adultsOnOahu--;

               boatLocation = Molokai;
               adultsOnMolokai++;

               location = Molokai; 
               coms.speak(childrenOnMolokai+adultsOnMolokai);

               Lib.assertTrue(childrenOnMolokai > 0);

               // adult arrive in Molokai, wake up one child in Molokai
               onMolokai.wakeAll();

               // current adult is sleeping
               onMolokai.sleep();
           }
           else if (location == Molokai)
           {
               onMolokai.sleep();
           }
           else 
           {
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
