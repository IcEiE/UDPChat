package Commons;

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