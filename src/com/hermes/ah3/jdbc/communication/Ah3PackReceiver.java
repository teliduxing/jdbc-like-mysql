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
    private byte[] remainBuffer = null; //�洢δת����������
    private int remainDataLength = 0;   //��δת��ΪDataPackר�ø�ʽ���������ĳ��ȣ����ֽ�Ϊ��λ��
    private byte[] receiverBuffer = null;   //�洢���յ���������
	
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
                break; //�˳�ѭ��
            }
            this.append(inputDataLen);
            //������ݰ�
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
     * �ر��߳�
     */
    public void shutdown(){
        this.iAmKeepingRunning = false;
    }

    /**
     * ȡ��һ���������������ݰ�������,
     * ����ͷ���� + �������ݡ�
     *��
     * @return �����������ݣ�
     * ���ʣ������ݲ���һ���������������ݣ��򷵻�null.
     */
    private byte[] getNextRequestData(){
         //�������һ����ͷ�ĳ��ȣ��򷵻�null
         if(this.remainDataLength < Ah3PackHead.PACK_HEAD_LENGTH)
             return null;
         //ȡ��ͷ����
         byte[] packHead = new byte[Ah3PackHead.PACK_HEAD_LENGTH];
         System.arraycopy(this.remainBuffer,0,packHead,0,Ah3PackHead.PACK_HEAD_LENGTH);
         //������һ�������������ݰ����ܳ��ȣ�
         //���ʣ������ݳ��Ȳ�����һ��������������ݳ��ȣ�
         //�򷵻�null
         short totalLength = getRequestBodyLength(packHead);
         if(this.remainDataLength < totalLength)
             return null;
         byte[] request = new byte[totalLength];
         //ȡ��һ���������ݰ������ݣ����ƶ�ʣ������ݡ�
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
     * ȡ���������ݱ��ĳ���
     * @param packHeadBuffer �����ĵİ�ͷ����
     * @return �������ݱ��ĳ���
     */
    private short getRequestBodyLength(byte[] packHead){
        byte[] buffer=new byte[2];
        buffer[0] = packHead[Ah3PackHead.PACK_HEAD_LENGTH_OFFSET];
        buffer[1] = packHead[Ah3PackHead.PACK_HEAD_LENGTH_OFFSET + 1];
        //��ǰ���峤��
        return(ConvertDataType.toShort(buffer));
    }

    /**
     * ��SOCKET�ж�ȡ��������
     * @return ���ؽ������ݵ��ֽ���
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
�� ��* �Ѹմ�SOCKET�н��յ�������append��
���� * δ������������ݻ������С�
     * @param int appendLength �ս������ݵĳ���
     */
    private void append(int appendLength){
        //�жϴ��������ݻ������Ƿ��㹻�������������Ҫ����
        //�������Ĵ�С
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
