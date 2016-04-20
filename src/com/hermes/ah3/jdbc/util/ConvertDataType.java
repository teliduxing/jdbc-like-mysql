/**
 * 
 */
package com.hermes.ah3.jdbc.util;


/**
 * �ѻ�������ת��Ϊbyte[]���byte[]ת��Ϊ��������
 * @author wuwl
 *
 */
public class ConvertDataType{
    /**
     *��cdataת�����캯��
     */
    public ConvertDataType(){}

    /**
     *������ת��Ϊ�ֽ�����
     *@param num ����������
     *@return byte[] �����ֽ�����
     */
    static public byte[] toBytes(short num){
            int j;
            byte tmp[]=new byte[2];
            for(j=0;j<2;j++){
                tmp[2-1-j]= (byte)num;
                num = (short)(num >>> 8);
            }
            return tmp;
    }//method toBytes

    /**
     *����ת��Ϊ�ֽ�����
     *@param num ��������
     *@return byte[] �����ֽ�����
     */
    static public byte[] toBytes(int num){
            int j;
            byte tmp[]=new byte[4];

            for(j=0;j<4;j++){
                tmp[4-1-j]= (byte)num;
                num = num >>> 8;
            }
            return tmp;
    }//method tobytes

    /**
     *�ַ�ת��Ϊ�ֽ�����
     *@param num �ַ�
     *@return byte[] �����ֽ�����
     */
    static public byte[] toBytes(char num){
            int j;
            byte tmp[]=new byte[1];
            tmp[0]= (byte)num;
            return tmp;
    }//method tobytes

    /**
     *���ַ������������Ȳ���ת��Ϊ�ֽ�����
     *@param str �ַ���
     *@param len �ַ�������
     *@return byte[] �����ֽ�����
     */
    static public byte[] toBytes(String str, int len){
            int i;
            byte b[] = new byte[len];
            int strlen = str.length();
            if (strlen >=len){
                    str = str.substring(0,len);
            }//if

            byte tmpbuf[] = str.getBytes();

            for(i=0;i<tmpbuf.length;i++){b[i]=tmpbuf[i];}//for
            for(i=tmpbuf.length;i<len;i++){b[i]=0;}//for

            return b;
    }//method string2bytes

    /**
     *���ַ������������Ȳ���ת��Ϊ�ֽ�����
     *@param str �ַ���
     *@return byte[] �����ֽ�����
     */
    static public byte[] toBytes(String str){
            byte b[] = new byte[str.getBytes().length];
            b=str.getBytes();
            return b;
    }//method string2bytes


    /**
     *������ת��Ϊ�ֽ�����
     *@param num ����������
     *@return byte[] �����ֽ�����
     */
    static public byte[] toBytes(long num){
              int j;
              byte tmp[]=new byte[4];

              for(j=0;j<4;j++){
                tmp[4-1-j]= (byte)num;
                num = num >>> 8;
              }
              return tmp;

    }//method long2bytes

    /**
     *�ֽ�����ת��Ϊ������
     *@param b[] �ֽ�����
     *@return short ���ض�����
     */
    static public short toShort(byte b[]){
            short tmp,tmp0,j;
            tmp=0;
            tmp0=0;
            j=0;
            for(j=0;j<2;j++){
            tmp0 = (short)b[2-1-j];
            if(tmp0<0) {tmp0 = (short)(tmp0 + 256);}
            tmp = (short)(tmp + (tmp0<<(j*8)));
            }//for
            return tmp;
    }//method bytes2short

   /**
     *ʮ����������ת��Ϊ������
     *@param hexData ʮ��������������
     *@return short ���ض���������
     */
    static public short toShort(int hexData){
            return((short)(Integer.parseInt(Integer.toString(hexData), 16)));
    }//method hex2short

    /**
     *�ֽ�����ת��Ϊ����������
     *@param b[] �ֽ�����
     *@return short[] ���ض���������
     */
    static public short[] toShortArray(byte b[]){
            int i,j;
            int len = b.length/2;
            short tmp[] = new short[len];
            byte tmpb[] = new byte[2];

            for(i=0;i<len;i++){
                    j=i*2;
                    tmpb[0] = b[j];
                    tmpb[1] = b[j+1];
            tmp[i] = toShort(tmpb);
            }//for
            return tmp;
    }//method bytes2shortarray

