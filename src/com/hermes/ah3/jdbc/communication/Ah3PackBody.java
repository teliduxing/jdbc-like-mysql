package com.hermes.ah3.jdbc.communication;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.hermes.ah3.jdbc.Ah3DriverConstant;
import com.hermes.ah3.jdbc.util.ConvertDataType;

/**
 * Ah3 Driver与服务器传输信息的包体
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
	 * 获取服务器返回结果中字段的元数据信息
	 * @return map<key=columnName,value=columnDataType>
	 */
	public Map<String,ColumnMetaData> getMetaData() {
		Map<String,ColumnMetaData> retMap = new HashMap<String,ColumnMetaData>();
		if (this.packBody == null || this.packBody.length() < 1) {
			return retMap;
		}		
		
		byte metaType = this.packHead.getMetaType();
		
		//把返回的数据包体按'\n'分隔
		String[] tmpArr = this.packBody.toString().split("\n");
		int columnCnt = (tmpArr[0].split(",")).length;
		String[] columnNames = new String[columnCnt];
		String[] columnDataTypes = new String[columnCnt];
		
		//根据元数据获取类型判断哪几行是元数据信息
		if (metaType == Ah3DriverConstant.PACK_METATYPE_NEED_COLUMNNAME) {
			columnNames = tmpArr[0].split(","); //字段英文名
		} else if (metaType == Ah3DriverConstant.PACK_METATYPE_NEED_COLUMNTYPE) {
			columnDataTypes = tmpArr[0].split(",");	//字段数据类型
		} else if (metaType == Ah3DriverConstant.PACK_METATYPE_NEED_BOTH) {
			columnNames = tmpArr[0].split(","); //字段英文名
			columnDataTypes = tmpArr[1].split(",");	//字段数据类型
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
	 * 获取服务器返回结果中每个字段的值（多行）
	 * @return list<以英文逗号分隔的多个字段值的字符串>
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
			//第1、2行是列的元数据信息，第3行开始才是数据
			dataIndex = 2;
		} else if (metaType == Ah3DriverConstant.PACK_METATYPE_NEED_COLUMNNAME || metaType == Ah3DriverConstant.PACK_METATYPE_NEED_COLUMNTYPE) {
			//第1行是元数据信息，第2行开始是数据
			dataIndex = 1;			
		} else {
			//返回数据中没有元数据信息，第1行开始就是数据
			dataIndex = 0;
		}
		
		for(int i = dataIndex; i < tmpArr.length; i++) {
			retList.add(tmpArr[i]);
		}
		return retList;
	}
	
	/**
	 * 获取服务器返回结果中每个字段的值（多行）
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
