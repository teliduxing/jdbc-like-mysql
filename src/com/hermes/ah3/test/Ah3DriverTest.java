/********************************************************************************************
libah3dri.so����JDK��binĿ¼��/usr/java/jdk1.6.0_31/jre/bin��

AH3Driver.jar,commons-logging-1.1.1.jar,log4j-1.2.17.jar����/home/xielj/test-driver
java -Djava.ext.dirs=/home/xielj/test-driver com.hermes.ah3.test.Ah3DriverTest

Ah3DriverTest.javaʹ��/home/xielj/lib�µ�libah3dri.so
********************************************************************************************/

package com.hermes.ah3.test;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.Statement;

import java.sql.ResultSetMetaData;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.hermes.ah3.jdbc.communication.implc.ah3dri;

/**
 * @author wuwl
 *
 */
public class Ah3DriverTest 
{
	private static Log log = LogFactory.getLog(Ah3DriverTest.class);

	public Ah3DriverTest() {
		// TODO Auto-generated constructor stub
	}

	public static void main(String[] args) {
		String url = "jdbc:hermes://10.1.251.94:9087/test";
	    String username = "xielj";
	    String password = "xielj";
		try {
			Class.forName("com.hermes.ah3.jdbc.Driver");
			Connection con = DriverManager.getConnection(url, username, password);   
			
			long t1,t2,avg = 0;
			//long[] avgs = new long[10];
			for(int i = 0; i < 10; i++) {
				t1 = System.currentTimeMillis();
				exec(con, "select * from staff");
				//exec(con, "get 'user 439228822168888'");
				exec(con, "select brand,sum(age),sum(net_flow) from gsm_data where not(in(name,'杨丽')) and flow_3g>=200 and flow_3g<=400  group by $1 order by $1");
			
				t2 = System.currentTimeMillis();
				avg  = avg + (t2 - t1);
			}
			
			System.out.println("Used Mills: " + avg/10);
			
			con.close();
		} catch (Exception e) {
			System.out.println(e);
			log.error("", e);
		}
	}
	
	private static void exec(Connection con,String sql) throws Exception 
	{
		Statement stmt = con.createStatement() ; 

		System.out.println("\nSQL:"+sql);
		try {
			ResultSet rs = stmt.executeQuery(sql);
		
			ResultSetMetaData md = rs.getMetaData();
			for (int i=0; i<md.getColumnCount(); i++) {
				System.out.println("i:"+i+"\tColname:" + md.getColumnName(i)+",\tdataType: " + md.getColumnType(i) + ";");
			}
			System.out.println("");
		
			while (rs.next()) {
				//System.out.println(", ename:" + rs.getString("ename"));
				System.out.print(rs.getString(0));
				System.out.println(rs.getString(1));
			}
		} catch (Exception e) {
			System.out.println(e);
			log.error("", e);			
		}
	}
}
