package clienteHTTP;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.net.UnknownHostException;

/*Cliente HTTP simples, apenas com requisição GET*/

public class Cliente {
	public final static String versaoHTTP = "HTTP/1.1"; //Versão HTTP que aparecerá como a padrão
	private String host;
	private int porta;
	
	/*Construtor da classe*/
	
	public Cliente(String host, int porta) {
		super();
		this.host = host;
		this.porta = porta;
	}
	
	/*Método que realiza a requisição HTTP e devolve uma resposta */
	public String getRequisicaoHTTP(String caminho) throws UnknownHostException, IOException{
		Socket socket = null;
		try {
			/*Abre a conexão HTTP*/
			socket = new Socket(this.host, this.porta);
			PrintWriter saida = new PrintWriter (socket.getOutputStream(), true);
			BufferedReader entrada = new BufferedReader(new InputStreamReader(socket.getInputStream()));
			/*Envia a requisição*/
			saida.println("GET" + caminho + " " + versaoHTTP);
			saida.println("Host: "+ this.host);
			saida.println("Connection: Close");
			saida.println();
			boolean loop = true;
			StringBuffer aux = new StringBuffer();
			/*Agora, se a resposta estiver disponível, será recuperada*/
			while(loop) {
				if(entrada.ready()) {
					int i = 0;
					while((i = entrada.read()) != -1) {
						aux.append((char) i);
					}
					loop = false;
				}
			}
			return aux.toString();
		} finally {
			if(socket != null) {
				socket.close();
			}
		}	
	}
}
