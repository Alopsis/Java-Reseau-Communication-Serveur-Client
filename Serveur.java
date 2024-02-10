import java.awt.event.KeyEvent;
import java.awt.KeyEventDispatcher;
import java.awt.KeyboardFocusManager;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;
import javax.swing.JFrame;

public class Serveur implements KeyEventDispatcher {

	private static final int PORT = 2121;
	private ServerSocket servSocket;
	private List<Socket> listClientSocket = new ArrayList<>();
	private List<DataInputStream> listdIn = new ArrayList<>();
	private List<DataOutputStream> listdOut = new ArrayList<>();
	private volatile boolean fin = false;

	public Serveur() {
		KeyboardFocusManager.getCurrentKeyboardFocusManager().addKeyEventDispatcher(this);
		try {
			servSocket = new ServerSocket(PORT);
			System.out.println("S : serveur actif");
		} catch (IOException e) {
			System.err.println("S : erreur d'instanciation de la socket du serveur");
			e.printStackTrace();
		}
	}

	public void ecouter() {
		System.out.println("On attend le client");
		try {
			while (!fin) {
				Socket socketAttente = servSocket.accept();
				if (socketAttente != null) {
					System.out.println("Nouveau client connecté: " + socketAttente.getInetAddress().getHostAddress());
					listClientSocket.add(socketAttente);
					// ouvrir le flux pour le socketATtente
					DataOutputStream dOut = ouvrirFlux(socketAttente);
					// envoyer message de confirm
					if(dOut != null){
						envoyerConfirm(dOut);

					}
				}
			}
		} catch (IOException e) {
			// Gérer l'exception
			if (!fin) {
				e.printStackTrace();
			}
		}
		System.out.println("Fin");
	}

	public void ouvrirFlux() {
		try {
			for (Socket clientSocket : listClientSocket) {
				listdIn.add(new DataInputStream(clientSocket.getInputStream()));
				listdOut.add(new DataOutputStream(clientSocket.getOutputStream()));
			}
			System.out.println("S : Tous les flux ont été instanciés");
		} catch (IOException e) {
			System.err.println("S : erreur d'ouverture des flux");
			e.printStackTrace();
		}
	}

	public DataOutputStream ouvrirFlux(Socket socketAttente) {
		try {
			DataOutputStream dOut;

			dOut = new DataOutputStream(socketAttente.getOutputStream());
			listdIn.add(new DataInputStream(socketAttente.getInputStream()));
			listdOut.add(dOut);
			return dOut;

		} catch (IOException e) {
			e.printStackTrace();
		}
		return null;
	}

	private void envoyerConfirm() {
		for (DataOutputStream out : listdOut) {
			try {
				out.writeUTF("Confirm");
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}
	private void envoyerConfirm(DataOutputStream out) {
			try {
				out.writeUTF("Confirm");
			} catch (IOException e) {
				e.printStackTrace();
			}
	}
	public void deconnecterClient() {
		try {
			for (int i = 0; i < listdIn.size(); i++) {
				listdIn.get(i).close();
				listdOut.get(i).close();
				listClientSocket.get(i).close();
				System.out.println("S : client déconnecté par le serveur");
			}
		} catch (IOException e) {
			System.err.println("S : erreur lors de la déconnexion du client");
			e.printStackTrace();
		}
	}

	public void arreter() {
		fin = true;
		try {
			servSocket.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	@Override
	public boolean dispatchKeyEvent(KeyEvent e) {
		if (e.getID() == KeyEvent.KEY_PRESSED) {
			this.arreter();
		}
		return false;
	}

	public synchronized void attendreMessage() {
		while (true) {
			for (int i = 0; i < listdIn.size(); i++) {
				String message = "";
				try {
					message = listdIn.get(i).readUTF();
				} catch (IOException e) {
					e.printStackTrace();
				}
				if (!message.isEmpty()) {
					System.out.println("Demande recu " + message);
					try {
						listdOut.get(i).writeUTF("S : Demande recu " + message);
					} catch (IOException e) {
						e.printStackTrace();
					}
					break;
				}
			}
		}
	}

	public static void main(String[] args) {
		JFrame frame = new JFrame("Test");
		frame.setTitle("Test collision");
		frame.setSize(50, 50);
		frame.setLayout(null);
		frame.setLocationRelativeTo(null);
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		frame.setVisible(true);

		Serveur serv = new Serveur();
		serv.ecouter();
		// serv.ouvrirFlux();
		// serv.envoyerConfirm();
		new Thread(() -> serv.attendreMessage()).start();
	}
}
