/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package UDPChat.Server;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.util.Random;

/**
 * 
 * @author brom
 */
public class ClientConnection {
	
	static double TRANSMISSION_FAILURE_RATE = 0
			;
	
	private final String m_name;
	private final InetAddress m_address;
	private final int m_port;

	public ClientConnection(String name, InetAddress address, int port) {
		m_name = name;
		m_address = address;
		m_port = port;
	}

	public void sendMessage(String message, DatagramSocket socket) {
		
		Random generator = new Random();
    	double failure = generator.nextDouble();
    	
    	if (failure > TRANSMISSION_FAILURE_RATE){
    		try {
				socket.send(getDatagramToSend(message));
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
		DatagramPacket packetToSend = new DatagramPacket(bytedString, bytedString.length, m_address, m_port);
		return packetToSend;
	}
	
	public boolean hasName(String testName) {
		return testName.equals(m_name);
	}

}
