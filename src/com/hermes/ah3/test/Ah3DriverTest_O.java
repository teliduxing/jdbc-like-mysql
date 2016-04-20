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
//		String expr = new String("select * from staff where like(ename,'王%')\n");
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
	 * 使用给定的用户名/密码建立与指定服务器及端口的socket连接
	 * @param serverIP 如：10.1.1.254
	 * @param serverPort 如：9087
	 * @param username 如：xielj
	 * @param userPwd 如：xielj
	 * @return 连接验证是否成功，成功返回true，失败返回false
	 * @throws SocketException
	 */
	public boolean authenticate(String serverIP, String serverPort, String username, String userPwd) throws java.net.SocketException {
		return false;
	}

	/**
	 * 执行指定的SQL语句并返回执行结果
	 * @param sql 要执行的sql语句，如：select * from city
	 * @return 执行结果，String[2]，其中：
	 * 			1、String[0]存放的是服务器返回的包头信息，值格式如：Version:1,PackType:3,MetaType:3,PackageSeqno:1,ReturnCode:1,ThreadId:9,RequestId:8,TotalLength:163
	 * 			2、String[1]存放的是服务器返回的包体信息，值格式如：每列的值使用逗号分隔，每行使用"\n"分隔（原始的服务器端返回值，不需要做处理）
	 * @throws SQLException
	 */
	public String[] sendExecuteSqlToServer(String  sql) throws java.sql.SQLException {
		return null;
	}
	
	/**
	 * 关闭与服务端的socket连接
	 * @throws java.io.IOException 如果连接已关闭或关闭过程中发生错误，抛出此错误
	 */
	public void close() throws java.io.IOException {
		
	}
	
	/**
	 * 发送一个测试的紧急数据到服务端，检测到服务端的连接是否还有效
	 * 注：Socket的SO_OOBINLINE属性没有打开，就会自动舍弃这个字节，而SO_OOBINLINE属性默认情况下是关闭的，不知道C中的socket是否有这个属性设置？
	 * @param data 待发送的紧急数据，一般是一个任意的十六进制数
	 * @throws java.net.SocketException 如果到服务端的连接无效，抛出错误
	 */
	public void sendUrgentData(byte data) throws java.net.SocketException {
		
	}
	
	//http://blog.csdn.net/kangyaping/article/details/6584027
}
