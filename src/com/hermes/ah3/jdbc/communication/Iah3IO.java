package com.hermes.ah3.jdbc.communication;

import java.util.Map;

public interface Iah3IO {

	/**
	 * 检查到服务端的连接是否还有效，如果无效，重新连接服务端
	 * @return
	 * @throws Exception
	 */
	public abstract boolean checkAndReConnect() throws Exception;

	/**
	 * 向服务端发送SQL语句包，服务端返回的数据存储在Ah3IO内的metaDataMap、resultDataList中
	 * @param sql
	 * @throws Exception
	 */
	public abstract void sendExecuteSQLPackToServer(String sql)
			throws Exception;

	/**
	 * 向服务端发送心跳检测包，如果发生异常或服务端没有响应，关闭客户端socket连接
	 * @param sql
	 * @throws Exception
	 */
	public abstract void sendHeartLinePackToServer() throws Exception;

	/**
	 * socket连接是否已经关闭（包括被服务端关闭）
	 * @return
	 */
	public abstract boolean isClosed();

	/**
	 * 判断结果数据是否还有下一行
	 * @return true:还有下一行数据；false：没有下一行数据
	 */
	public abstract boolean next();

	/**
	 * 通过字段名取字段在当前行的字符串值
	 * @param columnName
	 * @return
	 * @throws Exception
	 */
	public abstract String getString(String columnName) throws Exception;

	/**
	 * 通过索引取字段在当前行的字符串值
	 * @param columnIndex
	 * @return
	 * @throws Exception
	 */
	public abstract String getString(int columnIndex) throws Exception;

	/**
	 * 通过字段名取字段在当前行的long值
	 * @param columnName
	 * @return
	 * @throws Exception
	 */
	public abstract long getLong(String columnName) throws Exception;

	/**
	 * 通过索引取字段在当前行的long值
	 * @param columnIndex
	 * @return
	 * @throws Exception
	 */
	public abstract long getLong(int columnIndex) throws Exception;

	/**
	 * 通过字段名取字段在当前行的short值
	 * @param columnName
	 * @return
	 * @throws Exception
	 */
	public abstract short getShort(String columnName) throws Exception;

	/**
	 * 通过索引取字段在当前行的shrot值
	 * @param columnIndex
	 * @return
	 * @throws Exception
	 */
	public abstract short getShort(int columnIndex) throws Exception;

	/**
	 * 通过字段名取字段在当前行的int值
	 * @param columnName
	 * @return
	 * @throws Exception
	 */
	public abstract int getInt(String columnName) throws Exception;

	/**
	 * 通过索引取字段在当前行的int值
	 * @param columnIndex
	 * @return
	 * @throws Exception
	 */
	public abstract int getInt(int columnIndex) throws Exception;

	/**
	 * 通过字段名取字段在当前行的double值
	 * @param columnName
	 * @return
	 * @throws Exception
	 */
	public abstract double getDouble(String columnName) throws Exception;

	/**
	 * 通过索引取字段在当前行的double值
	 * @param columnIndex
	 * @return
	 * @throws Exception
	 */
	public abstract double getDouble(int columnIndex) throws Exception;

	/**
	 * 获取查询返回结果的元数据信息
	 * @return
	 * @throws Exception
	 */
	public abstract Map<String,ColumnMetaData> getMetaData() throws Exception;
	
	/**
	 * 强制关闭到服务器的连接
	 */
	public abstract void forceClose();

	/**
	 * 释放相关资源，并关闭到服务器的连接
	 */
	public abstract void quit();

}