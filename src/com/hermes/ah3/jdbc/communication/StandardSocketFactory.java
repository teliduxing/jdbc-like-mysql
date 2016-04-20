/**
 * 
 */
package com.hermes.ah3.jdbc.communication;

import java.net.Socket;
import java.util.Properties;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * 构造客户端Socket的标准工厂类
 * @author wuwl
 *
 */
public class StandardSocketFactory {
	
	private static Log log = LogFactory.getLog(StandardSocketFactory.class);
	
	public static final String TCP_NO_DELAY_PROPERTY_NAME = "tcpNoDelay";

	public static final String TCP_KEEP_ALIVE_PROPERTY_NAME = "tcpKeepAlive";

	public static final String TCP_RCV_BUF_PROPERTY_NAME = "tcpRcvBuf";

	public static final String TCP_SND_BUF_PROPERTY_NAME = "tcpSndBuf";

	public static final String TCP_TRAFFIC_CLASS_PROPERTY_NAME = "tcpTrafficClass";
	
	public static final String TCP_CONN_TIMEOUT_PROPERTY_NAME = "connectionTimeout";
	
	public static final String TCP_CONN_TIMEOUT_DEFAULT_VALUE = "0";

	public static final String TCP_RCV_BUF_DEFAULT_VALUE = "8196";

	public static final String TCP_SND_BUF_DEFAULT_VALUE = "8196";

	public static final String TCP_TRAFFIC_CLASS_DEFAULT_VALUE = "0";

	public static final String TCP_NO_DELAY_DEFAULT_VALUE = "true";
	
	public static final String TCP_KEEP_ALIVE_DEFAULT_VALUE = "true";
	
	/**
	 * 创建一个与服务端连接的客户端Socket实例
	 * @param serverHost
	 * @param serverPort
	 * @param prop
	 * @return 可使用的socket实例
	 * @throws Exception
	 */
	public Socket createSocket(String serverHost, int serverPort,Properties prop) throws Exception {
		Socket socket = new Socket(serverHost,serverPort);
		
		this.configureSocket(socket, prop);		
		
		return socket;
	}
	
	/**
	 * 使用配配置文件中的熟悉配置初始Socket
	 * @param socket
	 * @param prop
	 * @throws Exception
	 */
	private void configureSocket(Socket socket,Properties prop) throws Exception {
		int timeOut = this.getPropertyAsIntValue(TCP_CONN_TIMEOUT_PROPERTY_NAME, TCP_CONN_TIMEOUT_DEFAULT_VALUE, prop);
		socket.setSoTimeout(timeOut);
		
		int rcvBuf = this.getPropertyAsIntValue(TCP_RCV_BUF_PROPERTY_NAME, TCP_RCV_BUF_DEFAULT_VALUE, prop);
		socket.setReceiveBufferSize(rcvBuf);
		
		int sndBuf = this.getPropertyAsIntValue(TCP_SND_BUF_PROPERTY_NAME, TCP_SND_BUF_DEFAULT_VALUE, prop);
		socket.setSendBufferSize(sndBuf);
		
		int trafficClass = this.getPropertyAsIntValue(TCP_TRAFFIC_CLASS_PROPERTY_NAME, TCP_TRAFFIC_CLASS_DEFAULT_VALUE, prop);
		socket.setTrafficClass(trafficClass);
		
		boolean noDelay = this.getPropertyAsBoolValue(TCP_NO_DELAY_PROPERTY_NAME, TCP_NO_DELAY_DEFAULT_VALUE, prop);
		socket.setTcpNoDelay(noDelay);
		
		boolean keepAlive = this.getPropertyAsBoolValue(TCP_KEEP_ALIVE_PROPERTY_NAME, TCP_KEEP_ALIVE_DEFAULT_VALUE, prop);
		socket.setKeepAlive(keepAlive);
	}
	
	/**
	 * 从属性集中获取整型参数值（如果参数集中没有对应的属性值，则使用传入的默认参数值）
	 * @param propName 参数名称
	 * @param propDefaultValue 参数默认值
	 * @param prop 属性集
	 * @return 参数值
	 */
	private int getPropertyAsIntValue(String propName,String propDefaultValue,Properties prop) {
		String propValue = prop.getProperty(propName, propDefaultValue);
		int retIntValue = 0;
		try {
			retIntValue = Integer.parseInt(propValue);
		} catch (Exception e) {
			log.error("parseInt propertie [" + propName + "]=" + propValue + " failure",e);
		}
		return retIntValue;
	}
	
	/**
	 * 从属性集中获取布尔型参数值（如果参数集中没有对应的属性值，则使用传入的默认参数值）
	 * @param propName 参数名称
	 * @param propDefaultValue 参数默认值
	 * @param prop 属性集
	 * @return 参数值
	 */
	private boolean getPropertyAsBoolValue(String propName,String propDefaultValue,Properties prop) {
		String propValue = prop.getProperty(propName, propDefaultValue);
		boolean retBoolValue = false;
		try {
			retBoolValue = Boolean.parseBoolean(propValue);
		} catch (Exception e) {
			log.error("parseInt propertie [" + propName + "]=" + propValue + " failure",e);
		}
		return retBoolValue;
	}
	

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		// TODO Auto-generated method stub

	}

}