    /**
     *�ֽ�����ת��Ϊ����
     *@param  b �ֽ�����
     *@return ��Ӧ����������,��������򷵻�INT�͵���Сֵ��-2147483648��
     */
    public static int toInt(byte[] b){
            int value=0;
    	int value0,value1,value2,value3;
    	//������ݱ��벻���ڣ�����INT�͵���Сֵ��INT�͵���Сֵϵͳ����
	if(b.length < 4) return -2147483648;
	Byte byte0 = new Byte(b[0]);
    	Byte byte1 = new Byte(b[1]);
    	Byte byte2 = new Byte(b[2]);
    	Byte byte3 = new Byte(b[3]);
	value0 = byte0.intValue();
	value1 = byte1.intValue();
	value2 = byte2.intValue();
	value3 = byte3.intValue();
	if(value0 < 0) value0 += 256;
	if(value1 < 0) value1 += 256;
	if(value2 < 0) value2 += 256;
	if(value3 < 0) value3 += 256;
	value = (int)(value0*256*256*256 + value1*256*256 + value2*256 + value3);
            return value;
    }//method toint


    /**
     *�ֽ�����ת��Ϊ������
     *@parameter b[] �ֽ���������
     *@return long ���س�����
     */
    static public long toLong(byte[] b){
        long value;
       try{
            if(ConvertDataType.toString(b).trim().equals(""))
             	return Long.MIN_VALUE;
            value = Long.parseLong(ConvertDataType.toString(b).trim());
           return value;
        }catch(NumberFormatException e){
            System.out.println(e);
            return Long.MIN_VALUE;
        }
   }//toLong(byte[] tmpVaule)

   /**
     *�ֽ�����ת��Ϊ������
     *@parameter b[] �ֽ���������
     *@return double ���ظ�����
    */
   static public double toDouble(byte[] b){
   	   String tmpString = new String(b).trim();
   	   if(tmpString.equals("")){
    	   	return Double.MIN_VALUE;
   	   }
        //System.out.println("in class ConvertDataType toDouble().");
       return Double.parseDouble(tmpString.trim());
   }//toDouble(byte[] b)


    /**
     *�ֽ�����ת��Ϊ�ַ���
     *@parameter b[] �ֽ�����
     *@return String �����ַ���
     */
    static public String toString(byte[] b){
            String tmp = new String(b);
            return tmp;
    }//method bytes2string

    /**
     *������ת��Ϊ�ַ�����
     *param short
     *return String
     */
    static public String toString(short sot){
            return (new Short(sot)).toString(sot);
    }

    /**
     *�ֽ�����ת��Ϊʮ�������ַ���
     *@param b[] �ֽ�����
     *@return String �����ַ���
     */
    static public String toHexString(byte[] msg){
            String tmpstr="";
            int len = 0;
            len = msg.length;
            for(int i=0;i<len;i++){
                    Byte mbyte=new Byte(msg[i]);
                    Integer mInt=new Integer(mbyte.toString(msg[i]));
                    String tmpHex="";
                    tmpHex = Integer.toHexString(mInt.intValue ());

                    if (mInt.intValue ()>9)
                            tmpstr = tmpstr + tmpHex +" ";
                    else
                            if (mInt.intValue ()>=0){
                                    tmpstr = tmpstr + "0" + tmpHex + " ";
                            }
                    else{
                            tmpstr = tmpstr + tmpHex.substring(6,tmpHex.length())+" ";//�з���hex
                    }//if
             }//for
            return tmpstr;
    }//method bytes2hexstr


    /**
     *����ת���ַ�
     *@param cnStr �����ַ���
     *@return String �����ַ���
     */
    static public String toCnString(String cnStr){
            byte[] b = new byte[cnStr.getBytes().length];
            b = cnStr.getBytes();
            String str = "";

            try{
                    str = new String(b,"gb2312");
            }
            catch(java.io.UnsupportedEncodingException  e){
                    System.out.println(e);
            }//try
            return str;
    }//cnstring

}
