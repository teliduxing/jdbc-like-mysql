/**
 * 
 */
package com.hermes.ah3.jdbc;

import java.util.Map;

/**
 * Ah3 driver��ʹ�õ��ĳ�������
 * @author wuwl
 *
 */
public class Ah3DriverConstant {
	//Hermes Server ��Ĭ�ϵ�ַ
	public static String DB_DEFAULT_HOST = "localhost";
	//Hermes Server ��Ĭ�϶˿�
	public static String DB_DEFAULT_PORT = "8087";
	
	public static String JDBC_CONNECTION_HOST = "JDBC_CONNECTION_HOST";
	
	public static String JDBC_CONNECTION_PORT = "JDBC_CONNECTION_PORT";

	public static String JDBC_CONNECTION_USER = "JDBC_CONNECTION_USER";
	
	public static String JDBC_CONNECTION_PWD = "JDBC_CONNECTION_PWD";
	
	public static String JDBC_CONNECTION_URL = "JDBC_CONNECTION_URL";
	
	//����������֤��ʱ�İ�����
	public static byte PACK_PACKTYPE_HANDSHAKE = 0x01;
	//������������ʱ�İ�����
	public static byte PACK_PACKTYPE_HEARTLINE = 0x02;
	//����SQL�����ʱ�İ�����
	public static byte PACK_PACKTYPE_SQL = 0x03;
	
	//��ҪԪ����ʱmetaType���Ե�ȡֵ-��Ҫ�ֶ���
	public static byte PACK_METATYPE_NEED_COLUMNNAME = 0x01;
	//��ҪԪ����ʱmetaType���Ե�ȡֵ-��Ҫ�ֶ���������
	public static byte PACK_METATYPE_NEED_COLUMNTYPE = 0x02;
	//��ҪԪ����ʱmetaType���Ե�ȡֵ-�ֶ������ֶ��������Ͷ���Ҫ
	public static byte PACK_METATYPE_NEED_BOTH = 0x03;
	
	//��ͷĬ�ϵİ汾��
	public static byte PACK_VERSION = 0x01;
	
	public static byte PACK_SEQNO_HEAD = 0x01;
	public static byte PACK_SEQNO_MIDDLE = 0x02;
	public static byte PACK_SEQNO_TAIL = 0x03;
	
	//���ذ���ִ�н��-�ɹ�
	public static byte PACK_RETURNCODE_SUCCESS = 1;
	//���ذ���ִ�н��-ʧ��
	public static byte PACK_RETURNCODE_FAILURE = 0;
	
	//��ͷ��threadId���Ե�Ĭ��ֵ-Ŀǰ��ʹ������
	public static byte PACK_THREADID_DEFAULT = 0x09;
	//��ͷ��requestId���Ե�Ĭ��ֵ-Ŀǰ��ʹ������
	public static byte PACK_REQUESTID_DEFAULT = 0x08;
	//��ͷ�����һ���ַ�-�س����з������������
	public static byte SPLITCHAR_DEFAULT = '\n';
	
	//�����ǰ����ֶ�Ԫ�������ֶ��������͵Ķ���
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
