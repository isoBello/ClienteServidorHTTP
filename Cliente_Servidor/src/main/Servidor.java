package main;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.Reader;
import java.io.StringReader;
import java.io.StringWriter;
import java.io.Writer;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Scanner;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.nio.file.Files;

public class Servidor implements Runnable {

	private final static Logger logger = Logger.getLogger(Servidor.class.toString());

	protected static final DateFormat HTTP_DATE_FORMAT = new SimpleDateFormat("EEE, dd MMM yyyy HH:mm:ss z");

	private String host;
	private int porta;
	private String metodo;
	private String uri;
	private String protocolo;
	private static String address;
	private static int door;
	private Socket socket;

	public Servidor(String host, int door) {
		super();
		this.host = host;
		this.porta = door;
	}

	Servidor(Socket servidor) {
		this.socket = servidor;
	}

	@SuppressWarnings("resource")
	public void serve() throws IOException {
		ServerSocket serverSocket = null;

		logger.info("Iniciando servidor no endereço: " + this.host + ":" + this.porta);

		try {
			// Cria a conexão servidora
			serverSocket = new ServerSocket(porta, 1, InetAddress.getByName(host));

		} catch (IOException e) {
			logger.log(Level.SEVERE, "Erro ao iniciar servidor!", e);
			return;
		}
		logger.info("Conexão com o servidor aberta no endereço: " + this.host + ":" + this.porta);

		// Fica esperando pela conexão cliente

		while (true) {
			logger.info("Aguardando conexões...");
			InputStream input = null;
			OutputStream output = null;

			try {
				socket = serverSocket.accept();
				new Thread(new Servidor(address, door)).start();

				input = socket.getInputStream();
				output = socket.getOutputStream();

				// Realiza o parse da requisição recebida
				String requestString = convertStreamToString(input);
				logger.info("Conexão recebida. Conteúdo:\n" + requestString);
				parse(requestString);

				// recupera a resposta de acordo com a requisicao

				// arquivos();
				String responseString = resposta();
				logger.info("Resposta enviada. Conteúdo:\n" + responseString);
				output.write(responseString.getBytes());

				// updateInfos();

				// Fecha a conexão
				socket.close();

			} catch (Exception e) {
				logger.log(Level.SEVERE, "Erro ao executar servidor!", e);
				continue;
			}
		}
	}

	private String convertStreamToString(InputStream is) {
		if (is != null) {
			Writer writer = new StringWriter();

			char[] buffer = new char[2048];
			try {
				Reader reader = new BufferedReader(new InputStreamReader(is));
				int i = reader.read(buffer);
				writer.write(buffer, 0, i);
			} catch (IOException e) {
				logger.log(Level.SEVERE, "Erro ao converter stream para string", e);
				return "";
			}
			return writer.toString();
		} else {
			return "";
		}
	}

	public void parse(String input) throws IOException {
		BufferedReader br = new BufferedReader(new StringReader(input));
		String line = null;
		int linha = 0;
		while ((line = br.readLine()) != null) {
			System.out.println(linha + " " + line);
			if (linha == 0) {
				String[] valor = line.split(" ");
				if (valor.length == 3) {
					metodo = valor[0];
					uri = valor[1];
					protocolo = valor[2];
				} // TODO Tratar erro
			} else {
				// TODO Recuperar os cabeçalhos e corpo
			}
			linha++;

		}
	}

