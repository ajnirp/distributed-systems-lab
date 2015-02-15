import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;


public class Chat extends UnicastRemoteObject implements MessageInterface {
	public Chat() throws RemoteException {
	
	}

	public void confirmJoin(String clientId, String ip) throws RemoteException {  
		DistributedChat.addClient(clientId, ip);
	}
	
	// public void getMessage(String message, int msgID, int clientID) throws RemoteException {  // get Message from peer.
	// 	DistributedChat.displayMessage(message, clientID);
	// }

	public int askProposal(String message, int messageId) throws RemoteException {
		return DistributedChat.sendProposal(message,messageId);
	}
	
	public void finalMessage(int msgID, int seqNo) throws RemoteException{
		DistributedChat.readyToDeliver(msgID,seqNo);
	}

	public void peerLeaving(String exitMsg) throws RemoteException {
		String peerID = "";
		if (exitMsg.split(":")[0].equals("LEAVE")) {
			peerID = exitMsg.split(":")[1];
		}
		DistributedChat.removePeer(peerID);
	}

}