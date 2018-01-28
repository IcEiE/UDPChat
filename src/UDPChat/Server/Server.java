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

	public static void main(String[] args) {
		controlSizeOfInputString(args);
		Server instance = createServerInstance(args);
		instance.listenForClientMessages();
	}

	private Server(int portNumber) {
		m_socket = createDatagramSocket(portNumber);
	}

	private void listenForClientMessages() {
		System.out.println("Waiting for client messages... ");
		int numbOfMessenges = 0;
		do {
			DatagramPacket msgPacket = getPacketMessage(m_socket);
			executeMessage(msgPacket);

		} while (true);
	}

	private void executeMessage(DatagramPacket p) {
		String message = new String(p.getData());
		String arr[] = message.split("\\s+", 4);
		String type = arr[2].trim();
		String clientWhoSent = arr[0].trim();
		int messageID = Integer.parseInt(arr[1]);
		if (packetHandler.isANewMessage(clientWhoSent, messageID)) {
			switch (type) {
			case "/connect":
				if (addClient(clientWhoSent, p.getAddress(), p.getPort())) {
					broadcast(clientWhoSent + " " + messageID + " " + clientWhoSent + " has connected!");
				}
				break;
			case "/dc":
				if (removeClientFromServer(clientWhoSent)) {
					broadcast(clientWhoSent + " " + messageID + " " + clientWhoSent + " has disconnected from the server!");
				}
				break;
			case "/tell":
				String splitForClientToSend[] = arr[3].split("\\s+", 2);
				String clientToSend = splitForClientToSend[0].trim();
				message = clientWhoSent + " " + messageID + " " + clientWhoSent + " to " + clientToSend + ": " + splitForClientToSend[1];
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
			}
			packetHandler.markPacketAsReceived(clientWhoSent, messageID);
		}
	}

	private boolean removeClientFromServer(String name) {
		for (ClientConnection cc : m_connectedClients) {
			if (name.equals(cc.getName())) {
				m_connectedClients.remove(m_connectedClients.indexOf(cc));
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

	private static Server createServerInstance(String[] args) {
		try {
			Server instance = new Server(Integer.parseInt(args[0]));
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
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return incoming;
	}

	private DatagramPacket getDatagramToReceive() {
		byte[] buffer = new byte[1000];
		DatagramPacket receiveablePacket = new DatagramPacket(buffer, buffer.length);
		return receiveablePacket;
	}

}
