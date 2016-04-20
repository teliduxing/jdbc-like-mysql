package com.hermes.ah3.jdbc.communication;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.hermes.ah3.jdbc.Ah3DriverConstant;
import com.hermes.ah3.jdbc.util.ConvertDataType;

/**
 * Ah3 Driver�������������Ϣ�İ���
 * @author wuwl
 *
 */
public class Ah3PackBody {

	private StringBuffer packBody;
	private Ah3PackHead packHead;
	
	private byte[] bodyBuffer = null;
	
	public Ah3PackBody(Ah3PackHead packHead) {
		this.packHead = packHead;
		packBody = new StringBuffer();
	}
	
	public Ah3PackBody(byte[] bodyBytes,Ah3PackHead packHead) {
		this.packHead = packHead;
		packBody = new StringBuffer();
		this.bodyBuffer = bodyBytes;
		packBody.append(ConvertDataType.toString(this.bodyBuffer));
	}
	
	public void setPackBody(String body) {
		this.packBody.append(body);
		bodyBuffer = new byte[body.length()];
		System.arraycopy(body.getBytes(),0,bodyBuffer,0,body.length());
	}
	
	public byte[] toBytes() {
		return this.bodyBuffer;
	}
	
	public String dumpPackBody() {
		StringBuffer sb = new StringBuffer();
		sb.append("\n|----------------------------PackBody-----------------------------------------------------------------------\n|")
			.append(this.packBody.toString())
			.append("\n|----------------------------PackBody-----------------------------------------------------------------------");
		
		return sb.toString();
	}
	
	/**
	 * ��ȡ���������ؽ�����ֶε�Ԫ������Ϣ
	 * @return map<key=columnName,value=columnDataType>
	 */
	public Map<String,ColumnMetaData> getMetaData() {
		Map<String,ColumnMetaData> retMap = new HashMap<String,ColumnMetaData>();
		if (this.packBody == null || this.packBody.length() < 1) {
			return retMap;
		}		
		
		byte metaType = this.packHead.getMetaType();
		
		//�ѷ��ص����ݰ��尴'\n'�ָ�
		String[] tmpArr = this.packBody.toString().split("\n");
		int columnCnt = (tmpArr[0].split(",")).length;
		String[] columnNames = new String[columnCnt];
		String[] columnDataTypes = new String[columnCnt];
		
		//����Ԫ���ݻ�ȡ�����ж��ļ�����Ԫ������Ϣ
		if (metaType == Ah3DriverConstant.PACK_METATYPE_NEED_COLUMNNAME) {
			columnNames = tmpArr[0].split(","); //�ֶ�Ӣ����
		} else if (metaType == Ah3DriverConstant.PACK_METATYPE_NEED_COLUMNTYPE) {
			columnDataTypes = tmpArr[0].split(",");	//�ֶ���������
		} else if (metaType == Ah3DriverConstant.PACK_METATYPE_NEED_BOTH) {
			columnNames = tmpArr[0].split(","); //�ֶ�Ӣ����
			columnDataTypes = tmpArr[1].split(",");	//�ֶ���������
		}
		ColumnMetaData tmpMD;
		for(int i = 0; i < columnNames.length; i++) {
			tmpMD = new ColumnMetaData();
			tmpMD.setDataType(columnDataTypes[i]);
			tmpMD.setIndex(i);
			tmpMD.setColumnName(columnNames[i].trim().toLowerCase());
			retMap.put(columnNames[i].trim().toLowerCase(), tmpMD);
		}
		
		return retMap;
	}
	
	/**
	 * ��ȡ���������ؽ����ÿ���ֶε�ֵ�����У�
	 * @return list<��Ӣ�Ķ��ŷָ��Ķ���ֶ�ֵ���ַ���>
	 */
	public List<String> getDataAsList() {
		List<String> retList = new ArrayList<String>();
		
		if (this.packBody == null || this.packBody.length() < 1) {
			return retList;
		}
		
		String[] tmpArr = this.packBody.toString().split("\n");		
		byte metaType = this.packHead.getMetaType();
		
		int dataIndex = 0;
		if (metaType == Ah3DriverConstant.PACK_METATYPE_NEED_BOTH) {
			//��1��2�����е�Ԫ������Ϣ����3�п�ʼ��������
			dataIndex = 2;
		} else if (metaType == Ah3DriverConstant.PACK_METATYPE_NEED_COLUMNNAME || metaType == Ah3DriverConstant.PACK_METATYPE_NEED_COLUMNTYPE) {
			//��1����Ԫ������Ϣ����2�п�ʼ������
			dataIndex = 1;			
		} else {
			//����������û��Ԫ������Ϣ����1�п�ʼ��������
			dataIndex = 0;
		}
		
		for(int i = dataIndex; i < tmpArr.length; i++) {
			retList.add(tmpArr[i]);
		}
		return retList;
	}
	
	/**
	 * ��ȡ���������ؽ����ÿ���ֶε�ֵ�����У�
	 * @return String[]
	 */
//	private String[] getDataAsArray() {
//		if (this.packBody == null || this.packBody.length() < 1) {
//			return null;
//		}
//		
//		String[] tmpArr = this.packBody.toString().split("\n");
//		String[] retArr;
//		if (tmpArr.length > 2) {
//			retArr = new String[tmpArr.length - 2];
//			for(int i = 2; i < tmpArr.length; i++) {
//				retArr[i - 2 ] = tmpArr[i];
//			}
//		} else {
//			retArr = tmpArr;
//		}
//		return retArr;
//	}
}
