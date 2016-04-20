/**
 * 
 */
package com.hermes.ah3.jdbc.communication;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.IOException;
import java.net.Socket;
import java.net.SocketException;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.hermes.ah3.jdbc.Ah3DriverConstant;
import com.hermes.ah3.jdbc.Ah3DriverException;
import com.hermes.ah3.jdbc.util.Base64Util;
import com.hermes.ah3.jdbc.util.ConfuseStrUtil;
import com.hermes.ah3.jdbc.util.ConvertDataType;

/**
 * 处理Connection底层与Server之间进行TCP/IP通信的逻辑类
 * @author wuwl
 *
 */
public class Ah3IO implements Iah3IO {
	
	private static Log log = LogFactory.getLog(Ah3IO.class);
	
	//尝试连接服务器的最大次数
	private static int CONNECT_MAX_TRY_CNT = 3;
	
	//发送sql给服务端的尝试次数
	private int sendSqlTryCnt = 0;
	
	private Socket ah3Socket;
	private String serverHost;
	private int serverPort;
	private String userName;
	private String userPwd;
	private Properties connProps;
	
	private Map<String,ColumnMetaData> metaDataMap = null;
	
	private List<String> resultStrDataList = null;
	
	private String[] resultDataArray = null;
	
	private String[] currRowDataArray = null;
	
	private int currRowNumber = -1;

	/**
	 * 通过指定的参数，构造一个到服务端的Socket连接对象
	 * @param serverHost 服务器名称（名称或IP）
	 * @param serverPort 服务端口
	 * @param userName 连接数据库服务端的用户名
	 * @param userPwd 连接数据库服务端的密码
	 * @param prop 初始化socket的属性集
	 * @throws Exception
	 */
	public Ah3IO(String serverHost, int serverPort,String userName,String userPwd,Properties prop) throws Exception {
		
		this.serverHost = serverHost;
		this.serverPort = serverPort;
		this.userName = userName;
		this.userPwd = userPwd;
		this.connProps = prop;
		
		this.connectToServer();		
	}
	
	/**
	 * 通过Socket连接到指定的服务端
	 * @throws Exception
	 */
	private void connectToServer() throws Exception {
		log.debug("Host: " + serverHost + ", Port: " + serverPort + ", User: " + userName + ", Pwd: " + userPwd);
		
		StandardSocketFactory socketFactory = new StandardSocketFactory();
		if (this.ah3Socket != null) {
			this.ah3Socket.close();
		}
		this.ah3Socket = socketFactory.createSocket(serverHost, serverPort, this.connProps);
		
		boolean isAuthed = this.sendHandShakePackToServer(userName,userPwd);
		
		if (!isAuthed) {
			this.ah3Socket.close();
			throw new Ah3DriverException("连接数据库的用户名或密码错误");
		}
	}
	
	/* (non-Javadoc)
	 * @see com.hermes.ah3.jdbc.communication.Iah3IO#checkAndReConnect()
	 */
	@Override
	public boolean checkAndReConnect() throws Exception {
		boolean isConnected = true;
		
		//如果socket不为空，并且连接已经关闭或没有连接到服务端，则重新建立与服务端的连接
//		if (this.ah3Socket != null && (this.ah3Socket.isClosed() || !this.ah3Socket.isConnected())) {
		if (this.ah3Socket != null) {
			try {
				//在套接字上发送一个紧急数据字节，一般情况下，如果服务端的“OOBINLINE”属性是false，就会忽略此紧急数据
				this.ah3Socket.sendUrgentData(0xFF);
				//log.debug("Send Urgent Data To Server for check connection is alive.");
			} catch (Exception e) {
				//log.error("",e);
				log.error("Lost the connection to server, Reconnect to server...");
				this.connectToServer();
//				try {
//					this.ah3Socket.sendUrgentData(0xFF);
//				} catch (Exception ee) {
//					log.error("",ee);
//				}
			}
		}
		
		this.sendSqlTryCnt = 0;
		
		return isConnected;
	}
	
