package Commons;
/*
 * Class used to control packets received by a server or a client. It is used to determining if a packet has been taken before or not, to not work on a identical packet.
 * This is done with a help of a hashmap name = key, contain each PacketID from that client.
 */

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class PacketHandler {
	private Map<String, ArrayList<Integer>> m_receivedPackets = new HashMap<>();

	public void markPacketAsReceived(String name, int packetID) {
		if (m_receivedPackets.containsKey(name)) {
			addMessage(name, packetID);
		} else {
			addClient(name);
			addMessage(name, packetID);
		}
	}

	private void addMessage(String name, int packetID) {
			m_receivedPackets.get(name).add(packetID);
	}

	private void addClient(String name) {
			m_receivedPackets.put(name, new ArrayList<>());
	}

	public boolean isANewMessage(String name, int packetID) {
		if (m_receivedPackets.containsKey(name) && m_receivedPackets.get(name).contains(packetID)) {
			return false;
		}
		return true;
	}

	public void removeClient(String name) {
		m_receivedPackets.remove(name);
	}
}