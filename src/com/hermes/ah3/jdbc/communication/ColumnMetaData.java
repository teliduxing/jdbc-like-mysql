/**
 * 
 */
package com.hermes.ah3.jdbc.communication;

/**
 * �ֶ�Ԫ������Ϣ��
 * @author wuwl
 *
 */
public class ColumnMetaData {
	
	private String dataType;
	private int index;
	private String columnName;
	
	public String getColumnName() {
		return columnName;
	}
	public void setColumnName(String columnName) {
		this.columnName = columnName;
	}
	public String getDataType() {
		return dataType;
	}
	public void setDataType(String dataType) {
		this.dataType = dataType;
	}
	public int getIndex() {
		return index;
	}
	public void setIndex(int index) {
		this.index = index;
	}
	
	
}
