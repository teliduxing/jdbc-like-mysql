/**
 * 
 */
package com.hermes.ah3.server;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.net.Socket;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.hermes.ah3.jdbc.Ah3DriverConstant;
import com.hermes.ah3.jdbc.communication.Ah3DataPack;
import com.hermes.ah3.jdbc.communication.Ah3PackHead;
import com.hermes.ah3.jdbc.communication.Ah3PackReceiver;
import com.hermes.ah3.jdbc.communication.Ah3PackSender;
import com.hermes.ah3.jdbc.util.ConvertDataType;

/**
 * @author wuwl
 *
 */
public class Ah3ClientProcesser extends Thread {
	
	private static Log log = LogFactory.getLog(Ah3ClientProcesser.class);
	
	private Socket socket;
	private boolean iAmKeepRunning = true;
	
	public Ah3ClientProcesser(Socket socket) {
		this.socket = socket;
	}
	
	public void shutDown() {
		this.iAmKeepRunning = false;
	}
	
	public void run() {
		log.debug("Process request from " + this.socket.getRemoteSocketAddress() + " Start...");
		Ah3PackReceiver receiver;
		Ah3PackSender sender;
		try {
//			BufferedOutputStream bos = new BufferedOutputStream(this.socket.getOutputStream());
//			BufferedInputStream bis = new  BufferedInputStream(this.socket.getInputStream());
//			
//			byte[] receiverBuffer = new byte[this.M_MAX_SIZE];
//			while (bis.read(receiverBuffer,0,receiverBuffer.length) != -1) {
//				log.debug(new String(receiverBuffer));
//			}
//			
//			String retValue = "";
//			bos.write(retValue.getBytes());
			
			if (!doHandShakeWithClient()) {
				this.socket.close();
			}
			
			receiver = new Ah3PackReceiver(this.socket);
			sender = new Ah3PackSender(this.socket);
			while (iAmKeepRunning) {
				List<Ah3DataPack> retList = receiver.receiverDataFromServer();
				Ah3DataPack requestDataPack = null;
				for(Ah3DataPack tmpPack : retList) {
					log.debug(tmpPack.dumpDataPack());
					requestDataPack = tmpPack;
				}
				
				
				Ah3DataPack retDataPack = null;
				if (requestDataPack.getPackType() == Ah3DriverConstant.PACK_PACKTYPE_HANDSHAKE) {
					retDataPack = this.genHandShakeDataPack(requestDataPack);
				}
				else if (requestDataPack.getPackType() == Ah3DriverConstant.PACK_PACKTYPE_SQL) {
					retDataPack = this.genExecuteSqlDataPack(requestDataPack);
				}
				if (retDataPack == null) {
					this.socket.close();
				} else {
					sender.sendDataPackToServer(retDataPack);
				}
			}			
		} catch (Exception e) {
			log.error("",e);
		}
		log.debug("Process request from " + this.socket.getRemoteSocketAddress() + " End...");
	}
	
	
	private boolean doHandShakeWithClient() throws Exception {
		boolean ret = false;
		BufferedOutputStream bos = new BufferedOutputStream(this.socket.getOutputStream());
		BufferedInputStream bis = new  BufferedInputStream(this.socket.getInputStream());
		
		//向客户端发送加密密码的密钥
		String cipherCode = "1791273929werewjrlejrpw";
		bos.write(cipherCode.getBytes());
		bos.flush();
		
		ret = tryHandShakeAutheticate(bis,bos,0);
		
		return ret;
	}
	
	private boolean tryHandShakeAutheticate(BufferedInputStream bis,BufferedOutputStream bos,int tryCnt) throws Exception {
		boolean ret = false;
		
		//获取客户端发送来的用户名、密码，格式如“username,加密后的密码”
		byte[] receiverBuffer = new byte[100];
		bis.read(receiverBuffer, 0, receiverBuffer.length);
		String tmpStr = ConvertDataType.toString(receiverBuffer).trim();
		log.debug("Get userName And Pwd from Client: " + tmpStr);
		String[] userAndPwd = tmpStr.split(",");
		String userName = userAndPwd[0];
		String userPwd = userAndPwd[1];
		
		String result = "failure";
		if (userName.equals("admin")) {
			bos.write("ok".getBytes());
			bos.flush();
			ret = true;
		} else {
			if (tryCnt < 2) {
				tryCnt++;
				bos.write(result.getBytes());
				bos.flush();
				tryHandShakeAutheticate(bis,bos,tryCnt);				
			} else {
				bos.write("bye".getBytes());
				bos.flush();
			}
		}
		return ret;
	}

	/**
	 * 验证客户端发送来的用户名、密码是否正确
	 * @param requestDataPack
	 * @return
	 * @throws Exception
	 */
	private Ah3DataPack genHandShakeDataPack(Ah3DataPack requestDataPack) throws Exception {
		String userAndPwd = requestDataPack.getDataAsList().get(0);
		String[] userAndPwdArr = userAndPwd.split(",");
		boolean isAuthed = false;
		String user = null;
		if (userAndPwdArr.length == 2) {
			user = userAndPwdArr[0];
			String pwd = userAndPwdArr[1];
			if (user.trim().toLowerCase().equals("admin")
					&& pwd.trim().toLowerCase().equals("admin")) {
				isAuthed = true;
			}
		}
		
		Ah3DataPack dataPack = null;
		if (isAuthed) {
			dataPack = new Ah3DataPack();
			dataPack.setPackType(Ah3DriverConstant.PACK_PACKTYPE_HANDSHAKE);
			String retStr = "Welcome! " + user;
			dataPack.setPackBody(retStr);
			int packLength = retStr.getBytes().length + Ah3PackHead.PACK_HEAD_LENGTH;
			dataPack.setPackLength(packLength);
		}
		return dataPack;
	}
	
	private Ah3DataPack genExecuteSqlDataPack(Ah3DataPack requestDataPack) throws Exception {		
		Ah3DataPack dataPack  = new Ah3DataPack();
		dataPack.setPackType(Ah3DriverConstant.PACK_PACKTYPE_SQL);
		StringBuffer retStr = new StringBuffer();
		retStr.append("column1,column2,column3,column4,column5\n")
			.append("dataType1,dataType2,dataType3,dataType4,dataType5\n")
			.append("value11,value12,value13,value14,value15\n")
			.append("value21,value22,value33,value24,value25\n")
			.append("value31,value32,value33,value34,value35\n")
			.append("value41,value42,value43,value44,value45\n")
			.append("value51,value52,value53,value54,value55\n")
			.append("value61,value62,value63,value64,value65\n");
		dataPack.setPackBody(retStr.toString());
		int packLength = retStr.toString().getBytes().length + Ah3PackHead.PACK_HEAD_LENGTH;
		dataPack.setPackLength(packLength);
		return dataPack;
	}
	
	/**
	 * @param args
	 */
	public static void main(String[] args) {
		// TODO Auto-generated method stub

	}

}
