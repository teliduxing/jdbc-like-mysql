/**
 * 
 */
package com.hermes.ah3.jdbc.communication;

import java.util.List;
import java.util.Map;

/**
 * Ah3 Driver�������������Ϣ�İ���������ͷ�����壩
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
	 * ʹ��һ�����������飨�ӿͻ��˻����˷�������ԭʼ������������ʼ����
	 * @param data
	 */
	public Ah3DataPack(byte[] data) {
		//�Ѱ�ͷ���ݷ��뵽��ͷ������
		byte[] packHead = new byte[Ah3PackHead.PACK_HEAD_LENGTH];
        System.arraycopy(data,0,packHead,0,Ah3PackHead.PACK_HEAD_LENGTH);
        this.ah3PackHead = new Ah3PackHead(packHead);
        
        int bodyLength = data.length - Ah3PackHead.PACK_HEAD_LENGTH;
        byte[] packBody = new byte[bodyLength];
        System.arraycopy(data,Ah3PackHead.PACK_HEAD_LENGTH - 1,packBody,0,bodyLength);
        this.ah3PackBody = new Ah3PackBody(packBody,this.ah3PackHead);
	}
	
	/**
	 * ���ð�������
	 * @param packType
	 */
	public void setPackType(byte packType) {
		this.ah3PackHead.setPackType(packType);
	}
	
	/**
	 * ��ȡ��������
	 * @return
	 */
	public byte getPackType() {
		return this.ah3PackHead.getPackType();
	}
	
	/**
	 * ���ð��������
	 * @param packBody
	 */
	public void setPackBody(String packBody) {
		this.ah3PackBody.setPackBody(packBody);
	}
	
	/**
	 * ���������������ݳ���
	 * @param packLength
	 */
	public void setPackLength(int packLength) {
		if (packLength > Short.MAX_VALUE) {
			
		}
		this.ah3PackHead.setTotalLength(Short.parseShort(String.valueOf(packLength)));
	}
	
	/**
	 * ��2�����������ʽ���ذ��е����ݣ�������ͷ����������ݣ�
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
	 * ��ȡ���������ؽ�����ֶε�Ԫ������Ϣ
	 * @return map<key=columnName,value=columnDataType>
	 * @return
	 */
	public Map<String,ColumnMetaData> getMetaData() {
		return this.ah3PackBody.getMetaData();
	}
	
	/**
	 * ��ȡ���������ؽ����ÿ���ֶε�ֵ�����У�
	 * @return list<��Ӣ�Ķ��ŷָ��Ķ���ֶ�ֵ���ַ���>
	 */
	public List<String> getDataAsList() {
		return this.ah3PackBody.getDataAsList();
	}
	
	/**
	 * ��ȡ���������ؽ����ÿ���ֶε�ֵ�����У�
	 * @return String[]
	 */
//	public String[] getDataAsArray() {
//		return this.ah3PackBody.getDataAsArray();
//	}
	
	/**
	 * ���ַ�������ʽ���ذ��е����ݣ�������ͷ����������ݣ�
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
