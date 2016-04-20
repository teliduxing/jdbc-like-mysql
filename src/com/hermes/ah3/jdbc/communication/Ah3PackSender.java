/**
 * 
 */
package com.hermes.ah3.jdbc.communication;

import java.io.BufferedOutputStream;
import java.net.Socket;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * @author wuwl
 *
 */
public class Ah3PackSender {

private static Log log = LogFactory.getLog(Ah3PackSender.class);
	
	private Socket socket;
	
	public Ah3PackSender(Socket socket) {
		this.socket = socket;		
	}
	
	public void sendDataPackToServer(Ah3DataPack dataPack) throws Exception {
		BufferedOutputStream bos = new BufferedOutputStream(this.socket.getOutputStream());
		log.debug("\n>>>>>>>>>>>>>>>>>>>>>>------------------	Send DataPack Begin ------------------>>>>>>>>>>>>>>>>>>>>>>>");
		log.debug(dataPack.dumpDataPack());
		log.debug("\n>>>>>>>>>>>>>>>>>>>>>>------------------	Send DataPack End -------------------->>>>>>>>>>>>>>>>>>>>>>>");
		bos.write(dataPack.toBytes());
		bos.flush();
		//bos.close();
	}
}
