package UDPChat.Client;

import java.awt.event.*;

public class Client implements ActionListener {

	private String m_name = null;
	private final ChatGUI m_GUI;
	private ServerConnection m_connection = null;

	public static void main(String[] args) {
		if (args.length < 3) {
			System.err.println("Usage: java Client serverhostname serverportnumber username");
			System.exit(-1);
		}

		try {
			Client instance = new Client(args[2]);
			instance.connectToServer(args[0], Integer.parseInt(args[1]));
		} catch (NumberFormatException e) {
			System.err.println("Error: port number must be an integer.");
			System.exit(-1);
		}
	}

	private Client(String userName) {
		m_name = userName;

		// Start up GUI (runs in its own thread)
		m_GUI = new ChatGUI(this, m_name);
	}

	private void connectToServer(String hostName, int port) {
		// Create a new server connection
		m_connection = new ServerConnection(hostName, port);
		if (m_connection.handshake(m_name)) {
			m_GUI.displayMessage("You are now connected");
			listenForServerMessages();
		} else {
			System.err.println("Unable to connect to server");
		}
	}

	private void listenForServerMessages() {
		do {
			m_GUI.displayMessage(m_connection.receiveChatMessage());
		} while (true);
	}

	@Override
	public void actionPerformed(ActionEvent e) {
		m_connection.sendChatMessage(m_name, m_GUI.getInput());
		m_GUI.clearInput();
	}
}
