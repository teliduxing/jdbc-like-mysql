package com.hermes.ah3.jdbc.communication;

import com.hermes.ah3.jdbc.Ah3DriverConstant;
import com.hermes.ah3.jdbc.util.ConvertDataType;

/**
 * Ah3 Driver与服务器传输信息的包头
 * @author wuwl
 *
 */
public class Ah3PackHead {
	//内容固定为0x01，指示数据为ASCII编码的字符文本
	private byte version;
	private static final int VERSION_FLAG_OFFSET = 0;
	//private static final int VERSION_FLAG_LENGTH = 1;
	
	//内容为SQL请求-3、SQL响应-3、登录请求-1、心跳-2
	private byte packType;
	private static final int PACKTYPE_FLAG_OFFSET = 0;
	//private static final int PACKTYPE_FLAG_LENGTH = 1;
	
	//指示是否需要元数据
	private byte metaType;
	private static final int METATYPE_FLAG_OFFSET = 1;
	//private static final int METATYPE_FLAG_LENGTH = 1;
	
	//由server端填写，指示1-头包、2-中间包、3-尾包
	private byte packageSeqno;
	private static final int PACKAGESEQNO_FLAG_OFFSET = 1;
	//private static final int PACKAGESEQNO_FLAG_LENGTH = 1;
	
	//由server端填写，指示查询执行成功与否
	private byte returnCode;
	private static final int RETURNCODE_FLAG_OFFSET = 2;
	private static final int RETURNCODE_FLAG_LENGTH = 1;
	
	//内容由客户端决定
	private byte threadId;
	private static final int THREADID_FLAG_OFFSET = 3;
	private static final int THREADID_FLAG_LENGTH = 1;
	
	//暂时未定义
	private byte requestId;
	private static final int REQUESTID_FLAG_OFFSET = 4;
	private static final int REQUESTID_FLAG_LENGTH = 1;
	
	//数值为包头长度加后续数据长度
	private short totalLength;
	private static final int TOTALLENTH_FLAG_OFFSET = 5;
	private static final int TOTALLENTH_FLAG_LENGTH = 2;
	
	private byte splitChar;
	private static final int SPLITCHAR_FLAG_OFFSET = 7;
	private static final int SPLITCHAR_FLAG_LENGTH = 1;
	
	//整个包头的长度，8byte
	public static final int PACK_HEAD_LENGTH = 8;
	
	//标示整个数据包长度的变量在包头中的偏移量
	public static final int PACK_HEAD_LENGTH_OFFSET = 5;
	
	//存储所有属性二进制编码的容器
	private byte[] headBuffer = null; 
	
	public Ah3PackHead() {
		this.headBuffer = new byte[PACK_HEAD_LENGTH];
		
		this.setVersion(Ah3DriverConstant.PACK_VERSION);
		this.setMetaType(Ah3DriverConstant.PACK_METATYPE_NEED_BOTH);
		this.setPackageSeqno(Ah3DriverConstant.PACK_SEQNO_HEAD);
		this.setReturnCode(Ah3DriverConstant.PACK_RETURNCODE_SUCCESS);
		this.setThreadId(Ah3DriverConstant.PACK_THREADID_DEFAULT);
		this.setRequestId(Ah3DriverConstant.PACK_REQUESTID_DEFAULT);
		this.setSplitChar(Ah3DriverConstant.SPLITCHAR_DEFAULT);
	}
	
	/**
	 * 用标准格式的二进制数据初始化包头
	 * @param headBytes
	 */
	public Ah3PackHead(byte[] headBytes) {
		this.headBuffer = headBytes;
		
		byte[] tmpBytes = new byte[1];
		//System.arraycopy(headBytes,VERSION_FLAG_OFFSET,tmpBytes,0,(int)VERSION_FLAG_LENGTH);
		//this.version = tmpBytes[0];
		this.version = (byte)((headBytes[0] & 0x0F));
		
		tmpBytes = new byte[1];
		//System.arraycopy(headBytes,PACKTYPE_FLAG_OFFSET,tmpBytes,0,(int)PACKTYPE_FLAG_LENGTH);
		//this.packType = tmpBytes[0];
		this.packType = (byte)((headBytes[0] & 0xF0) >>> 4);
		
		tmpBytes = new byte[1];
		//System.arraycopy(headBytes,METATYPE_FLAG_OFFSET,tmpBytes,0,(int)METATYPE_FLAG_LENGTH);
		//this.metaType = tmpBytes[0];
		this.metaType = (byte)((headBytes[1] & 0x0F));
		
		tmpBytes = new byte[1];
		//System.arraycopy(headBytes,PACKAGESEQNO_FLAG_OFFSET,tmpBytes,0,(int)PACKAGESEQNO_FLAG_LENGTH);
		//this.packageSeqno = tmpBytes[0];
		this.packageSeqno = (byte)((headBytes[1] & 0xF0) >>> 4);
		
		tmpBytes = new byte[1];
		System.arraycopy(headBytes,RETURNCODE_FLAG_OFFSET,tmpBytes,0,(int)RETURNCODE_FLAG_LENGTH);
		this.packageSeqno = tmpBytes[0];
		
		tmpBytes = new byte[1];
		System.arraycopy(headBytes,THREADID_FLAG_OFFSET,tmpBytes,0,(int)THREADID_FLAG_LENGTH);
		//this.threadId = ConvertDataType.toString(tmpBytes).charAt(0);
		this.threadId = tmpBytes[0];
		
		tmpBytes = new byte[1];
		System.arraycopy(headBytes,REQUESTID_FLAG_OFFSET,tmpBytes,0,(int)REQUESTID_FLAG_LENGTH);
		//this.requestId = ConvertDataType.toString(tmpBytes).charAt(0);
		this.requestId = tmpBytes[0];
		
		tmpBytes = new byte[2];
		System.arraycopy(headBytes,TOTALLENTH_FLAG_OFFSET,tmpBytes,0,(int)TOTALLENTH_FLAG_LENGTH);
		this.totalLength = ConvertDataType.toShort(tmpBytes);
	}
	
