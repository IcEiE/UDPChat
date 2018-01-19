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

public class Server {

	private ArrayList<ClientConnection> m_connectedClients = new ArrayList<ClientConnection>();
	private DatagramSocket m_socket;

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
		int numbOfMessenges = 0;
		do {
			DatagramPacket msgPacket = getPacketMessage(m_socket);
			executeMessage(msgPacket);
			++numbOfMessenges;
			System.out.println("Finished Messages: " + numbOfMessenges);

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
		String arr[] = message.split(" ", 2);
		String type = arr[1].trim();
		switch (type) {
		case "/connect":
			if (addClient(arr[0], p.getAddress(), p.getPort())) {
				sendPrivateMessage("You are now connected!", arr[0]);
			}
			break;
			
		case "/tell":
			break;
		case "/all":
			break;
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
}
