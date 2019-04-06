package clienteHTTP;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.OutputStream;
//import java.io.PrintWriter;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Scanner;

/*Cliente HTTP simples, apenas com requisição GET*/

public class Cliente {
	public final static String versaoHTTP = "HTTP/1.1"; //Versão HTTP que aparecerá como a padrão
	/*
	 * private String host; private int porta; private static Scanner aux;
	 */
	private static String host;
	private static int porta;
	private static Socket socket;
	static String requisicao;
	
	/*Construtor da classe*/
	
	/*
	 * public Cliente(String host, int porta) { 
	 * 		super(); this.host = host;
	 * 		this.porta = porta; 
	 * }
	 */
	
	public static void main(String[] args) throws IOException{
		System.out.println("---------------------------CLIENTE HTTP----------------------------\n"
				+"Lembre-se: Para páginas que serão acessadas como caminho, a URL deve conter '/' ao final\n"
				+"Aproveite! :)");
		Scanner url = new Scanner(System.in);
		StringBuffer aux = new StringBuffer(url.nextLine());
		aux.replace(0, aux.indexOf(" ") + 1, "");
		StringBuffer porta = new StringBuffer(aux.subSequence(aux.length() - 2, aux.length()));
		int portaHTTP = 80, i;
		String endereco = aux.toString();
		
		for(i = 0; i < porta.length() - 1; i++) {
			String portaAUX = porta.toString();
			if (Character.isDigit(porta.charAt(i))==true) {			
				portaHTTP = Integer.parseInt(portaAUX);
				//new Cliente(endereco, portaHTTP);
			}else {
				portaHTTP = 80;
			}
		}	
		url.close();
		//System.out.println(endereco);
		String novoHost = endereco.replace("http://", "").replace("https://", "").replace(porta.toString(), "");
		//System.out.println(novoHost);
		String[] dadosCliente = novoHost.split("/");
		String arquivoDownload = null;
		
		if(dadosCliente.length == 1) {
			arquivoDownload = "index";
		}else if (novoHost.subSequence(novoHost.length() - 2, novoHost.length()- 1).equals("/")){
			arquivoDownload = dadosCliente[dadosCliente.length - 2];
		}else {
			arquivoDownload = dadosCliente[dadosCliente.length - 1];
		}
		Cliente.host = dadosCliente[0];
		Cliente.porta = portaHTTP;
		getRequisicaoHTTP(arquivoDownload);
	}
	
	/*Método que realiza a requisição HTTP e devolve uma resposta */
	public static void getRequisicaoHTTP(String arquivo) throws UnknownHostException, IOException{
		int inicio = 0, i;
		
		try {
			/*Abre a conexão HTTP*/
			socket = new Socket(host, porta);
			//PrintWriter saida = new PrintWriter (socket.getOutputStream(), true);
			String requisicao;
			Scanner entrada = new Scanner(socket.getInputStream());

			if(socket.isConnected()) {
				System.out.println("Conexão estabelecida com o servidor "+socket.getInetAddress());

				if(host.subSequence(host.length() - 2, host.length()- 1).equals("/")) {
					/*Envia a requisição*/
					requisicao = "GET " + "/" + versaoHTTP+"\r\n" + "Host: "+ host + "\r\n" + "\r\n"; 
					
					/*
					 * saida.println("GET" + dados + " " + versaoHTTP); saida.println("\r\nHost: "+
					 * dados[0]); saida.println("\r\nConnection: Close"); saida.println("\r\n");
					 */
					 
					arquivo = arquivo + ".html";
				}else {
					/*Envia a requisição*/
					requisicao = "GET " + "/" + versaoHTTP + "\r\n" + "Host: "+ host + "\r\n" + "\r\n";
					/*
					 * saida.println("GET" + dados + " " + versaoHTTP); saida.println("\r\nHost: "+
					 * dados[0]); saida.println("\r\nConnection: Close"); saida.println("\r\n");
					 */
				}
				
				OutputStream resposta = socket.getOutputStream();
				byte[] bytes = requisicao.getBytes();
				
				resposta.write(bytes);
				resposta.flush();
				
				FileWriter arquivoSaida = new FileWriter(new File(arquivo));
				ArrayList<String> conteudo = new ArrayList<>();
				
				while (entrada.hasNext()) {
					String linha = entrada.nextLine();
					System.out.println(linha);
					conteudo.add(linha);
				}
				
				for(i = 0; i < conteudo.size(); i++) {
					if(conteudo.get(i).contains("Content-Type")) {
						inicio = i + 2;
						break;
					}
				}
				
				for(i = inicio; i < conteudo.size(); i++) {
					arquivoSaida.write(conteudo.get(i));
					arquivoSaida.write("\n");
				}
				arquivoSaida.flush();
				arquivoSaida.close();
				entrada.close();
			}
		} finally {
			if(socket != null) {
				socket.close();
			}
		}	
	}
}
