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
		String message = "/connect";
		sendChatMessage(name, message);
		System.out.println(receiveChatMessage());
		return true;
	}

	public String receiveChatMessage() {
		DatagramPacket d = getDatagramToReceive();
		try {
			m_socket.receive(d);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return new String(d.getData());
	}

	public void sendChatMessage(String name, String message) {
		Random generator = new Random();
		double failure = generator.nextDouble();
		String compMessage = name + " " + message;
		if (failure > TRANSMISSION_FAILURE_RATE) {
			try {
				m_socket.send(getDatagramToSend(compMessage));
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		} else {
			// Message got lost
		}
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
