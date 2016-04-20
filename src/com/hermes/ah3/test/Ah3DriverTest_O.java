/**
 * 
 */
package com.hermes.ah3.test;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.Statement;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.hermes.ah3.jdbc.communication.implc.ah3dri;

/**
 * @author wuwl
 *
 */
public class Ah3DriverTest_O {
	private static Log log = LogFactory.getLog(Ah3DriverTest_O.class);

	/**
	 * 
	 */
	public Ah3DriverTest_O() {
		// TODO Auto-generated constructor stub
	}

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		//String url = "jdbc:hermes://localhost:12345/test" ;
		String url = "jdbc:hermes://10.1.48.229:9087/test";
	    String username = "xielj" ;   
	    String password = "xielj" ;   
		try {
			Class.forName("com.hermes.ah3.jdbc.Driver") ;			
			
			Connection con = DriverManager.getConnection(url , username , password ) ;   
			
			int cnt = 0;
			int cycle = 1;

			//while (true) {
				exec(con," select * from city ");
				exec(con," select * from sex ");
				//Thread.sleep( cycle * 5 * 1000);
				//cnt ++;
				
				//System.out.println(">>>>>>>>>>>>>>" + cnt * cycle * 10 + "<<<<<<<<<<<<<<<<<");
			//}
			
			
		} catch (Exception e) {
			log.error("",e);
		}
	}
	
	private static void exec(Connection con,String sql) throws Exception {
		Statement stmt = con.createStatement() ; 
		ResultSet rs;
		int cnt = 0;
		int cycle = 1;
		long totalMem,freeMem,usedMem;
			rs = stmt.executeQuery(sql) ;
//			rs = stmt.executeQuery(" select name,age,caller_length,flow_3g,flow_tel,level,credit_level,credit_amount,roam_time,roam_calls,roam_city,calls,brand,network_life,city from gsm_data");
			java.sql.ResultSetMetaData md = rs.getMetaData();
			System.out.println("Column Count: " + md.getColumnCount());
			for (int i = 0; i < md.getColumnCount(); i++) {
				System.out.println("Column name: " + md.getColumnName(i)+", dataType: " + md.getColumnType(i) + ";");
			}
			System.out.println("");
			while (rs.next()) {
//				System.out.print("dept_name:" + rs.getString("dept_name"));
//				System.out.println(", ename:" + rs.getString("ename"));
				System.out.print(rs.getString(0));
				System.out.println(rs.getString(1));
				//rs.getInt("");
				//rs.getShort("");
				//rs.getFloat("");
				//rs.getDouble("");
				//rs.getDate("");
				//rs.getLong("");
				//rs.getTime("");
			}
			usedMem = Runtime.getRuntime().totalMemory() - Runtime.getRuntime().freeMemory();
			System.out.println(">>>>>>>>>>>>>>>>Last usedMemory:" + usedMem);
	}
	
//	public static void main(String[] args)
//	{
//		
//		ah3dri jd = null;
//		//try {
//		int ret=-1,ival;
//		String expr = new String("select * from staff where like(ename,'��%')\n");
//		String cp;
//
//
//		jd = new ah3dri();
//		if(jd == null) {
//			System.out.print("new ah3dri failure\n");
//		}
//
//		System.out.print("new ah3dri success");
//		try {
//
//			ret = jd.JInit();
//			ret = jd.JAuthenticate("10.1.48.229", "9087", "xielj", "xielj");
//	
//			if((ret=jd.JQuery(expr)) >0)
//			{
//				while(true)
//				{
//					if((ret=jd.JFetch()) <= 0) break;
//		
//					cp = jd.JGetStringFname("dept_name");
//					System.out.print("[" + cp + "]--");
//		
//					ival = jd.JGetIntFname("eno");
//					System.out.print("["+ival);
//	
//					//cp = jd.JGetStringFname("ename");
//					cp = jd.JGetCStringFname("ename");
//					System.out.println(cp+"]");
//				}
//			}
//			else {
//				System.out.println("JQuery sqlcode:"+jd.JSqlcode());
//			}
//		} catch (Exception e) {
//			e.printStackTrace();
//		} finally {
//			if (jd != null)
//			{
//				jd.JClose();
//				jd.JDestory();
//			}
//		}
//	}

	
	
//	No.     FLDNAME         FLDTYPE         MIN     MAX     KEY
//	  1     customer_id     long            1       1000    -
//	  2     msisdn          char[11]        -       -       -
//	  3     bcdimsi         bcdchar[8]      -       -       Yes
//	  4     prepay_fee      $li             -       -       -
//	  5     id_card         varchar[18]     -       -       -
//	  6     province        char[2]         -       -       -
//	  7     region          long            -       -       -
//	  8     county          int             -       -       -
//	  9     brand_id        varchar[12]     -       -       -
//	 10     bill_type       $li             1       1024    -
//	 11     deposit         double          0       100000  -
//	 12     sex             short           0       100     -
//	 13     op_time         datetime        -       -       -
	//ah3s -d -fher.conf

	/**
	 * ʹ�ø������û���/���뽨����ָ�����������˿ڵ�socket����
	 * @param serverIP �磺10.1.1.254
	 * @param serverPort �磺9087
	 * @param username �磺xielj
	 * @param userPwd �磺xielj
	 * @return ������֤�Ƿ�ɹ����ɹ�����true��ʧ�ܷ���false
	 * @throws SocketException
	 */
	public boolean authenticate(String serverIP, String serverPort, String username, String userPwd) throws java.net.SocketException {
		return false;
	}

	/**
	 * ִ��ָ����SQL��䲢����ִ�н��
	 * @param sql Ҫִ�е�sql��䣬�磺select * from city
	 * @return ִ�н����String[2]�����У�
	 * 			1��String[0]��ŵ��Ƿ��������صİ�ͷ��Ϣ��ֵ��ʽ�磺Version:1,PackType:3,MetaType:3,PackageSeqno:1,ReturnCode:1,ThreadId:9,RequestId:8,TotalLength:163
	 * 			2��String[1]��ŵ��Ƿ��������صİ�����Ϣ��ֵ��ʽ�磺ÿ�е�ֵʹ�ö��ŷָ���ÿ��ʹ��"\n"�ָ���ԭʼ�ķ������˷���ֵ������Ҫ������
	 * @throws SQLException
	 */
	public String[] sendExecuteSqlToServer(String  sql) throws java.sql.SQLException {
		return null;
	}
	
	/**
	 * �ر������˵�socket����
	 * @throws java.io.IOException ��������ѹرջ�رչ����з��������׳��˴���
	 */
	public void close() throws java.io.IOException {
		
	}
	
	/**
	 * ����һ�����ԵĽ������ݵ�����ˣ���⵽����˵������Ƿ���Ч
	 * ע��Socket��SO_OOBINLINE����û�д򿪣��ͻ��Զ���������ֽڣ���SO_OOBINLINE����Ĭ��������ǹرյģ���֪��C�е�socket�Ƿ�������������ã�
	 * @param data �����͵Ľ������ݣ�һ����һ�������ʮ��������
	 * @throws java.net.SocketException ���������˵�������Ч���׳�����
	 */
	public void sendUrgentData(byte data) throws java.net.SocketException {
		
	}
	
	//http://blog.csdn.net/kangyaping/article/details/6584027
}
