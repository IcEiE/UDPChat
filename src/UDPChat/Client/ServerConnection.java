/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package UDPChat.Client;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.util.Random;
import Commons.*;

/**
 *
 * @author brom
 */
public class ServerConnection {

	// Artificial failure rate of 30% packet loss
	static double TRANSMISSION_FAILURE_RATE = 0;

	private DatagramSocket m_socket = null;
	private InetAddress m_serverAddress = null;
	private int m_serverPort = -1;
	int messageID = 0;
	private MessageHandler messageHandler = new MessageHandler();

	public ServerConnection(String hostName, int port) {
		m_serverPort = port;
		m_serverAddress = createInetAddress(hostName);
		m_socket = createDatagramSocket();
	}

	private InetAddress createInetAddress(String hostName) {
		try {
			InetAddress address = InetAddress.getByName(hostName);
			return address;
		} catch (UnknownHostException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return null;
	}

	private DatagramSocket createDatagramSocket() {
		try {
			DatagramSocket socket = new DatagramSocket();
			return socket;
		} catch (SocketException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return null;
	}

	public boolean handshake(String name) {
		String message = "/connect ";
		sendChatMessage(name, message);
		receiveChatMessage();
		return true;
	}

	public String receiveChatMessage() {
		DatagramPacket d = getDatagramToReceive();
		do {
			try {
				m_socket.receive(d);
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		} while (OldMessageReceived(new String(d.getData())));
		return new String(d.getData());
	}

	private boolean OldMessageReceived(String message) {

		String arr[] = message.split("\\s+");
		String clientWhoSent = arr[0].trim();
		int packetID = Integer.parseInt(arr[1].trim());
		if (messageHandler.messageAlreadyReceived(clientWhoSent, packetID)) {
			return true;
		}
		messageHandler.markPacketAsReceived(clientWhoSent, packetID);
		return false;
	}

	public void sendChatMessage(String name, String message) {
		String compMessage = name + " " + messageID + " " + message;
		for (int i = 0; i < 10; ++i) {
			Random generator = new Random();
			double failure = generator.nextDouble();
			if (failure > TRANSMISSION_FAILURE_RATE) {
				try {
					m_socket.send(getDatagramToSend(compMessage));
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		}
		++messageID;
	}

	private DatagramPacket getDatagramToSend(String message) {
		String stringToSend = message;
		byte[] bytedString = stringToSend.getBytes();
		DatagramPacket packetToSend = new DatagramPacket(bytedString, bytedString.length, m_serverAddress,
				m_serverPort);
		return packetToSend;
	}

	private DatagramPacket getDatagramToReceive() {
		byte[] buffer = new byte[1000];
		DatagramPacket receiveablePacket = new DatagramPacket(buffer, buffer.length);
		return receiveablePacket;
	}
}
