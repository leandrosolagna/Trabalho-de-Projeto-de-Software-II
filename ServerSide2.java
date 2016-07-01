package trabalho;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

/*
 * o server recebe mensagens dos clientes para inserir no banco de dados SQLITE
// * quando o cliente conecta, uma nova thread eh iniciada para lidar com o cliente interativamente e tambam
 * para que varios clientes possam se conectar ao mesmo tempo
 * o cliente envia uma string com o produto e o preço separado por ponto e virgula ';'
 * o servidor ainda responde a uma mensagem especifica "show" devolvendo todo o conteudo da tabela PRODUTOS
 * vale ainda comentar que o servidor fica em loop infinito, portanto precisa ser terminado manualmente de 
 * preferencia no botaozinho vermelho do console do eclipse
 * 
// * o servidor aceita 3 comandos: 
 * "sair" 
 * "show"
 * "produto ; preco"
 */

public class ServerSide2 {

	private static int contagemGlobalParaID = 1;
	
	/*
	 *  o servidor escuta na porta 9898
	 */
	
	public static void main(String[] args) throws Exception {

        criarDB();
        contaProdutos();
    	
        System.out.println("Servidor online.");
        int clientNumber = 0;
        ServerSocket listener = new ServerSocket(9898);
        try {
            while (true) {
                new ThreadInteracaoComClient(listener.accept(), clientNumber++).start();
            }
        } finally {
            listener.close();
        }
        
    }

    private static class ThreadInteracaoComClient extends Thread {
        private Socket socket;
        private int clientNumber;

        public ThreadInteracaoComClient(Socket socket, int clientNumber) {
            this.socket = socket;
            this.clientNumber = clientNumber;
            log("Nova conexao com o cliente# " + clientNumber + " no socket " + socket);
        }

        /*
         * envia uma mensagem de boas vindas ao cliente e entra em loop de leitura e envio de feedback
         */
        public void run() {
            try {

				// cria streams de leitura e escrita
                BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream())); //buffer!
                PrintWriter out = new PrintWriter(socket.getOutputStream(), true); //pega do buffer os caracteres

                // Send a welcome message to the client.
                out.println("Ola, cliente#" + clientNumber + ".");
                out.println("Se desejar sair, digite \"sair\" na caixa de texto\n");

                // Get messages from the client, line by line; return them
                // capitalized
                while (true) {
                    String input = in.readLine();
                    if (input.equals("sair")) {
                        break;
                    }
                    if (input.equals("show")){
                    	out.println( "" + select() );
                    }else{
	                    String[] inputArray = input.split(";");
	                    inserir(inputArray[0], inputArray[1]);
	                    out.println("produto "+inputArray[0]+" de preco "+inputArray[1]+" inserido!");
	                    
                    }
                }
            } catch (IOException e) {
                log("Erro ao lidar com o client# " + clientNumber + ": " + e);
            } finally {
                try {
                    socket.close();
                } catch (IOException e) {
                    log("Erro, nao pode fechar conexao");
                }
                log("Conexao com client# " + clientNumber + " fechada");
            }
        }

        private void log(String message) {
            System.out.println(message);
        }
    }
    
    /*
     * os 3 metodos abaixo serao referentes ao banco de dados SQLITE, cujo foi inserido no build path
     *  
     */
    
	private static void criarDB() throws SQLException{
		Connection c = null;
		Statement stmt = null;
		try {
			Class.forName("org.sqlite.JDBC");
			c = DriverManager.getConnection("jdbc:sqlite:test.db");

			stmt = c.createStatement();
			String sql = "CREATE TABLE PRODUTOS " + "(ID 	INT PRIMARY KEY     NOT NULL,"
					+ " NOME TEXT NOT NULL, " + " PRECO REAL)";

			stmt.executeUpdate(sql);
			stmt.close();
			c.close();
		} catch (Exception e) {
			System.out.println("Banco de dados ja existe");
			//System.err.println(e.getClass().getName() + ": " + e.getMessage());
			//System.exit(0);
		}
	}
	
	private static void contaProdutos() {
		Connection c = null;
		Statement stmt = null;
		
		try {
			Class.forName("org.sqlite.JDBC");

			c = DriverManager.getConnection("jdbc:sqlite:test.db");

			stmt = c.createStatement();
			ResultSet rs = stmt.executeQuery( "SELECT COUNT(*) AS total FROM PRODUTOS;" );
			contagemGlobalParaID = rs.getInt("total") + 1;

			rs.close();
			stmt.close();
			c.close();
		} catch (Exception e) {
			//System.err.println(e.getClass().getName() + ": " + e.getMessage());
			//System.exit(0);
		}
	}
	
	
	
	private static void inserir(String nomeProduto,String preco) {
		Connection c = null;
		Statement stmt = null;
		
		nomeProduto = nomeProduto.trim();
		preco = preco.trim();
		preco = preco.replaceAll(",", ".");
		try {
			Class.forName("org.sqlite.JDBC");

			c = DriverManager.getConnection("jdbc:sqlite:test.db");
			c.setAutoCommit(false); //como ta falso, o commit que vai inserir tudo de uma vez no banco. Se nao, cada insercao seria tratada individualmente

			stmt = c.createStatement();
			String sql = "INSERT INTO PRODUTOS (ID,NOME,PRECO) " + "VALUES ("+contagemGlobalParaID+", '"+nomeProduto+"', "+preco+" );";
			contagemGlobalParaID++;
			stmt.executeUpdate(sql);
			
			stmt.close();
			c.commit();
			c.close();
		} catch (Exception e) {
			//System.err.println(e.getClass().getName() + ": " + e.getMessage());
			//System.exit(0);
		}
	}
	
	private static String select(){
		Connection c = null;
	    Statement stmt = null;
	    String output = "";
	    try {
	      Class.forName("org.sqlite.JDBC");
	      c = DriverManager.getConnection("jdbc:sqlite:test.db");
	      c.setAutoCommit(false);
	      
	      stmt = c.createStatement();
	      ResultSet rs = stmt.executeQuery( "SELECT * FROM PRODUTOS;" );
	      while ( rs.next() ) {
	         int id = rs.getInt("id");
	         String  nome = rs.getString("nome");
	         float preco = rs.getFloat("preco");
	         output += "ID = "+ id+";" + "NOME = "+ nome+";" + "PRECO = "+ preco+";" + " ;";
	      }
	      rs.close();
	      stmt.close();
	      c.close();
	    } catch ( Exception e ) {
	      System.err.println( e.getClass().getName() + ": " + e.getMessage() );
	      System.exit(0);
	    }
	    return output;
	}
}