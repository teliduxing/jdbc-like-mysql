/**
 * 
 */
package com.hermes.ah3.jdbc.communication;

import java.util.List;
import java.util.Map;

/**
 * Ah3 Driver与服务器传输信息的包（包含包头、包体）
 * @author wuwl
 *
 */
public class Ah3DataPack {
	
	private Ah3PackHead ah3PackHead;
	private Ah3PackBody ah3PackBody;
	
	public Ah3DataPack() {
		this.ah3PackHead = new Ah3PackHead();
		this.ah3PackBody = new Ah3PackBody(this.ah3PackHead);
	}
	
	/**
	 * 使用一个二进制数组（从客户端或服务端发送来的原始二进制流）初始化包
	 * @param data
	 */
	public Ah3DataPack(byte[] data) {
		//把包头数据放入到包头对象中
		byte[] packHead = new byte[Ah3PackHead.PACK_HEAD_LENGTH];
        System.arraycopy(data,0,packHead,0,Ah3PackHead.PACK_HEAD_LENGTH);
        this.ah3PackHead = new Ah3PackHead(packHead);
        
        int bodyLength = data.length - Ah3PackHead.PACK_HEAD_LENGTH;
        byte[] packBody = new byte[bodyLength];
        System.arraycopy(data,Ah3PackHead.PACK_HEAD_LENGTH - 1,packBody,0,bodyLength);
        this.ah3PackBody = new Ah3PackBody(packBody,this.ah3PackHead);
	}
	
	/**
	 * 设置包的类型
	 * @param packType
	 */
	public void setPackType(byte packType) {
		this.ah3PackHead.setPackType(packType);
	}
	
	/**
	 * 获取包的类型
	 * @return
	 */
	public byte getPackType() {
		return this.ah3PackHead.getPackType();
	}
	
	/**
	 * 设置包体的数据
	 * @param packBody
	 */
	public void setPackBody(String packBody) {
		this.ah3PackBody.setPackBody(packBody);
	}
	
	/**
	 * 设置整个包的数据长度
	 * @param packLength
	 */
	public void setPackLength(int packLength) {
		if (packLength > Short.MAX_VALUE) {
			
		}
		this.ah3PackHead.setTotalLength(Short.parseShort(String.valueOf(packLength)));
	}
	
	/**
	 * 以2进制数组的形式返回包中的数据（包括包头、包体的数据）
	 * @return
	 */
	public byte[] toBytes() {
		byte[] headBytes = this.ah3PackHead.toBytes();
		byte[] bodyBytes = this.ah3PackBody.toBytes();
		byte[] packBytes = new byte[headBytes.length + bodyBytes.length];
		System.arraycopy(headBytes,0,packBytes,0,headBytes.length);
		System.arraycopy(bodyBytes,0,packBytes,headBytes.length - 1,bodyBytes.length);
		return packBytes;
	}

	/**
	 * 获取服务器返回结果中字段的元数据信息
	 * @return map<key=columnName,value=columnDataType>
	 * @return
	 */
	public Map<String,ColumnMetaData> getMetaData() {
		return this.ah3PackBody.getMetaData();
	}
	
	/**
	 * 获取服务器返回结果中每个字段的值（多行）
	 * @return list<以英文逗号分隔的多个字段值的字符串>
	 */
	public List<String> getDataAsList() {
		return this.ah3PackBody.getDataAsList();
	}
	
	/**
	 * 获取服务器返回结果中每个字段的值（多行）
	 * @return String[]
	 */
//	public String[] getDataAsArray() {
//		return this.ah3PackBody.getDataAsArray();
//	}
	
	/**
	 * 以字符串的形式返回包中的数据（包括包头、包体的数据）
	 * @return
	 */
	public String dumpDataPack() {
		StringBuffer tmpSb = new StringBuffer();
		//tmpSb.append("\n+----------------------------------------------DataPack Begin-------------------------------------------------------+");
		tmpSb.append(this.ah3PackHead.dumpPackHead()).append(this.ah3PackBody.dumpPackBody());
		//tmpSb.append("\n+----------------------------------------------DataPack End---------------------------------------------------------+");
		return tmpSb.toString();
	}
	
	/**
	 * @param args
	 */
	public static void main(String[] args) {
		// TODO Auto-generated method stub

	}

}
