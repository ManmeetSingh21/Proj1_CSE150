package nachos.network;

import nachos.machine.*;
import nachos.threads.*;
import java.util.LinkedList;

/* Channel object necessary for connection establishment 
   between ports and server. Contains information about
   source and destination ports, as well as connection 
   state. Sliding Window method is implemented here to 
   handle packet transfer. 
*/

public class Channel extends OpenFile{

	//establish variables
	public int sourceID, sourcePort, destinationID, destinationPort; 
	
	
	public static enum ConnectionState = { CLOSED, SYN_SENT, SYN_RCVD, ESTABLISHED, STP_RCVD, STP_SENT, CLOSING};	
	public ConnectionState state;
	/* 7 Possible connection states:	
			CLOSED -- no connection exists
			SYN_SENT -- sent a SYN packet, waiting for a SYN/ACK packet.
			SYN_RCVD -- received a SYN packet, waiting for an app to call accept().
			ESTABLISHED -- a full-duplex connection has been established, data transfer can take place.
			STP_RCVD -- received a STP packet, can still receive data but cannot send data.
			STP_SENT -- the app called close(), sent an STP packet but still need to retransmit unacknowledged data.
			CLOSING -- send a FIN packet, waiting for a FIN/ACK packet.
	*/
	
	
	
	public Channel(int sourceID, int sourcePort, int destinationID, int destinationPort){
		this.sourceID = sourceID;
		this.sourcePort = sourcePort;
		this.destinationID = destinationID;
		this.destinationPort = destinationPort;
		state = ConnectionState.CLOSED; 
	}
	
	
}
