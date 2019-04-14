package main;

import java.io.File;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Scanner;
import java.net.HttpURLConnection;
import java.net.URL;


public class Cliente {
	static String host;
	static int porta;
	static String path;
	static String u_AUX;
	static String arquivoDownload;
	static String address;
	private static FileWriter arquivo;
	private static Socket socket;
	static String requisicao;
	
	private static final int BUFFER_SIZE = 4096;

	public static void main(String[] args) throws UnknownHostException, IOException {
		Scanner scanner = new Scanner(System.in);
		String[] dados;
		int i;
		
		System.out.println("---------------------------CLIENTE----------------------------\n"
				+"Lembre-se: Para páginas que serão acessadas como caminho, a URL deve conter '/' ao final\n"
				+"Aproveite! :)");
		StringBuffer aux = new StringBuffer(scanner.nextLine());
		aux.replace(0, aux.indexOf(" ") + 1, "");
		StringBuffer door = new StringBuffer(aux.subSequence(aux.length() - 2, aux.length()));
		String endereco = aux.toString();
		
		for(i = 0; i < door.length() - 1; i++) {
			String portaAUX = door.toString();
			if (Character.isDigit(door.charAt(i)) == true) {			
				Cliente.porta = Integer.parseInt(portaAUX);
				endereco = endereco.replace(door, "");
			}else {
				Cliente.porta = 80;
			}
		}
		
		scanner.close();	
		u_AUX = endereco;
		endereco = endereco.replace("https://", "").replace("http://", "");
		dados = endereco.split("/");
		String caminho = "";
		String arquivoAUX = null;
		
		for (i = dados.length - 1; i > 0; i--) {
			caminho = dados[i].concat("/" + caminho);
		}
		
		caminho = "/" + caminho;		 
		caminho = caminho.substring(0, caminho.length() - 1);
		arquivoAUX = (caminho.substring(caminho.lastIndexOf("/") + 1));
		
		if (arquivoAUX.length() == 0) {
			arquivoAUX = "index.html";
		}
		
		host = dados[0];
		path = caminho;
		arquivoDownload = arquivoAUX;
		address = endereco;
		
		socket = new Socket(host, porta);
		iniciaConexao();
	}
	
	public static void iniciaConexao() throws UnknownHostException, IOException {
		String requisicao = "";
		Scanner scanner;
		
		int inicio = 0, i;
		String last = "\r\n\r\n";	

		if(socket.isConnected()) {
			
			System.out.println("Conectado a " + socket.getInetAddress());			
			
			requisicao = "GET " + path + " HTTP/1.1\r\n" + "Host: " + host + "\r\n" +
					"Accept-Encoding: deflate" + "\r\n" +
					"Connection: keep-alive" + "\r\n" + 
					"Upgrade-Insecure-Requests: 1" + "\r\n" + 
					"User-Agent: isolda/1.0 (X11; Linux x86_64)" + "\r\n" + 
					"Accept: text/html" + "\r\n" + 
					"Accept-Language: en-US,en;q=0.9,pt-BR;q=0.8,pt;q=0.7" + 
					"\r\n\r\n";
			
			OutputStream saida = socket.getOutputStream();
			byte[] bytes = requisicao.getBytes();
			saida.write(bytes);
			saida.flush();
			
			if(arquivoDownload.equals("index.html") || arquivoDownload.contains("html") || arquivoDownload.contains("htm")) {
				scanner = new Scanner(socket.getInputStream());
				arquivo = new FileWriter(new File(arquivoDownload));
				
				ArrayList<String> conteudo = new ArrayList<>();

				while (scanner.hasNext()) {
					String linha = scanner.nextLine();
					System.out.println(linha);
					conteudo.add(linha);
				}

				for (i = 0; i < conteudo.size(); i++) {
					if (conteudo.get(i).contains(last)) {
						inicio = i + 4;
						break;

					} else if(conteudo.get(i).contains("Content-Type")) {
						inicio = i + 2;
						break;
					} else if(conteudo.get(i).contains("</br></br>")) {
						inicio = i + 4;
					}
				}

				for (i = inicio; i < conteudo.size(); i++) {
					arquivo.write(conteudo.get(i));
					arquivo.write("\n");
				}

				arquivo.flush();
				arquivo.close();
			} else {
				download();
			}
		}	
	}
	
	@SuppressWarnings("static-access")
	static void download() throws IOException {
		URL url = new URL(u_AUX);
		HttpURLConnection http = (HttpURLConnection) url.openConnection();
		
		int codigo = http.getResponseCode();
		//System.out.println(arquivoDownload);
		
		if(codigo == HttpURLConnection.HTTP_OK) {
			String nome = arquivoDownload;
			String disposition = http.getHeaderField("Content-Disposition");
			String contentType = http.getContentType();
			int contentLength = http.getContentLength();
			
			System.out.println("Content-Type = " + contentType);
			System.out.println("Content-Disposition = " + disposition);
			System.out.println("Content-Length = " + contentLength);
			System.out.println("fileName = " + arquivoDownload);
			
			InputStream entrada = http.getInputStream();
			String diretorio = "src/";
			
			String caminho = diretorio + File.separator + nome;
			
			// opens an output stream to save into file
			FileOutputStream saida = new FileOutputStream(caminho);

			int bytesRead = -1;
			byte[] buffer = new byte[BUFFER_SIZE];
			while ((bytesRead = entrada.read(buffer)) != -1) {
				saida.write(buffer, 0, bytesRead);
			}

			saida.close();
			entrada.close();

			System.out.println("File downloaded");
		} else {
			System.out.println("No file to download. Server replied HTTP code: " + codigo);
		}
		http.disconnect();
	}
	
	static void writeByte(byte []data) { 
        try { 
  
            // Initialize a pointer 
            // in file using OutputStream
        	
        	FileOutputStream writer = new FileOutputStream(arquivoDownload);
            writer.write(data);
            System.out.println("Successfully"
                               + " byte inserted"); 
            // Close the file 
            writer.close(); 
        } 
  
        catch (Exception e) { 
            System.out.println("Exception: " + e); 
        } 
    } 
}

