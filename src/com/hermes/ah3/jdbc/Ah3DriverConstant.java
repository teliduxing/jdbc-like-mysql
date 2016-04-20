/**
 * 
 */
package com.hermes.ah3.jdbc;

import java.util.Map;

/**
 * Ah3 driver中使用到的常量定义
 * @author wuwl
 *
 */
public class Ah3DriverConstant {
	//Hermes Server 的默认地址
	public static String DB_DEFAULT_HOST = "localhost";
	//Hermes Server 的默认端口
	public static String DB_DEFAULT_PORT = "8087";
	
	public static String JDBC_CONNECTION_HOST = "JDBC_CONNECTION_HOST";
	
	public static String JDBC_CONNECTION_PORT = "JDBC_CONNECTION_PORT";

	public static String JDBC_CONNECTION_USER = "JDBC_CONNECTION_USER";
	
	public static String JDBC_CONNECTION_PWD = "JDBC_CONNECTION_PWD";
	
	public static String JDBC_CONNECTION_URL = "JDBC_CONNECTION_URL";
	
	//发送握手验证包时的包类型
	public static byte PACK_PACKTYPE_HANDSHAKE = 0x01;
	//发送心跳检测包时的包类型
	public static byte PACK_PACKTYPE_HEARTLINE = 0x02;
	//发送SQL命令包时的包类型
	public static byte PACK_PACKTYPE_SQL = 0x03;
	
	//需要元数据时metaType属性的取值-需要字段名
	public static byte PACK_METATYPE_NEED_COLUMNNAME = 0x01;
	//需要元数据时metaType属性的取值-需要字段数据类型
	public static byte PACK_METATYPE_NEED_COLUMNTYPE = 0x02;
	//需要元数据时metaType属性的取值-字段名、字段数据类型都需要
	public static byte PACK_METATYPE_NEED_BOTH = 0x03;
	
	//包头默认的版本号
	public static byte PACK_VERSION = 0x01;
	
	public static byte PACK_SEQNO_HEAD = 0x01;
	public static byte PACK_SEQNO_MIDDLE = 0x02;
	public static byte PACK_SEQNO_TAIL = 0x03;
	
	//返回包的执行结果-成功
	public static byte PACK_RETURNCODE_SUCCESS = 1;
	//返回包的执行结果-失败
	public static byte PACK_RETURNCODE_FAILURE = 0;
	
	//包头中threadId属性的默认值-目前无使用意义
	public static byte PACK_THREADID_DEFAULT = 0x09;
	//包头中requestId属性的默认值-目前无使用意义
	public static byte PACK_REQUESTID_DEFAULT = 0x08;
	//包头中最后一个字符-回车换行符，方便调试用
	public static byte SPLITCHAR_DEFAULT = '\n';
	
	//以下是包体字段元数据中字段数据类型的定义
	public static String PACK_COLUMN_DATA_TYPE_SHORT = "1";	
	public static String PACK_COLUMN_DATA_TYPE_INT = "2";	
	public static String PACK_COLUMN_DATA_TYPE_$LI = "3";	
	public static String PACK_COLUMN_DATA_TYPE_LONG = "4";	
	public static String PACK_COLUMN_DATA_TYPE_DATE = "5";	
	public static String PACK_COLUMN_DATA_TYPE_TIME = "6";	
	public static String PACK_COLUMN_DATA_TYPE_DATETIME = "7";	
	public static String PACK_COLUMN_DATA_TYPE_FLOAT = "8";	
	public static String PACK_COLUMN_DATA_TYPE_DOUBLE = "9";	
	public static String PACK_COLUMN_DATA_TYPE_CHAR = "10";	
	public static String PACK_COLUMN_DATA_TYPE_BCDCHAR = "11";	
	public static String PACK_COLUMN_DATA_TYPE_VARCHAR = "12";
	
	public static enum ColumnDataTypeEnum {
		PACK_COLUMN_DATA_TYPE_SHORT,
		PACK_COLUMN_DATA_TYPE_INT,
		PACK_COLUMN_DATA_TYPE_$LI,
		PACK_COLUMN_DATA_TYPE_LONG,
		PACK_COLUMN_DATA_TYPE_DATE,
		PACK_COLUMN_DATA_TYPE_TIME,
		PACK_COLUMN_DATA_TYPE_DATETIME,
		PACK_COLUMN_DATA_TYPE_FLOAT,
		PACK_COLUMN_DATA_TYPE_DOUBLE,
		PACK_COLUMN_DATA_TYPE_CHAR,
		PACK_COLUMN_DATA_TYPE_BCDCHAR,
		PACK_COLUMN_DATA_TYPE_VARCHAR
	};
}
