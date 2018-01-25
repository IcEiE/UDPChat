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
import Commons.*;

public class Server {

	private ArrayList<ClientConnection> m_connectedClients = new ArrayList<ClientConnection>();
	private DatagramSocket m_socket;
	private MessageHandler messageHandler = new MessageHandler();

	public static void main(String[] args) {
		controlSizeOfInputString(args);
		Server instance = createServerInstance(args);
		instance.listenForClientMessages();
	}

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

	private Server(int portNumber) {
		m_socket = createDatagramSocket(portNumber);
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

	private void listenForClientMessages() {
		System.out.println("Waiting for client messages... ");
		do {
			DatagramPacket msgPacket = getPacketMessage(m_socket);
			executeMessage(msgPacket);
		} while (true);
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

	private void executeMessage(DatagramPacket p) {
		String message = new String(p.getData());
		String arr[] = message.split("\\s+");
		String clientWhoSent = arr[0].trim();
		int packetID = Integer.parseInt(arr[1].trim());
		String type = arr[2].trim();
		System.out.println(message);
		if (!messageHandler.messageAlreadyReceived(clientWhoSent, packetID)) {

			switch (type) {
			case "/connect":
				if (addClient(clientWhoSent, p.getAddress(), p.getPort())) {
					broadcast(clientWhoSent + " " + arr[1] + " has connected!");
				}
				break;
			case "/dc":
				if (removeClientFromServer(clientWhoSent)) {
					broadcast(clientWhoSent + " has disconnected from the server!");
				}
				break;
			case "/tell":
				String clientToSend = arr[2].trim();
				message = trimMessageToSend(message, 3, clientWhoSent, clientToSend);
				if (clientIsConnected(clientWhoSent) && clientIsConnected(clientToSend)) {
					sendPrivateMessage(message, arr[0]);
					sendPrivateMessage(message, arr[2]);
				}
				break;
			case "/all":
				if (clientIsConnected(clientWhoSent)) {
					message = trimMessageToSend(message, 3, clientWhoSent, "all");
					broadcast(message);
				}
				break;
			case "/list":
				if (clientIsConnected(clientWhoSent)) {
					String newMessage = "Connected Clients: ";
					for (ClientConnection cc : m_connectedClients) {
						newMessage += cc.getName() + ", ";
					}
					sendPrivateMessage(newMessage, clientWhoSent);
				}
			}
			messageHandler.markPacketAsReceived(clientWhoSent, packetID);
		}
	}

	

	private boolean removeClientFromServer(String name) {
		for (ClientConnection cc : m_connectedClients) {
			if (name.equals(cc.getName())) {
				m_connectedClients.remove(m_connectedClients.indexOf(cc));
				messageHandler.removeClient(name);
				return true;
			}
		}
		return false;
	}

	private String trimMessageToSend(String message, int indexStart, String sender, String receiver) {
		String arr[] = message.split("\\s+");
		String newMessage = sender + " to " + receiver + ": ";
		for (int i = indexStart; i < arr.length; ++i) {
			newMessage += arr[i] + " ";
		}
		return newMessage;
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
}
