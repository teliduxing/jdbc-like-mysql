/**
 * 
 */
package com.hermes.ah3.jdbc.communication.implc;

/**
 * @author wuwl
 *
 */
public class ah3dri {
	
	static{
		System.out.println(System.getProperty("java.library.path"));
		System.loadLibrary("ah3dri");
	}

	public native int JInit();
	public native int JAuthenticate(String serverIP, String serverPort, String username, String userPwd);
	public native int JConn();
	public native int JSqlcode();
	public native int JQuery(String expr);
	public native int JFetch();
	public native String JFldname(int colid);
	public native int JFldid(String colname);
	public native String JGetLine();
	public native short JGetShortFid(int colid);
	public native int JGetIntFid(int colid);
	public native double JGetDoubleFid(int colid);
	public native String JGetStringFid(int colid);
	public native short JGetShortFname(String colname);
	public native int JGetIntFname(String colname);
	public native double JGetDoubleFname(String colname);
	public native String JGetStringFname(String colname);
	public native String JGetCStringFname(String colname);
	public native int JClose();
	public native int JDestory();


	/**
	 * 
	 */
	public ah3dri() {
		// TODO Auto-generated constructor stub
	}

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		// TODO Auto-generated method stub

	}

}