	public byte getVersion() {
		return version;
	}
	public void setVersion(byte version) {
		this.version = version;
		//byte[] tmpBytes = new byte[]{version};
        //System.arraycopy(tmpBytes,0,headBuffer,(int)VERSION_FLAG_OFFSET,(int)VERSION_FLAG_LENGTH);
		headBuffer[0] = (byte)(version & 0x0F);
	}
	public byte getPackType() {
		return packType;
	}
	public void setPackType(byte packType) {
		this.packType = packType;
		//byte[] tmpBytes = new byte[]{packType};
		//System.arraycopy(tmpBytes,0,headBuffer,(int)PACKTYPE_FLAG_OFFSET,(int)PACKTYPE_FLAG_LENGTH);
		headBuffer[0] |= (byte)((packType & 0x0F) << 4);
	}
	public byte getMetaType() {
		return metaType;
	}
	public void setMetaType(byte metaType) {
		this.metaType = metaType;
		//byte[] tmpBytes = new byte[]{metaType};
		//System.arraycopy(tmpBytes,0,headBuffer,(int)METATYPE_FLAG_OFFSET,(int)METATYPE_FLAG_LENGTH);
		headBuffer[1] = (byte)(metaType & 0x0F);
	}
	public byte getPackageSeqno() {
		return packageSeqno;
	}
	public void setPackageSeqno(byte packageSeqno) {
		this.packageSeqno = packageSeqno;
		//byte[] tmpBytes = new byte[]{packageSeqno};
		//System.arraycopy(tmpBytes,0,headBuffer,(int)PACKAGESEQNO_FLAG_OFFSET,(int)PACKAGESEQNO_FLAG_LENGTH);
		headBuffer[1] |= (byte)((packageSeqno & 0x0F) << 4);
	}
	public byte getReturnCode() {
		return returnCode;
	}
	public void setReturnCode(byte returnCode) {
		this.returnCode = returnCode;
		byte[] tmpBytes = new byte[]{returnCode};
		System.arraycopy(tmpBytes,0,headBuffer,(int)RETURNCODE_FLAG_OFFSET,(int)RETURNCODE_FLAG_LENGTH);
	}
	public byte getThreadId() {
		return threadId;
	}
	public void setThreadId(byte threadId) {
		this.threadId = threadId;
		byte[] tmpBytes = new byte[]{threadId};
		System.arraycopy(tmpBytes,0,headBuffer,(int)THREADID_FLAG_OFFSET,(int)THREADID_FLAG_LENGTH);
	}
	public byte getRequestId() {
		return requestId;
	}
	public void setRequestId(byte requestId) {
		this.requestId = requestId;
		byte[] tmpBytes = new byte[]{requestId};
		System.arraycopy(tmpBytes,0,headBuffer,(int)REQUESTID_FLAG_OFFSET,(int)REQUESTID_FLAG_LENGTH);
	}
	public short getTotalLength() {
		return totalLength;
	}
	public void setTotalLength(short totalLength) {
		this.totalLength = totalLength;
		byte[] tmpBytes = ConvertDataType.toBytes(totalLength);
		System.arraycopy(tmpBytes,0,headBuffer,(int)TOTALLENTH_FLAG_OFFSET,(int)TOTALLENTH_FLAG_LENGTH);
	}
	public byte getSplitChar() {
		return this.splitChar;
	}
	public void setSplitChar(byte splitChar) {
		this.splitChar = splitChar;
		byte[] tmpBytes = new byte[]{splitChar};
		System.arraycopy(tmpBytes,0,headBuffer,(int)SPLITCHAR_FLAG_OFFSET,(int)SPLITCHAR_FLAG_LENGTH);
	}
	
	/**
     * 返回代表该数据包的字节数组
     * @return 代表数据包的字节数组
     */
    public byte[] toBytes(){
            return headBuffer;
    }

    /**
     * 返回代表数据包的字符串
     * @return 代表数据包的字符串
     */
    public String dumpPackHead(){
    	StringBuffer tmpSb = new StringBuffer();
    	tmpSb.append("\n|----------------------------PackHead-----------------------------------------------------------------------\n|Version:")
    		.append(ConvertDataType.toString(this.getVersion()))
    		.append(", PackType:").append(ConvertDataType.toString(this.getPackType()))
    		.append(", MetaType:").append(ConvertDataType.toString(this.getMetaType()))
    		.append(", PackageSeqno:").append(ConvertDataType.toString(this.getPackageSeqno()))
    		.append(", ReturnCode:").append(ConvertDataType.toString(this.getReturnCode()))
    		.append(", ThreadId:").append(ConvertDataType.toString(this.getThreadId()))
    		.append(", RequestId:").append(ConvertDataType.toString(this.getRequestId()))
    		.append(", TotalLength:").append(ConvertDataType.toString(this.getTotalLength()));
    		//.append(",Head:").append(ConvertDataType.toString(this.headBuffer));
    	
        return(tmpSb.toString());
    }
    
    public static void main(String[] args) {
    	byte b = 0x00;
    	byte v = 0x01;
    	byte t = 0x03;
    	b = (byte)(v & 0x0F);
    	b |= (byte)((t & 0x0F) << 4);
    	System.out.println(Integer.toBinaryString(b));
    }
}
