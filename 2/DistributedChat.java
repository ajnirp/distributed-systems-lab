import java.net.*;
import java.rmi.*;
import java.rmi.registry.LocateRegistry;
import java.util.*;
import java.io.*;

class DistributedChat {
	Scanner inUser = null;

	PrintStream outuser = System.out;
	String ID;
	InetAddress MulticastAddress = null;
	private MulticastSocket MultiSocket = null;
	int sendPort = 9999;
	int DatagramSize = 20;
	Thread listenThread = null;

	static int lAgreedSeq;
	static int lProposedSeq;

	static String clientIp = null;
	public static Map<Integer, String> msgIdMap;
	public static SortedMap<Integer, Integer> queuedSeqMap;
	public static SortedMap<Integer, String> deliveredMap;

	private static Map<String, String> userList;
	protected static Vector<String> clients = new Vector<String>();

	private String networkInterfaceType;

	private static boolean doNotReply = false;

	private static String peerToRemove = "";

	DistributedChat(String id, String _networkInterfaceType) {
		ID = id;
		networkInterfaceType = _networkInterfaceType;

		inUser = new Scanner(System.in);

		msgIdMap = new HashMap<Integer, String>();
		queuedSeqMap = new TreeMap<Integer, Integer>();
		deliveredMap = new TreeMap<Integer, String>();
		userList = new HashMap<String, String>();
		lAgreedSeq = 0;
		lProposedSeq = 0;
	}

	public void run() throws IOException {
		Thread rmiThread = new Thread(new Runnable() {
			public void run() {
				try {
					try {
						LocateRegistry.createRegistry(1099);
					} catch (RemoteException ee) {
						outuser.println("Registry could not be created.");
					}

					MessageInterface stub = new Chat();
					NetworkInterface ni = NetworkInterface.getByName(networkInterfaceType);
					Enumeration<InetAddress> inetAddresses = ni
							.getInetAddresses();
					while (inetAddresses.hasMoreElements()) {
						InetAddress ia = inetAddresses.nextElement();
						if (!ia.isLinkLocalAddress()) {
							clientIp = ia.getHostAddress();
						}
					}
					outuser.println("my ip " + clientIp);
					Naming.rebind("rmi://" + clientIp + ":1099/" + ID, stub);
				} catch (Exception e) {
				}
			}
		});

		rmiThread.start();

		listenThread = new Thread(new Runnable() {
			public void run() {
				listenUsers();
			}
		});

		while (true) {
			// System.out.print("#");
			String cmd = inUser.nextLine();
			if (cmd.equals("#Control join#")) {
				try {
					MultiSocket = new MulticastSocket(sendPort);

				} catch (Exception e) {
					System.err.println("Couldnt acquire to port " + sendPort);
				}
				try {
					MulticastAddress = InetAddress.getByName("224.0.55.55");
				} catch (java.net.UnknownHostException e) {
					System.err.println("Unknown Host Reported");
				}
				try {
					SocketAddress socketAddress = new InetSocketAddress(
							MulticastAddress, 9999);
					NetworkInterface ni = NetworkInterface.getByName(networkInterfaceType);
					// outuser.println("joining multicast grp, my ip " + clientIp);
					MultiSocket.joinGroup(socketAddress, ni);
				} catch (IOException e) {
					System.err.println("Unable to join Group");
				}

				String joinMsg = "JOIN:" + ID;
				byte[] buf = new byte[DatagramSize];
				buf = joinMsg.getBytes();
				DatagramPacket packet = new DatagramPacket(buf, buf.length,
						MulticastAddress, sendPort);
				try {
					MultiSocket.send(packet);
					System.out.println("Multicasting Join messages");
					listenThread.start();
				} catch (IOException e) {
				}

			} else if (cmd.split(" ")[0].equals("#Reply") && !doNotReply) {
				// outuser.println("Reply");
				String message =  cmd.substring(6);

				String msgOut = ID + ": " + message;
				int msgId = msgOut.hashCode();

				int msgSeqNum = 0;
				// outuser.println("clients size " + clients.size());
				// outuser.println("user list " + userList.size());
				for (int i = 0; i < clients.size(); i++) {
					// outuser.println("in loop with i = " + i);
					try {
						int val;
						String id = clients.get(i);
						MessageInterface msgI = (MessageInterface) Naming
								.lookup("rmi://" + userList.get(id) + ":1099/"
										+ id);
						val = msgI.askProposal(msgOut, msgId);
						msgSeqNum = Math.max(msgSeqNum, val);
					} catch (Exception e) {
					}
				}
				for (int i = 0; i < clients.size(); i++) {
					try {
						String id = clients.get(i);
						MessageInterface msgI = (MessageInterface) Naming
								.lookup("rmi://" + userList.get(id) + ":1099/"
										+ id);
						msgI.finalMessage(msgId, msgSeqNum);
					} catch (Exception e) {

					}
				}
			} else if (cmd.split(" ")[0].equals("#ReplyTo") && !doNotReply) {
				// ReplyTo
				// outuser.println("#ReplyTo");
				String replyToId = cmd.split(" ")[1];
				if(clients.contains(replyToId)){
					String message =  cmd.replace("#ReplyTo " + replyToId, "");

					String msgOut = ID + ": " + message;
					int msgId = msgOut.hashCode();

					int msgSeqNum = 0;
					// outuser.println("clients size " + clients.size());
					// outuser.println("user list " + userList.size());
					for (int i = 0; i < clients.size(); i++) {
						// outuser.println("in loop with i = " + i);
						try {
							int val;
							String id = clients.get(i);
							MessageInterface msgI = (MessageInterface) Naming
									.lookup("rmi://" + userList.get(id) + ":1099/"
											+ id);
							val = msgI.askProposal(msgOut, msgId);
							msgSeqNum = Math.max(msgSeqNum, val);
						} catch (Exception e) {
						}
					}
					for (int i = 0; i < clients.size(); i++) {
						try {
							String id = clients.get(i);
							MessageInterface msgI = (MessageInterface) Naming
									.lookup("rmi://" + userList.get(id) + ":1099/"
											+ id);
							msgI.finalMessage(msgId, msgSeqNum);
						} catch (Exception e) {

						}
					}
				}

			} else if (cmd.equals("#Control leave#")) {
				String exitMsg = "LEAVE:" + ID;

				for (int i = 0; i < clients.size(); i++) {
					try {
						int val;
						String id = clients.get(i);
						if (!ID.equals(id)) {
							MessageInterface msgI = (MessageInterface) Naming.lookup("rmi://" + userList.get(id) + ":1099/" + id);
							outuser.println("Calling peerLeaving for " + id);
							msgI.peerLeaving(exitMsg);
						}
					} catch (Exception e) {
					}
				}

				System.exit(0);
			}
		}
	}