	public String resposta() throws IOException, ClassNotFoundException {
		//updateInfos();
		String aux = uri.replace("/", "");
		
		String arquivo = address + aux;
		//System.out.println("Arquivo: " + arquivo);
		File file = new File(arquivo);

		StringBuilder sb = new StringBuilder();
		// Cria primeira linha do status, no caso sempre 200 OK
		sb.append("HTTP/1.1 200 OK").append("\r\n");

		// Cria os cabeçalhos
		sb.append("Date: ").append(HTTP_DATE_FORMAT.format(new Date())).append("\r\n");
		sb.append("Server: Test Server").append("\r\n");
		sb.append("Connection: Close").append("\r\n");
		sb.append("Content-Type: text/html; charset=UTF-8").append("\r\n");
		sb.append("\r\n");

		// Corpo
		sb.append("<html><head><title>Response</title></head><body><h1>HttpServer Response</h1>");
		sb.append("Method: ").append(metodo.concat("<br/>"));
		sb.append("URI: ").append(uri.concat("<br/>"));
		sb.append("Protocol: ").append(protocolo.concat("<br/>"));
		sb.append("</body></html>");
		sb.append("\r\n");

		if (file.isDirectory()) {
			OutputStream stream = socket.getOutputStream();
			String arquivos[] = file.list();

			for (int i = 0; i < arquivos.length; i++) {
				// System.out.println(arquivos[i]);
				sb.append("<br/>");
				sb.append("<a href="+ arquivos[i]+ ">" + arquivos[i] + "</a>".concat("<br/>"));
				sb.append("\r\n");
			}
			stream.flush();
		}else if (file.exists()) {
			String mime = Files.probeContentType(file.toPath());
			//System.out.println("Content Type: " + mime);
			OutputStream stream = socket.getOutputStream();
			
			String send = "HTTP/1.1 200 OK\r\nContent-Type: " + mime + "\r\n";
			sb.append(send);
			sb.append("\r\n");
			//stream.flush();

			FileInputStream f_aux = new FileInputStream(file);

			BufferedReader b_aux = new BufferedReader(new FileReader(file));
			
			sb.append(("Content-Length: " + String.valueOf(file.length()) + "\r\n\r\n"));
			sb.append("\r\n");
			//stream.flush();
			//sb.append(file);
			
			String linha = null;
			String ls = System.getProperty("line.separator");
			while ((linha = b_aux.readLine()) != null) {
				sb.append("<br/>");
				sb.append(linha);
				sb.append(ls);
			}
			// deleta a ultima linha do separador 
			sb.deleteCharAt(sb.length() - 1);
			b_aux.close();
			stream.flush();
			f_aux.close();
			b_aux.close();
		} else {
			String OUTPUT_NOT_FOUND = "<html><head><title>Not Found</title></head><body><p>Resource Not Found!!</p></body></html>";
			String OUTPUT_HEADERS_NOT_FOUND = "HTTP/1.1 404 Not Found\r\n" + "Content-Type: text/html\r\n"
					+ "Content-Length: ";
			String OUTPUT_END_OF_HEADERS = "\r\n\r\n";
			sb.append(OUTPUT_HEADERS_NOT_FOUND + OUTPUT_NOT_FOUND.length() + OUTPUT_END_OF_HEADERS + OUTPUT_NOT_FOUND);
		}

		return sb.toString();
	}

	public static void main(String[] args) throws IOException {
		Scanner scanner = new Scanner(System.in);
		int i, p_AUX = 8080;

		System.out.println("-------------------------------------SERVIDOR-----------------------------------");
		StringBuffer aux = new StringBuffer(scanner.nextLine());
		aux.replace(0, aux.indexOf(" ") + 1, "");
		StringBuffer d_AUX = new StringBuffer(aux.subSequence(aux.length() - 4, aux.length()));
		String endereco = aux.toString();

		for (i = 0; i < d_AUX.length() - 1; i++) {
			String portaAUX = d_AUX.toString();
			if (Character.isDigit(d_AUX.charAt(i)) == true) {
				p_AUX = Integer.parseInt(portaAUX);
				endereco = endereco.replace(d_AUX, "");
			} else {
				p_AUX = 8080;
			}
		}

		scanner.close();

		if (endereco.endsWith("/") == false) {
			endereco = endereco + "/";
			endereco = endereco.replace(" ", "");
			// System.out.println(endereco);
		}

		address = endereco;
		// door = p_AUX;

		if (endereco.startsWith("/home/")) {
			endereco = "localhost";
		}

		Servidor server = new Servidor(endereco, p_AUX);
		server.serve();
	}

	@Override
	public void run() {
		//TODO
	}
}
