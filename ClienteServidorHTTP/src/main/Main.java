package main;

import clienteHTTP.*;
import java.util.Scanner;

public class Main {

	public static void main(String[] args) {
		Scanner url = new Scanner(System.in);
		StringBuffer aux = new StringBuffer(url.nextLine());
		aux.replace(0, aux.indexOf(" ") + 1, "");
		StringBuffer porta = new StringBuffer(aux.subSequence(aux.length() - 2, aux.length()));
		
		for(int i = 0; i < porta.length() - 1; i++) {
			String endereco = aux.toString();
			String portaAUX = porta.toString();
			if (Character.isDigit(porta.charAt(i))==true) {			
				int portaHTTP = Integer.parseInt(portaAUX);
				new Cliente(endereco, portaHTTP);
			}else {
				new Cliente(endereco, 80);
			}
		}	
		url.close();
	}
}