	public static void removePeer(String peerID) {
		peerToRemove = peerID;

		System.out.println("removePeer called by " + peerID);
		// System.out.println("deliveredMap size " + deliveredMap.size());

		doNotReply = true;

		Thread nonBlockingWait = new Thread(new Runnable() {
			public void run() {
				while (deliveredMap.size() != 0) {}
				clients.remove(peerToRemove);
				userList.remove(peerToRemove);
				doNotReply = false;
				return;
			}
		});

		nonBlockingWait.start();
	}

	public void listenUsers() {
		byte[] buf = new byte[20];
		DatagramPacket newClientJoinMsg = new DatagramPacket(buf, buf.length);

		while (true) {
			try {
				outuser.println("Listening...");
				MultiSocket.receive(newClientJoinMsg);
				String newClientJoinMsgStr = new String(
						newClientJoinMsg.getData(), 0,
						newClientJoinMsg.getLength());
				// outuser.println("Received message " + newClientJoinMsgStr);
				String[] fields = newClientJoinMsgStr.split(":");
				if (fields[0].equals("JOIN")) {
					String id = fields[1];
					if (id == ID) {
					}

					else {
						if (!clients.contains(id)) {
							outuser.println("Adding client using multicast " + id);
							clients.add(id);
							userList.put(id, newClientJoinMsg.getAddress()
									.getHostAddress());
							try {
								// outuser.println("User ip " + userList.get(id));
								MessageInterface msgI = (MessageInterface) Naming.lookup("rmi://" + userList.get(id) + ":1099/" + fields[1]);
								msgI.confirmJoin(ID, clientIp);
							} catch (Exception e) {
								e.printStackTrace();
							}
						}
					}
				}
				if (fields[0].equals("LEAVE")) {
				}
			} catch (IOException e) {
			}
		}
	}

	public static int sendProposal(String message, int messageId) {
		msgIdMap.put(messageId, message);
		lProposedSeq = Math.max(lAgreedSeq, lProposedSeq) + 1;
		queuedSeqMap.put(lProposedSeq, messageId );
		// System.out.println("Send Proposal " + messageId + " " + lProposedSeq);
		return lProposedSeq;
	}

	public static int readyToDeliver(int messageId, int seqNo) {
		lAgreedSeq = Math.max(seqNo, lAgreedSeq);
		while(queuedSeqMap.values().remove(messageId));
		queuedSeqMap.put(seqNo, messageId);
		deliveredMap.put(messageId, msgIdMap.get(messageId));
		msgIdMap.remove(messageId);
		//Printing deliverable messages
		Iterator it = queuedSeqMap.entrySet().iterator();
		// System.out.println("Added to delivereables " + messageId + " " + deliveredMap.get(messageId) + "  " + seqNo);
		while (it.hasNext()) {
	        Map.Entry pairs = (Map.Entry)it.next();
	        if(deliveredMap.containsKey(pairs.getValue())){
	        	System.out.println("#" + deliveredMap.get(messageId));
	        	deliveredMap.remove(messageId);
	        	it.remove();
	        } else{
	        	break;
	        }
	    }




		return 0;
	}

	// public static void displayMessage(String message, int clientId) {
	// outuser.println(clientId + ":" + message);
	// }

	public static void addClient(String clientId, String ip) {
		if (!clients.contains(clientId)) {
			System.out.println("Adding client using rmi " + clientId);
			clients.add(clientId);
			userList.put(clientId, ip);
		} else{

		}
	}

	public static void main(String[] args) throws IOException {
		if (args.length != 2) {
			System.out.println("Syntax: <clientid> <network interface>");
			return;
		}
		new DistributedChat(args[0], args[1]).run();
	}
};
