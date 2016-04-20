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
 * ����Connection�ײ���Server֮�����TCP/IPͨ�ŵ��߼���
 * @author wuwl
 *
 */
public class Ah3IO implements Iah3IO {
	
	private static Log log = LogFactory.getLog(Ah3IO.class);
	
	//�������ӷ�������������
	private static int CONNECT_MAX_TRY_CNT = 3;
	
	//����sql������˵ĳ��Դ���
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
	 * ͨ��ָ���Ĳ���������һ��������˵�Socket���Ӷ���
	 * @param serverHost ���������ƣ����ƻ�IP��
	 * @param serverPort ����˿�
	 * @param userName �������ݿ����˵��û���
	 * @param userPwd �������ݿ����˵�����
	 * @param prop ��ʼ��socket�����Լ�
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
	 * ͨ��Socket���ӵ�ָ���ķ����
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
			throw new Ah3DriverException("�������ݿ���û������������");
		}
	}
	
	/* (non-Javadoc)
	 * @see com.hermes.ah3.jdbc.communication.Iah3IO#checkAndReConnect()
	 */
	@Override
	public boolean checkAndReConnect() throws Exception {
		boolean isConnected = true;
		
		//���socket��Ϊ�գ����������Ѿ��رջ�û�����ӵ�����ˣ������½��������˵�����
//		if (this.ah3Socket != null && (this.ah3Socket.isClosed() || !this.ah3Socket.isConnected())) {
		if (this.ah3Socket != null) {
			try {
				//���׽����Ϸ���һ�����������ֽڣ�һ������£��������˵ġ�OOBINLINE��������false���ͻ���Դ˽�������
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
	 * �����˽�����ʼ����ʱ���ӷ���˶�ȡ�����������Կ�ִ�
	 * @return ������˵������Ƿ���Ч
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
	 * �ӷ���˻�ȡ������֤ʱ���ص���֤���
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
	 * ����������֤��Ϣ�������
	 * 
	 * 2013-07-24�������£�
	 * ��¼�����������ˣ�
	*	1��	���ǵ�����ʽ�Ĳ�ѯ����Ӧ��֮ǰ�����������ͻ���һ��һ��û�в����������߼�����¼����ֱ�ӷ��ʹ����ݣ����ð�ͷ
	*	2��	TCP���ӽ����ɹ���server�ȸ�client������Կ��һ������Ĵ������ַ��������ڼ������룬������ܺ���Ҫ��ͬһ�ݣ�������ʼ����ͼ��ܺ������룩
	*	3��	Client�����û����ͼ��ܵ����봮������������֮���ö��ŷָ�
	*	4��	Server��֤�������֤�����ʾ��Ҳ�Ǵ�:
	*	a)	��OK�� ������֤ͨ�������Է��Ͳ�ѯ������
	*	b)	��Failure����֤ʧ�ܣ�ԭ�����û��������������Ҫ���·����û����ͼ������봮
	*	c)	��Bye����֤ʧ������ֹ��֤���ر�TCP���ӣ���ԭ���ǳ���ʧ�ܴ������࣬Clientֻ�ܴ����½�TCP���ӿ�ʼ�ˣ������˳���
	 * 
	 * @return ����������֤�û�����������ȷ������true�����򷵻�false
	 */
	private boolean sendHandShakePackToServer(String userName,String userPwd) {
		boolean retValue = false;		
//		Ah3PackReceiver receiver;
//		Ah3PackSender sender;
		BufferedInputStream inputStream;
		BufferedOutputStream outputStream;
		try {
			//�µ�������ֻ��һ�������û�����������ִ��������
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
			
			
			//�ɵ���������һ����������֤���������
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
	 * �������������֤�����û����������Ƿ���ȷ�����ݰ�
	 * @return
	 * @param cipherCode �����������Կ�ִ�
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
				
				//�ѷ���˷��ص�ÿ�����ݷŵ�������
				for(Ah3DataPack tmpPack : retPackList) {				
					if (tmpPack.getPackType() == Ah3DriverConstant.PACK_PACKTYPE_HEARTLINE) {						
						if (heartBeatCnt < 2) {
							//�������������������һ����Ӧ�����������յ��������������ŵ����������
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
			
			//�ѷ���˷��ص�ÿ�����ݷŵ�������
			this.resultDataArray = new String[this.resultStrDataList.size()];
			int index = 0;
			for(String tmpStr : this.resultStrDataList) {
				this.resultDataArray[index] = tmpStr;
				index++;
			}
			
			this.currRowNumber = -1;
		
			
		} catch (SocketException ske) { //����Socket���Ӵ���ʱ����Ҫ�������ӷ���ˡ����·���SQL����
			log.error("",ske);
			this.sendSqlTryCnt ++;
			if (this.sendSqlTryCnt <= this.CONNECT_MAX_TRY_CNT) {
				log.debug("Retry connect to server [" + this.sendSqlTryCnt + "]");
				//�������ӷ����
				this.connectToServer();
				//���·���sql����
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
	 * ����������ִ��SQL�����ݰ�
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
	 * �رտͻ���socket����
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
	 * ����������ִ��SQL�����ݰ�
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
			//ָ��ǰ�α�ָ���������
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
		//ͨ���ֶ�����ȡ�ֶ�Ԫ����
		ColumnMetaData tmpMD = this.metaDataMap.get(columnName.toLowerCase());
		if (tmpMD != null) {
			//��ȡ�ֶ�����������
			int columnIndex = tmpMD.getIndex();
			ret = this.currRowDataArray[columnIndex];
		} else {
			throw new Ah3DriverException("��������Ҳ�����Ӧ���ֶ�["+ columnName +"]ֵ");
		}
		
		return ret;
	}	
	
	/* (non-Javadoc)
	 * @see com.hermes.ah3.jdbc.communication.Iah3IO#getString(int)
	 */
	@Override
	public String getString(int columnIndex) throws Exception {
		if (columnIndex > this.currRowDataArray.length) {
			throw new Ah3DriverException("����������[" + columnIndex + "]�����ֶθ���[" + this.currRowDataArray.length + "]����");
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
