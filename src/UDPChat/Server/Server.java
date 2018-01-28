package UDPChat.Server;

import java.io.IOException;

//
// Source file for the server side. 
//
// Created by Sanny Syberfeldt
// Maintained by Marcus Brohede
//

import java.net.*;
//import java.io.*;
import java.util.ArrayList;
import java.util.Iterator;

import Commons.PacketHandler;

public class Server {

	private ArrayList<ClientConnection> m_connectedClients = new ArrayList<ClientConnection>();
	private DatagramSocket m_socket;
	private PacketHandler packetHandler = new PacketHandler();
	private int serverMessageID = 0;
	private boolean socketTimedOut;

	public static void main(String[] args) {
		controlSizeOfInputString(args);
		Server instance = createServerInstance(args[0]);
		instance.listenForClientMessages();
	}

	private Server(int portNumber) {
		m_socket = createDatagramSocket(portNumber);
	}

	private void listenForClientMessages() {
		System.out.println("Waiting for client messages... ");
		int checkCount = 0;
		do {
			socketTimedOut = false;
			DatagramPacket msgPacket = getPacketMessage(m_socket);
			if (socketTimedOut && m_connectedClients.size() > 0) {
				checkClients(checkCount);
				++checkCount;
			} else if (!socketTimedOut){
				executeMessage(msgPacket);
			}

		} while (true);
	}

	private void executeMessage(DatagramPacket p) {
		String message = new String(p.getData());
		String arr[] = message.split("\\s+", 4);
		String type = arr[2].trim();
		String clientWhoSent = arr[0].trim();
		int messageID = Integer.parseInt(arr[1]);

		setClientAsActive(clientWhoSent);

		if (packetHandler.isANewMessage(clientWhoSent, messageID)) {
			switch (type) {
			case "/connect":
				if (addClient(clientWhoSent, p.getAddress(), p.getPort())) {
					broadcast(clientWhoSent + " " + messageID + " " + clientWhoSent + " has connected!");
				}
				break;
			case "/leave":
				if (removeClientFromServer(clientWhoSent)) {
					broadcast(clientWhoSent + " " + messageID + " " + clientWhoSent
							+ " has left the server!");
				}
				break;
			case "/tell":
				String splitForClientToSend[] = arr[3].split("\\s+", 2);
				String clientToSend = splitForClientToSend[0].trim();
				message = clientWhoSent + " " + messageID + " " + clientWhoSent + " to " + clientToSend + ": "
						+ splitForClientToSend[1];
				if (clientIsConnected(clientWhoSent) && clientIsConnected(clientToSend)) {
					sendPrivateMessage(message, clientToSend);
					sendPrivateMessage(message, clientWhoSent);
				}
				break;
			case "/all":
				if (clientIsConnected(clientWhoSent)) {
					message = clientWhoSent + " " + messageID + " " + clientWhoSent + " to " + "all" + ": " + arr[3];
					broadcast(message);
				}
				break;
			case "/list":
				if (clientIsConnected(clientWhoSent)) {
					String newMessage = clientWhoSent + " " + messageID + " " + "Connected Clients: ";
					for (ClientConnection cc : m_connectedClients) {
						newMessage += cc.getName() + ", ";
					}
					sendPrivateMessage(newMessage, clientWhoSent);
				}
				break;
			}
			packetHandler.markPacketAsReceived(clientWhoSent, messageID);
		}
	}

	public boolean addClient(String name, InetAddress address, int port) {
		ClientConnection c;
		for (Iterator<ClientConnection> itr = m_connectedClients.iterator(); itr.hasNext();) {
			c = itr.next();
			if (c.hasName(name)) {
				return false; // Already exists a client with this name
			}
		}
		m_connectedClients.add(new ClientConnection(name, address, port));
		return true;
	}

	public void sendPrivateMessage(String message, String name) {
		ClientConnection c;
		for (Iterator<ClientConnection> itr = m_connectedClients.iterator(); itr.hasNext();) {
			c = itr.next();
			if (c.hasName(name)) {
				c.sendMessage(message, m_socket);
			}
		}
	}

	public void broadcast(String message) {
		for (Iterator<ClientConnection> itr = m_connectedClients.iterator(); itr.hasNext();) {
			itr.next().sendMessage(message, m_socket);
		}
	}

	/*
	 * -----------------------------------------------------------------------------
	 * Private functions used by the different public methods that enable the
	 * functionality of the program
	 * -----------------------------------------------------------------------------
	 */

	private static void controlSizeOfInputString(String[] args) {
		if (args.length < 1) {
			System.err.println("Usage: java Server portnumber");
			System.exit(-1);
		}
	}

	private static Server createServerInstance(String port) {
		try {
			Server instance = new Server(Integer.parseInt(port));
			return instance;
		} catch (NumberFormatException e) {
			System.err.println("Error: port number must be an integer.");
			System.exit(-1);
		}
		return null;
	}

	private DatagramSocket createDatagramSocket(int port) {
		try {
			DatagramSocket socket = new DatagramSocket(port);
			socket.setSoTimeout(300);
			return socket;
		} catch (SocketException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return null;
	}

	private DatagramPacket getPacketMessage(DatagramSocket socket) {
		DatagramPacket incoming = getDatagramToReceive();
		try {
			socket.receive(incoming);
		} catch (SocketTimeoutException e) {
			socketTimedOut = true;
			return null;
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return incoming;
	}

	private DatagramPacket getDatagramToReceive() {
		byte[] buffer = new byte[1024];
		DatagramPacket receiveablePacket = new DatagramPacket(buffer, buffer.length);
		return receiveablePacket;
	}

	private void checkClients(int currentLoop) {
		if (currentLoop % 5 == 0) {
			removeInactiveClients();
			setClientsAsInactive();
		}
		sendMessageToInactiveClient();
	}

	private void setClientsAsInactive() {
		for (ClientConnection cc : m_connectedClients) {
			cc.resetActive();
		}
	}

	private void setClientAsActive(String name) {
		for (ClientConnection cc : m_connectedClients) {
			if (cc.hasName(name)) {
				cc.clientIsActive();
			}
		}
	}

	
	private void sendMessageToInactiveClient() {
		for (ClientConnection cc : m_connectedClients) {
			if (!cc.isActive()) {
				sendPrivateMessage("Server" + " " + serverMessageID + " " + "/CC respond to continiue being connected!", cc.getName());
			}
		}
		++serverMessageID;
	}
	
	private void removeInactiveClients() {
		int index = 0;
		ClientConnection cc;
		while(index < m_connectedClients.size()) {
			cc = m_connectedClients.get(index);
			if(!cc.isActive()) {
				if (removeClientFromServer(cc.getName())) {
					broadcast("Sever" + " " + serverMessageID + " " + cc.getName() + " has disconnected from the server!");
					++serverMessageID;
				}
			}
			else {
				++index;
			}
		}
	}
	
	private boolean removeClientFromServer(String name) {
		for (ClientConnection cc : m_connectedClients) {
			if (name.equals(cc.getName())) {
				m_connectedClients.remove(m_connectedClients.indexOf(cc));
				packetHandler.removeClient(name);
				return true;
			}
		}
		return false;
	}

	private boolean clientIsConnected(String name) {
		ClientConnection c;
		for (Iterator<ClientConnection> itr = m_connectedClients.iterator(); itr.hasNext();) {
			c = itr.next();
			if (c.hasName(name)) {
				return true; // client exists in the server
			}
		}
		return false;
	}

}