	/**
	 * 与服务端建立初始连接时，从服务端读取加密密码的密钥字串
	 * @return 到服务端的连接是否有效
	 * @throws Exception
	 */
	private String getCipherCodeFromServer(BufferedInputStream inputStream) throws Exception {
		String ret = null;
		//BufferedInputStream inputStream = new BufferedInputStream(this.ah3Socket.getInputStream());
		byte[] receiverBuffer = new byte[100];
		inputStream.read(receiverBuffer, 0, receiverBuffer.length);
		ret = ConvertDataType.toString(receiverBuffer);
		return ret;
	}
	
	/**
	 * 从服务端获取握手验证时返回的验证结果
	 * @param inputStream
	 * @return
	 * @throws Exception
	 */
	private String checkErrorFromServer(BufferedInputStream inputStream) throws Exception {
		String ret = null;
		byte[] receiverBuffer = new byte[100];
		inputStream.read(receiverBuffer, 0, receiverBuffer.length);
		ret = ConvertDataType.toString(receiverBuffer).trim().toUpperCase();
		return ret;
	}
	
	/**
	 * 发送握手验证信息给服务端
	 * 
	 * 2013-07-24调整如下：
	 * 登录过程做调整了：
	*	1、	考虑到在正式的查询请求及应答之前，服务器及客户端一问一答，没有并发及复杂逻辑表达，登录过程直接发送串数据，不用包头
	*	2、	TCP连接建立成功后，server先给client发送密钥（一个随机的纯数字字符串，用于加密密码，另外加密函数要用同一份，后面的邮件发送加密函数代码）
	*	3、	Client发送用户名和加密的密码串，两个数据项之间用逗号分隔
	*	4、	Server验证后给出认证结果提示，也是串:
	*	a)	“OK” 代表认证通过，可以发送查询请求了
	*	b)	“Failure”认证失败，原因是用户名或密码错误，需要重新发送用户名和加密密码串
	*	c)	“Bye”认证失败且终止认证（关闭TCP连接）。原因是尝试失败次数过多，Client只能从重新建TCP连接开始了，或者退出。
	 * 
	 * @return 如果服务端验证用户名、密码正确，返回true，否则返回false
	 */
	private boolean sendHandShakePackToServer(String userName,String userPwd) {
		boolean retValue = false;		
//		Ah3PackReceiver receiver;
//		Ah3PackSender sender;
		BufferedInputStream inputStream;
		BufferedOutputStream outputStream;
		try {
			//新的做法，只发一个包含用户名、密码的字串到服务端
			inputStream = new BufferedInputStream(this.ah3Socket.getInputStream());
			outputStream = new BufferedOutputStream(this.ah3Socket.getOutputStream());
			
			String cipherCode = this.getCipherCodeFromServer(inputStream);
			log.debug("Get Cipher Code from Server: " + cipherCode);

			//String handShakeStr = userName + "," + ConfuseStrUtil.toShadow(userPwd.toCharArray(),cipherCode.toCharArray());
			String handShakeStr = userName + "," + Base64Util.encodeString(userPwd,cipherCode);
			
			int tryCnt = 0;
			String errorStr;
			while (tryCnt < this.CONNECT_MAX_TRY_CNT) {
				outputStream.write(handShakeStr.getBytes());
				//log.debug(handShakeStr.length());
				outputStream.flush();
				log.debug("Send Hand Shake String To Server: [" + handShakeStr + "]");
				
				errorStr = checkErrorFromServer(inputStream);
				log.debug("Hand Shake's Result from Server: " + errorStr);
				if ("FAILURE".equals(errorStr)) {					
					tryCnt++;
				} else if ("OK".equals(errorStr)) {
					retValue = true;
					break;
				} else if ("BYE".equals(errorStr)) {
					break;
				}
			}
			
			
			//旧的做法，发一个完整的验证包到服务端
//			Ah3DataPack dataPack = this.genHandShakeDataPack(cipherCode);
//			
//			sender = new Ah3PackSender(this.ah3Socket);
//			sender.sendDataPackToServer(dataPack);
//			
//			receiver = new Ah3PackReceiver(this.ah3Socket);
//			List<Ah3DataPack> retList = receiver.receiverDataFromServer();
//			
//			if (retList.size() > 0) {
//				retValue = true;
//			}
		} catch (Exception e) {
			log.error("",e);
		}
		return retValue;
	}
	
