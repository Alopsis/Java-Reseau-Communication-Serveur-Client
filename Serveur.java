
import java.awt.event.KeyEvent;
import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.awt.event.ActionEvent;
import java.util.StringTokenizer;
import java.awt.event.KeyListener;
import java.awt.event.ActionListener;

import javax.swing.JFrame;
import javax.swing.Timer;

import java.awt.*;

public class Serveur implements KeyEventDispatcher {

	private static final int PORT = 2121;
	private ServerSocket servSocket;
	List<Socket> listClientSocket = new ArrayList();
	List<DataInputStream> listdIn = new ArrayList();
	List<DataOutputStream> listdOut = new ArrayList();
	private boolean fin = false;
	Thread rechercheServeur = new Thread();
	// Instanti le serveur (il est ouvert)
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

	// phase ou on attend les joueurs
	public void ecouter() {

		System.out.println("On attend le client");
		Socket socketAttente = null;
		while (fin == false) {
			try {
				socketAttente = servSocket.accept();
				if (socketAttente != null) {
					listClientSocket.add(socketAttente);
					socketAttente = null;
				}
			} catch (Exception e) {

			}

		}
		System.out.println("Fin");
	}

	public void ouvrirFlux() {
		try {
			for (int i = 0; i < listClientSocket.size(); i++) {
				listdIn.add(new DataInputStream(listClientSocket.get(i).getInputStream()));
				listdOut.add(new DataOutputStream(listClientSocket.get(i).getOutputStream()));

			}

			System.out.println("S : Tous les flux ont été instanciés");
		} catch (IOException e) {
			System.err.println("S : erreur d'ouverture des flux");
			e.printStackTrace();
		}
	}

	private void envoyerConfirm() {
		for (int i = 0; i < listdOut.size(); i++) {
			try {
				listdOut.get(i).writeUTF("Confirm");

			} catch (IOException e) {
				e.printStackTrace();
			}
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


	// comme keylistener
    public boolean dispatchKeyEvent(KeyEvent e) {
        if (e.getID() == KeyEvent.KEY_PRESSED) {
            System.out.println("Key pressed: " + KeyEvent.getKeyText(e.getKeyCode()));
			fin = true;
        }
        return false;
    }


	public synchronized void attendreMessage() {
		while(true){

		for (int i = 0; i < listdIn.size(); i++) {
			String message = "";
			try {

				message = listdIn.get(i).readUTF();
			} catch (IOException e) {
				e.printStackTrace();
			}
			if (message != "") {
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
        frame.setSize(50,50);
        frame.setLayout(null);
        frame.setLocationRelativeTo(null);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setVisible(true);
		Serveur serv = new Serveur();
		
		serv.ecouter();
		serv.ouvrirFlux();
		serv.envoyerConfirm();
		// Timer timerReceptionMessage = new Timer(500, new ActionListener() {
		// 	@Override
		// 	public void actionPerformed(ActionEvent e) {
		// 		serv.attendreMessage();

		// 	}
		// });
		// timerReceptionMessage.start();
		new Thread(() -> {
			serv.attendreMessage();
        }).start();    

	}

}