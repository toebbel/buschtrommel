package de.tr0llhoehle.buschtrommel.test.mockups;

import java.io.IOException;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.Socket;

public class NetworkMock extends Thread{
	ServerSocket s;
	Socket so;
	int port;
	
	
	public NetworkMock(int port) {
		this.port = port;
		start();
		try {
			Thread.sleep(100);
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	@Override
	public void run() {
		try {
			s = new ServerSocket();
			s.bind(new InetSocketAddress("localhost", port));
			so = s.accept();
			System.out.println("Network Mock accepted connection!");
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	
	public void send(byte[] data) throws IOException {
		so.getOutputStream().write(data);
		so.getOutputStream().flush();
	}
	
	public void receive(byte[] buffer) throws IOException {
		so.getInputStream().read(buffer);
	}
	
	public void close() throws IOException{
		if(s != null)
			s.close();
		if(so != null)
			so.close();
	}
	
	public InetAddress getAddr() {
		return ((InetSocketAddress)s.getLocalSocketAddress()).getAddress();
	}
	
	public int getPort() {
		return ((InetSocketAddress)s.getLocalSocketAddress()).getPort();
	}
}
