package it.uniroma2.sii.service.tor.webproxy.server;

import it.uniroma2.sii.service.tor.webproxy.server.ProxyConnectionHandler.ProtocolType;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.Socket;

import org.springframework.stereotype.Service;

@Service
public class HTTPProxyServer extends Thread {

	//FIXME
	@Override
	public void run() {
		try {
			ServerSocket serverSocket = new ServerSocket(4567);
			System.out.println("HTTPProxy Started...");
			while (true) {
				try {
					final Socket clientSocket = serverSocket.accept();
					System.out.println("Accepted...");
					ProxyConnectionHandler proxyConnectionHandler = new ProxyConnectionHandler(
							this, clientSocket);
					
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	//FIXME
	public ProtocolType checkProtocolTypeByPort(int port) {
		// TODO Auto-generated method stub
		return ProtocolType.HTTP;
	}

	//FIXME
	public InetSocketAddress getTorSocketAddress() {
		// TODO Auto-generated method stub
		return InetSocketAddress.createUnresolved("127.0.0.1", 9051);
	}
}
