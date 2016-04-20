/**
 * 
 */
package com.hermes.ah3.jdbc.communication;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.hermes.ah3.jdbc.util.ConvertDataType;

/**
 * @author wuwl
 *
 */
public class Ah3PackReceiver {

	private static Log log = LogFactory.getLog(Ah3PackReceiver.class);
	
	private Socket socket;
	private static int M_MAX_SIZE = 8192;
    private boolean iAmKeepingRunning = true;

    private BufferedInputStream inputStream = null;
    private byte[] remainBuffer = null; //存储未转化的数据流
    private int remainDataLength = 0;   //还未转化为DataPack专用格式的数据流的长度（以字节为单位）
    private byte[] receiverBuffer = null;   //存储接收到的数据流
	
	public Ah3PackReceiver(Socket socket) {
		this.socket = socket;
		this.receiverBuffer = new byte[M_MAX_SIZE];
        this.remainBuffer = new byte[M_MAX_SIZE];
	}
	
	public List<Ah3DataPack> receiverDataFromServer() throws Exception {
		List<Ah3DataPack> retList = new ArrayList<Ah3DataPack>();
		iAmKeepingRunning = true;
		inputStream = new BufferedInputStream(socket.getInputStream());
		Ah3DataPack dataPack;
        while(iAmKeepingRunning){
            int inputDataLen = this.getInputData();
            if (inputDataLen == -1) {
                break; //退出循环
            }
            this.append(inputDataLen);
            //拆分数据包
            while(this.remainDataLength > 0){
                byte[] request = this.getNextRequestData();
                if(request == null) {
                	break;
                }                
                dataPack = new Ah3DataPack(request);
                log.debug("\n<<<<<<<<<<<<<<<<<<<<<<<------------------	Receive DataPack Begin --------------------<<<<<<<<<<<<<<<<<<<<<<<");
                log.debug(dataPack.dumpDataPack());
                log.debug("\n<<<<<<<<<<<<<<<<<<<<<<<------------------	Receive DataPack End ----------------------<<<<<<<<<<<<<<<<<<<<<<<");
                retList.add(dataPack);
            }//while(this.remainDataLength > 0)
        }//while
        
		return retList;
    }

    /**
     * 关闭线程
     */
    public void shutdown(){
        this.iAmKeepingRunning = false;
    }

    /**
     * 取下一个完整的请求数据包的数据,
     * 即包头数据 + 包体数据。
     *　
     * @return 返回请求数据，
     * 如果剩余的数据不是一个完整的请求数据，则返回null.
     */
    private byte[] getNextRequestData(){
         //如果不足一个包头的长度，则返回null
         if(this.remainDataLength < Ah3PackHead.PACK_HEAD_LENGTH)
             return null;
         //取包头数据
         byte[] packHead = new byte[Ah3PackHead.PACK_HEAD_LENGTH];
         System.arraycopy(this.remainBuffer,0,packHead,0,Ah3PackHead.PACK_HEAD_LENGTH);
         //计算下一个完整请求数据包的总长度，
         //如果剩余的数据长度不足下一个完整请求的数据长度，
         //则返回null
         short totalLength = getRequestBodyLength(packHead);
         if(this.remainDataLength < totalLength)
             return null;
         byte[] request = new byte[totalLength];
         //取下一个请求数据包的数据，并移动剩余的数据。
         System.arraycopy(this.remainBuffer,0,request,0,totalLength);
         this.remainDataLength -= totalLength;
         if(this.remainDataLength > 0){
             byte[] tmpBuffer = new byte[this.remainDataLength];
             System.arraycopy(this.remainBuffer,totalLength,tmpBuffer,0,this.remainDataLength);
             System.arraycopy(tmpBuffer,0,this.remainBuffer,0,this.remainDataLength);
         }
         return request;
    }

    /**
     * 取出请求数据报文长度
     * @param packHeadBuffer 请求报文的包头数据
     * @return 请求数据报文长度
     */
    private short getRequestBodyLength(byte[] packHead){
        byte[] buffer=new byte[2];
        buffer[0] = packHead[Ah3PackHead.PACK_HEAD_LENGTH_OFFSET];
        buffer[1] = packHead[Ah3PackHead.PACK_HEAD_LENGTH_OFFSET + 1];
        //当前包体长度
        return(ConvertDataType.toShort(buffer));
    }

    /**
     * 从SOCKET中读取输入数据
     * @return 返回接收数据的字节数
     */
    private int getInputData(){
        int inputDataLen = -1;
        try{
            inputDataLen = this.inputStream.read(this.receiverBuffer,0,
                                       this.receiverBuffer.length);
            if(inputDataLen == -1 || inputDataLen < this.M_MAX_SIZE){
                this.close();
                //return inputDataLen;
            }
            //log.debug(new String(this.receiverBuffer));
        }catch(Exception e){
            log.error("",e);
            this.close();
            return -1;
        }
        return inputDataLen;
    }

    /**
　 　* 把刚从SOCKET中接收到的数据append到
　　 * 未处理的请求数据缓冲区中。
     * @param int appendLength 刚接收数据的长度
     */
    private void append(int appendLength){
        //判断待处理数据缓冲区是否足够大，如果不够，则要增加
        //缓冲区的大小
        if(this.remainBuffer.length < this.remainDataLength + appendLength){
            byte[] tmpBuffer = new byte[this.remainDataLength + appendLength];
            System.arraycopy(this.remainBuffer,0,tmpBuffer,0,this.remainDataLength);
            this.remainBuffer = tmpBuffer;
        }
        System.arraycopy(this.receiverBuffer,
            0,this.remainBuffer,this.remainDataLength,appendLength);
        this.remainDataLength += appendLength;
    }
    
    
    private void close() {
    	this.iAmKeepingRunning = false;
    }
}