	/**
	 * 构造进行握手验证连接用户名、密码是否正确的数据包
	 * @return
	 * @param cipherCode 加密密码的密钥字串
	 * @throws Exception
	 */
//	private Ah3DataPack genHandShakeDataPack(String cipherCode) throws Exception {
//		Ah3DataPack dataPack = new Ah3DataPack();
//		dataPack.setPackType(Ah3DriverConstant.PACK_PACKTYPE_HANDSHAKE);
//		String userAndPwd = "admin,admin\n";
//		dataPack.setPackBody(userAndPwd);
//		int packLength = userAndPwd.getBytes().length + Ah3PackHead.PACK_HEAD_LENGTH;
//		dataPack.setPackLength(packLength);
//		
//		return dataPack;
//	}
	
	/* (non-Javadoc)
	 * @see com.hermes.ah3.jdbc.communication.Iah3IO#sendExecuteSQLPackToServer(java.lang.String)
	 */
	@Override
	public void sendExecuteSQLPackToServer(String sql) throws Exception {
		Ah3PackReceiver receiver;
		Ah3PackSender sender;
		boolean isGetRealDataFromServer = false;
		int heartBeatCnt = 0;
		try {			
			Ah3DataPack dataPack = this.genExecuteSqlDataPack(sql);
			
			sender = new Ah3PackSender(this.ah3Socket);
			sender.sendDataPackToServer(dataPack);
			
			this.resultStrDataList = new ArrayList<String>();
			List<Ah3DataPack> retPackList = null;
			this.metaDataMap = null;
			this.currRowDataArray = null;
			this.resultDataArray = null;
			this.sendSqlTryCnt = 0;
			this.currRowNumber = -1;
			
			while (!isGetRealDataFromServer) {				
				this.checkAndReConnect();
				receiver = new Ah3PackReceiver(this.ah3Socket);
				retPackList = receiver.receiverDataFromServer();				
				
				//把服务端返回的每行数据放到集合中
				for(Ah3DataPack tmpPack : retPackList) {				
					if (tmpPack.getPackType() == Ah3DriverConstant.PACK_PACKTYPE_HEARTLINE) {						
						if (heartBeatCnt < 2) {
							//如果是心跳检测包，发送一个回应的心跳包，收到的心跳检测包不放到结果数据中
							this.sendHeartLinePackToServer();
							heartBeatCnt++;
						}
						isGetRealDataFromServer = false;
					} else {
						if (this.metaDataMap == null) {
							this.metaDataMap = tmpPack.getMetaData();
						}	
						this.resultStrDataList.addAll(tmpPack.getDataAsList());
						isGetRealDataFromServer = true;
					}
				}
			}
			
			log.debug(this.resultStrDataList.size() + " rows selected.");
			
			//把服务端返回的每行数据放到数组中
			this.resultDataArray = new String[this.resultStrDataList.size()];
			int index = 0;
			for(String tmpStr : this.resultStrDataList) {
				this.resultDataArray[index] = tmpStr;
				index++;
			}
			
			this.currRowNumber = -1;
		
			
		} catch (SocketException ske) { //发生Socket连接错误时，需要重新连接服务端、重新发送SQL请求
			log.error("",ske);
			this.sendSqlTryCnt ++;
			if (this.sendSqlTryCnt <= this.CONNECT_MAX_TRY_CNT) {
				log.debug("Retry connect to server [" + this.sendSqlTryCnt + "]");
				//重新连接服务端
				this.connectToServer();
				//重新发送sql请求
				sendExecuteSQLPackToServer(sql);
			}
		} catch (SQLException sqe) {
			log.error("",sqe);
			throw sqe;
		} catch (Exception e) {
			log.error("",e);
			throw e;
		}
	}
	
