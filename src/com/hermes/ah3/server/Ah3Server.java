package com.hermes.ah3.server;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * Ah3Driver ²âÊÔ·þÎñ¶Ë
 * @author wuwl
 *
 */
public class Ah3Server extends Thread {
	
	private static Log log = LogFactory.getLog(Ah3Server.class);

	private boolean iskeepAlive = true;
	
	public void setIskeepAlive(boolean iskeepAlive) {
		this.iskeepAlive = iskeepAlive;
	}
	
	public void run() {
		log.debug("Ah3Server Start...");
		try {
			ServerSocket serverSocket = new ServerSocket(12345);			
			while (this.iskeepAlive) {
				Socket socket = serverSocket.accept();
				Ah3ClientProcesser clientProcesser = new Ah3ClientProcesser(socket);
				clientProcesser.start();
			}			
		} catch (IOException e) {
			log.error("",e);
		}
		log.debug("Ah3Server Stop...");
	}
	
	public static void main(String[] args) {
		Ah3Server server = new Ah3Server();
		server.start();
	}
}
