package servidorHTTP;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.PrintStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.util.Scanner;

public class Servidor implements Runnable{
	static ObjectOutputStream saida;
	static OutputStream saidaServidor;
	static int porta; 
	static String host; 
	static PrintStream mensagem;
	static Socket socket;
	static ServerSocket servidor;
	static BufferedWriter resposta;
	static Scanner scanner;
	static String caminho;
	static String navegador;
	static String url;
	static String door;
	public final static String versaoHTTP = "HTTP/1.1";
	
	private static final String saidaErro = "<html><head><title>"
			+ "Not Found"
			+ "</title></head><body><p>"
			+ "Resource Not Found!!"
			+ "</p></body></html>";
	private static final String saidaHeadersErro = "HTTP/1.1 "
			+ "404 Not Found\r\n" 
			+ "Content-Type: text/html\r\n"
			+ "Content-Length: ";
	private static final String saidaHeaders = "\r\n\r\n";
	
	Servidor(Socket socket){
		Servidor.socket = socket;
	}
	
	public static void main(String[] args) throws IOException, ClassNotFoundException {
		System.out.println("---------------------------SERVIDOR HTTP----------------------------\n"
				+"Aproveite! :)");
		Scanner url = new Scanner(System.in);
		StringBuffer aux = new StringBuffer(url.nextLine());
		aux.replace(0, aux.indexOf(" ") + 1, "");
		StringBuffer porta = new StringBuffer(aux.subSequence(aux.length() - 4, aux.length()));
		int portaHTTP = 8080, i;
		String endereco = aux.toString();
		 
		//Servidor.host = endereco;
		
		for(i = 0; i < porta.length() - 1; i++) {
			String portaAUX = porta.toString();
			if (Character.isDigit(porta.charAt(i))==true) {			
				portaHTTP = Integer.parseInt(portaAUX);
			}else {
				portaHTTP = 8080;
			}
		}
		url.close();
		endereco = endereco.replace(porta, "");
		Servidor.host = endereco;
		
		servidor = new ServerSocket(portaHTTP);
		System.out.println("O Servidor está conectado a porta "+ portaHTTP);
		  
		while (true) { 
			Socket socket = servidor.accept(); 
			new Thread(new Servidor(socket)).start(); 
		}		 
	}

	public void run() { 
		System.out.println("Conexão estabelecida!" + 
				"\n Servidor: " +
				socket.getInetAddress().getHostAddress()); 
		
		try { 
			resposta = new BufferedWriter( 
					new OutputStreamWriter( 
							new BufferedOutputStream(socket.getOutputStream()), "UTF-8")); 
			Scanner aux = new Scanner(socket.getInputStream());
			
			while (aux.hasNextLine()) {
				String requisicao = aux.nextLine();
				System.out.println(requisicao);
				String[] parts = requisicao.split(" ");
				if (parts.length == 3) {
					navegador = parts[0];
					url = parts[1];
					door = parts[2];
				} else if (parts.length == 2) {
					navegador = parts[0];
					url = parts[1];
					door = "8080";
				} else {
					System.out.println("Você digitou errado!");
				}
				break;
			}
			//aux.close();
			GET();
			resposta.flush();
			resposta.close();
			socket.close();
		}catch(Exception e) { 
			e.printStackTrace();
		} 
	}
	
	static public void GET() throws IOException, ClassNotFoundException{
		String nome = host + caminho;
		File arquivo = new File(nome);
		
		int i, j = 0;
		
		if(arquivo.isDirectory()) {
			OutputStream saida = socket.getOutputStream();
			String[] arquivos = arquivo.list();
			String mensagem = "Arquivos presentes no diretório local: ";
			saida.write(mensagem.getBytes(Charset.forName("UTF-8")));
			
			String quebraLinha = "\n";
			
			for (i = 0; i < arquivos.length; i++) {
				System.out.println(arquivos[i]);
				saida.write(quebraLinha.getBytes(Charset.forName("UTF-8")));
				saida.write(arquivos[i].getBytes(Charset.forName("UTF-8")));
				saida.write(quebraLinha.getBytes(Charset.forName("UTF-8")));
			}
			
			saida.flush();
		}else if(arquivo.exists()) {
			String tipoConteudo = Files.probeContentType(arquivo.toPath());
			System.out.println("Content Type: " + tipoConteudo);
			OutputStream saida = socket.getOutputStream();
			
			String envio = versaoHTTP + "200 OK \r\n Content-Type: " + tipoConteudo + "\r\n";
			saida.write(envio.getBytes(Charset.forName("UTF-8")));
			
			FileInputStream input = new FileInputStream(arquivo);
			BufferedInputStream buffer = new BufferedInputStream(input);
			
			saida.write(("Content-Length: " + String.valueOf(arquivo.length()) + "\r\n\r\n").getBytes());
			
			long tamanho = arquivo.length();
			byte[] bytes;
			
			while(tamanho > 0) {
				if(tamanho >= 1) {
					tamanho = tamanho - 1;
					j = 1;
				} else if (tamanho < 1) {
					j = (int) tamanho;
				}
				
				bytes = new byte[j];
				buffer.read(bytes, 0, j);
				saida.write(bytes);
			}
			
			saida.flush();
			input.close();
			buffer.close();
		}else {
			resposta.write(saidaHeadersErro + saidaErro.length() + saidaHeaders + saidaErro);
		}			
	}
}