	/**
	 * 构建包含需执行SQL的数据包
	 * @param sql
	 * @return
	 * @throws Exception
	 */
	private Ah3DataPack genExecuteSqlDataPack(String sql) throws Exception {
		Ah3DataPack dataPack = new Ah3DataPack();
		dataPack.setPackType(Ah3DriverConstant.PACK_PACKTYPE_SQL);
		dataPack.setPackBody(sql);
		int packLength = sql.getBytes().length + Ah3PackHead.PACK_HEAD_LENGTH;
		dataPack.setPackLength(packLength);
		
		return dataPack;
	}
	
	/* (non-Javadoc)
	 * @see com.hermes.ah3.jdbc.communication.Iah3IO#sendHeartLinePackToServer()
	 */
	@Override
	public void sendHeartLinePackToServer() throws Exception {
//		Ah3PackReceiver receiver;
		Ah3PackSender sender;
		Ah3DataPack dataPack = this.genHeartLineDataPack();
		
		//this.checkAndReConnect();			
		sender = new Ah3PackSender(this.ah3Socket);
		sender.sendDataPackToServer(dataPack);
		
//			receiver = new Ah3PackReceiver(this.ah3Socket);
//			List<Ah3DataPack> retList = receiver.receiverDataFromServer();
//			
//			if (retList.size() <= 0) {
//				this.close();
//			}
	}
	
	/**
	 * 关闭客户端socket连接
	 */
	private void close() {
		try {
			this.ah3Socket.close();
		} catch (IOException e) {
			log.error("",e);
		}
	}
	
	/* (non-Javadoc)
	 * @see com.hermes.ah3.jdbc.communication.Iah3IO#isClosed()
	 */
	@Override
	public boolean isClosed() {
		return this.ah3Socket.isClosed();
	}
	
	/**
	 * 构建包含需执行SQL的数据包
	 * @param sql
	 * @return
	 * @throws Exception
	 */
	private Ah3DataPack genHeartLineDataPack() throws Exception {
		Ah3DataPack dataPack = new Ah3DataPack();
		dataPack.setPackType(Ah3DriverConstant.PACK_PACKTYPE_HEARTLINE);
		String sendStr = "Heartbeat from client";
		dataPack.setPackBody(sendStr);
		int packLength = sendStr.getBytes().length + Ah3PackHead.PACK_HEAD_LENGTH;
		dataPack.setPackLength(packLength);
		
		return dataPack;
	}
	
	/* (non-Javadoc)
	 * @see com.hermes.ah3.jdbc.communication.Iah3IO#next()
	 */
	@Override
	public boolean next() {
		boolean ret = false;
		this.currRowNumber++;
		if (this.currRowNumber < this.resultStrDataList.size()) {
			ret = true;
			//指向当前游标指向的数据行
			String tmpRowData = this.resultDataArray[this.currRowNumber];
			this.currRowDataArray = tmpRowData.split(",");
		}
		return ret;
	}
	
	/* (non-Javadoc)
	 * @see com.hermes.ah3.jdbc.communication.Iah3IO#getString(java.lang.String)
	 */
	@Override
	public String getString(String columnName) throws Exception {
		String ret = null;
		//通过字段名获取字段元数据
		ColumnMetaData tmpMD = this.metaDataMap.get(columnName.toLowerCase());
		if (tmpMD != null) {
			//获取字段所处的索引
			int columnIndex = tmpMD.getIndex();
			ret = this.currRowDataArray[columnIndex];
		} else {
			throw new Ah3DriverException("结果集中找不到对应的字段["+ columnName +"]值");
		}
		
		return ret;
	}	
	
	/* (non-Javadoc)
	 * @see com.hermes.ah3.jdbc.communication.Iah3IO#getString(int)
	 */
	@Override
	public String getString(int columnIndex) throws Exception {
		if (columnIndex > this.currRowDataArray.length) {
			throw new Ah3DriverException("给出的索引[" + columnIndex + "]超出字段个数[" + this.currRowDataArray.length + "]长度");
		}
		return this.currRowDataArray[columnIndex];
	}
	
