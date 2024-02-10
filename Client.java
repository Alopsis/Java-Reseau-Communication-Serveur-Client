
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.Socket;
import java.net.UnknownHostException;
import java.nio.ByteBuffer;
import java.text.SimpleDateFormat;
import java.util.Scanner;
import java.util.StringTokenizer;

public class Client {

	private static final String CHEMIN_CLIENT = "./tp1/FichiersClient/";
	private String hote;
	private int port;
	private Socket cliSocket;
	private DataInputStream dIn;
	private DataOutputStream dOut;

	public Client(String h, int p) {
		hote = h;
		port = p;
	}

	public void initierConnexion() {
		try {
			cliSocket = new Socket(hote, port);
			System.out.println("C : connecté au serveur");

			dIn = new DataInputStream(cliSocket.getInputStream());
			dOut = new DataOutputStream(cliSocket.getOutputStream());
			System.out.println("C : flux binaires ouverts");
		} catch (UnknownHostException e) {
			System.err.println("C : erreur de connection au serveur, hôte inconnu");
			e.printStackTrace();
		} catch (IOException e) {
			System.err.println("C : erreur d'ouverture des flux");
			e.printStackTrace();
		}
	}

	public void seDeconnecter() {
		try {
			dIn.close();
			dOut.close();
			cliSocket.close();
			System.out.println("C : déconnexion du serveur");
		} catch (IOException e) {
			System.err.println("C : erreur lors de la déconnexion");
			e.printStackTrace();
		}

	}

	public void attendreConfirmationConnexion() {
		try {

			String message = "";
			while (message.equals("")) {
				message = dIn.readUTF();
				System.out.println(message);
			}

		} catch (Exception e) {

		}
	}

	public synchronized void envoyerDemande() {
		while(true){
			Scanner sc = new Scanner(System.in);
			String reponse = sc.nextLine();
			StringTokenizer string = new StringTokenizer(reponse, " ");
	
			String dem = string.nextToken();
	
			System.out.println("C : Vous voulez envoyer " + dem);
			envoyerViaSocket(dem);
		}

	}


	public String envoyerViaSocket(String message) {
		try {

			dOut.writeUTF(message);

			String mes = "";

			while (mes.equals("")) {
				mes = dIn.readUTF();
				System.out.println(mes);

			}
			return mes;
		} catch (Exception e) {

		}
		return "";

	}

	public static void main(String[] args) {
		Client client = new Client("127.0.0.1", 2121);
		client.initierConnexion();
		client.attendreConfirmationConnexion();
		client.envoyerDemande();
		
	}

}
