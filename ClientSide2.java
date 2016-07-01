package trabalho;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;

import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;

public class ClientSide2 {

	private BufferedReader in;
	private PrintWriter out;
	private JFrame frame = new JFrame("Janela de interacao com o SQLITE");
	private JTextField dataField = new JTextField(50);
	private JTextArea messageArea = new JTextArea(75, 50);

	public ClientSide2() {

		//GUI
		messageArea.setEditable(false);
		frame.getContentPane().add(dataField, "North");
		frame.getContentPane().add(new JScrollPane(messageArea), "Center");

		// Aqui estao os Listeners do campo de insercao de produtos
		dataField.addActionListener(new ActionListener() {

			public void actionPerformed(ActionEvent e) {
				out.println(dataField.getText());
				String response;
				try {
					response = in.readLine();
					if (response == null || response.equals("")) {
						System.exit(0);
					}
				} catch (IOException ex) {
					response = "Error: " + ex;
				}
				String[] responseArray = response.split(";");
				for (int i = 0; i < responseArray.length; i++) {
					messageArea.append(responseArray[i] + "\n");
				}
				//messageArea.append(response + "\n");
				dataField.selectAll();
			}
		});
	}

	public void connectToServer() throws IOException {

		// JOption pane simples para receber o IP da maquina que estamos tentando conectar
		String serverAddress = JOptionPane.showInputDialog(frame, "Entre com o IP do servidor:",
				"Bem vindo ao SQLITE", JOptionPane.QUESTION_MESSAGE);
		
		// Criar conexao pela porta 9898 e inicializar streams de leitura e escrita
		Socket socket = new Socket(serverAddress, 9898);
		
		in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
		out = new PrintWriter(socket.getOutputStream(), true);

		// receber as mensagens iniciais do servidor
		for (int i = 0; i < 3; i++) {
			messageArea.append(in.readLine() + "\n");
		}
	}

	public static void main(String[] args) throws Exception {
		ClientSide2 client = new ClientSide2();
		client.frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		client.frame.pack();
		client.frame.setVisible(true);
		client.connectToServer();
	}
}