	/* (non-Javadoc)
	 * @see com.hermes.ah3.jdbc.communication.Iah3IO#getLong(java.lang.String)
	 */
	@Override
	public long getLong(String columnName) throws Exception {
		String tmpStr = this.getString(columnName);
		long ret = Long.parseLong(tmpStr);
		return ret;
	}
	
	/* (non-Javadoc)
	 * @see com.hermes.ah3.jdbc.communication.Iah3IO#getLong(int)
	 */
	@Override
	public long getLong(int columnIndex) throws Exception {
		String tmpStr = this.getString(columnIndex);
		long ret = Long.parseLong(tmpStr);
		return ret;
	}
	
	/* (non-Javadoc)
	 * @see com.hermes.ah3.jdbc.communication.Iah3IO#getShort(java.lang.String)
	 */
	@Override
	public short getShort(String columnName) throws Exception {
		String tmpStr = this.getString(columnName);
		short ret = Short.parseShort(tmpStr);
		return ret;
	}
	
	/* (non-Javadoc)
	 * @see com.hermes.ah3.jdbc.communication.Iah3IO#getShort(int)
	 */
	@Override
	public short getShort(int columnIndex) throws Exception {
		String tmpStr = this.getString(columnIndex);
		short ret = Short.parseShort(tmpStr);
		return ret;
	}
	
	/* (non-Javadoc)
	 * @see com.hermes.ah3.jdbc.communication.Iah3IO#getInt(java.lang.String)
	 */
	@Override
	public int getInt(String columnName) throws Exception {
		String tmpStr = this.getString(columnName);
		int ret = Integer.parseInt(tmpStr);
		return ret;
	}
	
	/* (non-Javadoc)
	 * @see com.hermes.ah3.jdbc.communication.Iah3IO#getInt(int)
	 */
	@Override
	public int getInt(int columnIndex) throws Exception {
		String tmpStr = this.getString(columnIndex);
		int ret = Integer.parseInt(tmpStr);
		return ret;
	}
	
	/* (non-Javadoc)
	 * @see com.hermes.ah3.jdbc.communication.Iah3IO#getDouble(java.lang.String)
	 */
	@Override
	public double getDouble(String columnName) throws Exception {
		String tmpStr = this.getString(columnName);
		double ret = Double.parseDouble(tmpStr);
		return ret;
	}
	
	/* (non-Javadoc)
	 * @see com.hermes.ah3.jdbc.communication.Iah3IO#getDouble(int)
	 */
	@Override
	public double getDouble(int columnIndex) throws Exception {
		String tmpStr = this.getString(columnIndex);
		double ret = Double.parseDouble(tmpStr);
		return ret;
	}
	
	/* (non-Javadoc)
	 * @see com.hermes.ah3.jdbc.communication.Iah3IO#forceClose()
	 */
	@Override
	public void forceClose() {
		try {
			this.ah3Socket.close();
			log.debug("Close connection to server...");
		} catch (IOException e) {
			log.error("",e);
		}
	}
	
	/* (non-Javadoc)
	 * @see com.hermes.ah3.jdbc.communication.Iah3IO#quit()
	 */
	@Override
	public void quit() {
		this.currRowDataArray = null;
		this.metaDataMap = null;
		this.resultDataArray = null;
		this.resultStrDataList = null;
		this.forceClose();
		log.debug("quit...");
	}
	
	public static void main(String[] args) {
		Properties prop = new Properties();
		try {
			Iah3IO io = new Ah3IO("localhost",12345,"admin1","pass1",prop);
			while (true) {
				io.sendExecuteSQLPackToServer("select * from table");
				StringBuffer tmpSb = new StringBuffer();
				while (io.next()) {
					tmpSb.append(io.getString(0)).append("@")
						.append(io.getString(1)).append("@")
						.append(io.getString(2)).append("@")
						.append(io.getString(3)).append("@")
						.append(io.getString(4));
					log.debug(tmpSb.toString());
					tmpSb.delete(0, tmpSb.length());
				}
				
				Thread.sleep(10000);
			}
		} catch (Exception e) {
			log.error("",e);
		}
			
	}

	@Override
	public Map<String,ColumnMetaData> getMetaData() throws Exception {
		return this.metaDataMap;
	}
}
