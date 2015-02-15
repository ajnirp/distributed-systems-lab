import java.rmi.Remote;
import java.rmi.RemoteException;


public interface MessageInterface extends Remote {
	
	public void confirmJoin(String clientId, String ip) throws RemoteException; // Acknowledgement by peers to the join message.
	
	// public void getMessage(String message, int msgID, int peerID) throws RemoteException;  // get Message from peer.

	public int askProposal(String message, int messageId) throws RemoteException;

	public void finalMessage(int messageId, int seqNo) throws RemoteException;

	public void peerLeaving(String exitMsg) throws RemoteException;
}
