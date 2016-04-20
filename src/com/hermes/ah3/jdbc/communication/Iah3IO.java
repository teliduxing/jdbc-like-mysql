package com.hermes.ah3.jdbc.communication;

import java.util.Map;

public interface Iah3IO {

	/**
	 * ��鵽����˵������Ƿ���Ч�������Ч���������ӷ����
	 * @return
	 * @throws Exception
	 */
	public abstract boolean checkAndReConnect() throws Exception;

	/**
	 * �����˷���SQL����������˷��ص����ݴ洢��Ah3IO�ڵ�metaDataMap��resultDataList��
	 * @param sql
	 * @throws Exception
	 */
	public abstract void sendExecuteSQLPackToServer(String sql)
			throws Exception;

	/**
	 * �����˷���������������������쳣������û����Ӧ���رտͻ���socket����
	 * @param sql
	 * @throws Exception
	 */
	public abstract void sendHeartLinePackToServer() throws Exception;

	/**
	 * socket�����Ƿ��Ѿ��رգ�����������˹رգ�
	 * @return
	 */
	public abstract boolean isClosed();

	/**
	 * �жϽ�������Ƿ�����һ��
	 * @return true:������һ�����ݣ�false��û����һ������
	 */
	public abstract boolean next();

	/**
	 * ͨ���ֶ���ȡ�ֶ��ڵ�ǰ�е��ַ���ֵ
	 * @param columnName
	 * @return
	 * @throws Exception
	 */
	public abstract String getString(String columnName) throws Exception;

	/**
	 * ͨ������ȡ�ֶ��ڵ�ǰ�е��ַ���ֵ
	 * @param columnIndex
	 * @return
	 * @throws Exception
	 */
	public abstract String getString(int columnIndex) throws Exception;

	/**
	 * ͨ���ֶ���ȡ�ֶ��ڵ�ǰ�е�longֵ
	 * @param columnName
	 * @return
	 * @throws Exception
	 */
	public abstract long getLong(String columnName) throws Exception;

	/**
	 * ͨ������ȡ�ֶ��ڵ�ǰ�е�longֵ
	 * @param columnIndex
	 * @return
	 * @throws Exception
	 */
	public abstract long getLong(int columnIndex) throws Exception;

	/**
	 * ͨ���ֶ���ȡ�ֶ��ڵ�ǰ�е�shortֵ
	 * @param columnName
	 * @return
	 * @throws Exception
	 */
	public abstract short getShort(String columnName) throws Exception;

	/**
	 * ͨ������ȡ�ֶ��ڵ�ǰ�е�shrotֵ
	 * @param columnIndex
	 * @return
	 * @throws Exception
	 */
	public abstract short getShort(int columnIndex) throws Exception;

	/**
	 * ͨ���ֶ���ȡ�ֶ��ڵ�ǰ�е�intֵ
	 * @param columnName
	 * @return
	 * @throws Exception
	 */
	public abstract int getInt(String columnName) throws Exception;

	/**
	 * ͨ������ȡ�ֶ��ڵ�ǰ�е�intֵ
	 * @param columnIndex
	 * @return
	 * @throws Exception
	 */
	public abstract int getInt(int columnIndex) throws Exception;

	/**
	 * ͨ���ֶ���ȡ�ֶ��ڵ�ǰ�е�doubleֵ
	 * @param columnName
	 * @return
	 * @throws Exception
	 */
	public abstract double getDouble(String columnName) throws Exception;

	/**
	 * ͨ������ȡ�ֶ��ڵ�ǰ�е�doubleֵ
	 * @param columnIndex
	 * @return
	 * @throws Exception
	 */
	public abstract double getDouble(int columnIndex) throws Exception;

	/**
	 * ��ȡ��ѯ���ؽ����Ԫ������Ϣ
	 * @return
	 * @throws Exception
	 */
	public abstract Map<String,ColumnMetaData> getMetaData() throws Exception;
	
	/**
	 * ǿ�ƹرյ�������������
	 */
	public abstract void forceClose();

	/**
	 * �ͷ������Դ�����رյ�������������
	 */
	public abstract void quit();

}