/**
 * 
 */
package com.hermes.ah3.jdbc.communication;

import java.util.Map;
import java.util.Properties;

import com.hermes.ah3.jdbc.Ah3DriverException;
import com.hermes.ah3.jdbc.communication.implc.ah3dri;

/**
 * @author wuwl
 *
 */
public class Ah3IOImplByC implements Iah3IO {
	
	private ah3dri ah3driByC = null;
	
	public Ah3IOImplByC(String serverHost, int serverPort,String userName,String userPwd,Properties prop) throws Exception {
		this.ah3driByC = new ah3dri();
		System.out.println(">>>>>>>>>>After new ah3dri(), used Mem:" + this.getUsedMem());
		this.ah3driByC.JInit();
		System.out.println(">>>>>>>>>>After JInit(), used Mem:" + this.getUsedMem());
		int ret = this.ah3driByC.JAuthenticate(serverHost, String.valueOf(serverPort), userName, userPwd);
		System.out.println(">>>>>>>>>>After JAuthenticate(), used Mem:" + this.getUsedMem());
		if (ret > 0) {
			throw new Ah3DriverException("连接数据库的用户名或密码错误，" + getSqlCodeDesc(this.ah3driByC.JSqlcode()));
		}
	}

	/* (non-Javadoc)
	 * @see com.hermes.ah3.jdbc.communication.Iah3IO#checkAndReConnect()
	 */
	@Override
	public boolean checkAndReConnect() throws Exception {
		return true;
	}

	/* (non-Javadoc)
	 * @see com.hermes.ah3.jdbc.communication.Iah3IO#sendExecuteSQLPackToServer(java.lang.String)
	 */
	@Override
	public void sendExecuteSQLPackToServer(String sql) throws Exception {
		System.out.println(">>>>>>>>>>Before JQuery(), used Mem:" + this.getUsedMem());
		int ret = this.ah3driByC.JQuery(sql);
		System.out.println(">>>>>>>>>>After JQuery(), used Mem:" + this.getUsedMem());
		if (ret < 0) {
			throw new Ah3DriverException("数据库执行sql[" + sql + "]失败，" + getSqlCodeDesc(this.ah3driByC.JSqlcode()));
		}
	}

	/* (non-Javadoc)
	 * @see com.hermes.ah3.jdbc.communication.Iah3IO#sendHeartLinePackToServer()
	 */
	@Override
	public void sendHeartLinePackToServer() throws Exception {
		// TODO Auto-generated method stub

	}

	/* (non-Javadoc)
	 * @see com.hermes.ah3.jdbc.communication.Iah3IO#isClosed()
	 */
	@Override
	public boolean isClosed() {
		// TODO Auto-generated method stub
		return false;
	}

	/* (non-Javadoc)
	 * @see com.hermes.ah3.jdbc.communication.Iah3IO#next()
	 */
	@Override
	public boolean next() {
		boolean retBool = false;
		System.out.println(">>>>>>>>>>Before JFetch(), used Mem:" + this.getUsedMem());
		int ret = this.ah3driByC.JFetch();
		System.out.println(">>>>>>>>>>After JFetch(), used Mem:" + this.getUsedMem());
		if (ret > 0) {
			retBool = true;
			//System.out.println(this.ah3driByC.JGetLine());
		}
		return retBool;
	}

	/* (non-Javadoc)
	 * @see com.hermes.ah3.jdbc.communication.Iah3IO#getString(java.lang.String)
	 */
	@Override
	public String getString(String columnName) throws Exception {
//		System.out.println(">>>>>>>>>>Before JGetCStringFname(), used Mem:" + this.getUsedMem());
//		String ret = this.ah3driByC.JGetCStringFname(columnName);
		String ret = this.ah3driByC.JGetStringFname(columnName);
//		System.out.println(">>>>>>>>>>After JGetCStringFname(), used Mem:" + this.getUsedMem());
		return ret;
	}

	/* (non-Javadoc)
	 * @see com.hermes.ah3.jdbc.communication.Iah3IO#getString(int)
	 */
	@Override
	public String getString(int columnIndex) throws Exception {
		return this.ah3driByC.JGetStringFid(columnIndex);
	}

	/* (non-Javadoc)
	 * @see com.hermes.ah3.jdbc.communication.Iah3IO#getLong(java.lang.String)
	 */
	@Override
	public long getLong(String columnName) throws Exception {
		// TODO Auto-generated method stub
		return 0;
	}

	/* (non-Javadoc)
	 * @see com.hermes.ah3.jdbc.communication.Iah3IO#getLong(int)
	 */
	@Override
	public long getLong(int columnIndex) throws Exception {
		// TODO Auto-generated method stub
		return 0;
	}

	/* (non-Javadoc)
	 * @see com.hermes.ah3.jdbc.communication.Iah3IO#getShort(java.lang.String)
	 */
	@Override
	public short getShort(String columnName) throws Exception {
		return this.ah3driByC.JGetShortFname(columnName);
	}

	/* (non-Javadoc)
	 * @see com.hermes.ah3.jdbc.communication.Iah3IO#getShort(int)
	 */
	@Override
	public short getShort(int columnIndex) throws Exception {
		return this.ah3driByC.JGetShortFid(columnIndex);
	}

	/* (non-Javadoc)
	 * @see com.hermes.ah3.jdbc.communication.Iah3IO#getInt(java.lang.String)
	 */
	@Override
	public int getInt(String columnName) throws Exception {

		return this.ah3driByC.JGetIntFname(columnName);
	}

	/* (non-Javadoc)
	 * @see com.hermes.ah3.jdbc.communication.Iah3IO#getInt(int)
	 */
	@Override
	public int getInt(int columnIndex) throws Exception {

		return this.ah3driByC.JGetIntFid(columnIndex);
	}

	/* (non-Javadoc)
	 * @see com.hermes.ah3.jdbc.communication.Iah3IO#getDouble(java.lang.String)
	 */
	@Override
	public double getDouble(String columnName) throws Exception {

		return this.ah3driByC.JGetDoubleFname(columnName);
	}

	/* (non-Javadoc)
	 * @see com.hermes.ah3.jdbc.communication.Iah3IO#getDouble(int)
	 */
	@Override
	public double getDouble(int columnIndex) throws Exception {

		return this.ah3driByC.JGetDoubleFid(columnIndex);
	}

	/* (non-Javadoc)
	 * @see com.hermes.ah3.jdbc.communication.Iah3IO#forceClose()
	 */
	@Override
	public void forceClose() {
		this.ah3driByC.JClose();
		this.ah3driByC.JDestory();
	}

	/* (non-Javadoc)
	 * @see com.hermes.ah3.jdbc.communication.Iah3IO#quit()
	 */
	@Override
	public void quit() {
		this.forceClose();
	}
	
	private String getSqlCodeDesc(int sqlCode) {
		String desc;
		if (sqlCode == 0) {
			desc = "SQLCODE:" + sqlCode + "，错误描述：和服务器通信中断,查询未能执行。";
		} else {
			desc = "SQLCODE:" + sqlCode + "，错误描述：查询执行失败,请确认查询对象、语法是否正确。";
		}
		return desc;
	}
	
	/**
	 * 获取系统中已被使用的内存数
	 * @return
	 */
	private long getUsedMem() {
		return Runtime.getRuntime().totalMemory() - Runtime.getRuntime().freeMemory();
	}

	@Override
	public Map<String,ColumnMetaData> getMetaData() throws Exception {
		// TODO Auto-generated method stub
		return null;
	}

}
