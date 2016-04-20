package com.hermes.ah3.jdbc.util;

/**
 * A Base64 Encoder/Decoder.
  
 */
public class Base64Util {
 
    //Mapping table from 6-bit nibbles to Base64 characters.
    private static char[] map1=new char[66];
    static {
        int i=0;
        for(char c='A'; c <='Z'; c++)
            map1[i++]=c;
        for(char c='a'; c <='z'; c++)
            map1[i++]=c;
        for(char c='0'; c <='9'; c++)
            map1[i++]=c;
        map1[i++]='+';
        map1[i++]='/';
        map1[i++]='^';
        map1[i++]='@';
    }
 
    //Mapping table from Base64 characters to 6-bit nibbles.
    private static byte[] map2=new byte[132];
    static {
        for(int i=0; i<map2.length; i++)
            map2[i]=-1;
        for(int i=0; i<66; i++)
            map2[map1[i]]=(byte) i;
    }
 
    // Encodes a string into Base64 format. 
    //No blanks or line breaks are inserted. 
    public static String encodeString(String s,String key) {
    	char[] encodeResArray = encode(s.getBytes());
    	//System.out.println("Encode Result Str:[" + new String(encodeResArray) + "]");
    	char[] keyArray = key.toCharArray();
    	int len = (encodeResArray.length <= keyArray.length) ? encodeResArray.length : keyArray.length;
    	char[] resArray = new char[encodeResArray.length];
    	for(int i = 0; i < encodeResArray.length; i++) {
    		if (i < len) {
    			resArray[i] = (char)(encodeResArray[i] ^ keyArray[i]);
    		} else {
    			resArray[i] = encodeResArray[i];
    		}
    	}
    	
        return new String(resArray);
    }
 
 
     //Encodes a byte array into Base64 format.
     // No blanks or line breaks areinserted. 
    public static char[] encode(byte[] in) {
        return encode(in,in.length);
    }
 
     
    //Encodes a byte array into Base64 format. 
    //No blanks or line breaks are inserted. 
    public static char[] encode(byte[] in,int iLen) {
        int oDataLen=(iLen * 4 + 2) / 3; //output length without padding
        int oLen=((iLen + 2) / 3) * 4; //output length including padding
        char[] out=new char[oLen];
        int ip=0;
        int op=0;
        while(ip <iLen) {
            int i0=in[ip++] & 0xff;
            int i1=ip <iLen ? in[ip++] & 0xff : 0;
            int i2=ip <iLen ? in[ip++] & 0xff : 0;
            int o0=i0 >>> 2;
            int o1=((i0 & 3) <<4) |(i1 >>> 4);
            int o2=((i1 & 0xf) <<2) |(i2 >>> 6);
            int o3=i2 & 0x3F;
            out[op++]=map1[o0];
            out[op++]=map1[o1];
            out[op]=op <oDataLen ? map1[o2] : '=';
            op++;
            out[op]=op <oDataLen ? map1[o3] : '=';
            op++;
        }
        return out;
    }
 
    //Decodes a string from Base64 format. 
    public static String decodeString(String s,String key) {
    	char[] decodeArray = s.toCharArray();    	
    	char[] keyArray = key.toCharArray();
    	int len = (decodeArray.length <= keyArray.length) ? decodeArray.length : keyArray.length;
    	char[] resArray = new char[decodeArray.length];
    	for(int i = 0; i < decodeArray.length; i++) {
    		if (i < len) {
    			resArray[i] = (char)(decodeArray[i] ^ keyArray[i]);
    		} else {
    			resArray[i] = decodeArray[i];
    		}
    	}
    	System.out.println("Exclusive-OR Again Str:[" + new String(resArray) + "]");
    	
        return new String(decode(new String(resArray)));
    }
 
     
     //Decodes a byte array from Base64 format.  
    public static byte[] decode(String s) {
        return decode(s.toCharArray());
    }
 
     
     //Decodes a byte array from Base64 format.
     //No blanks or line breaks are
    //allowed within the Base64 encoded data. 
    public static byte[] decode(char[] in) {
        int iLen=in.length;
        if(iLen % 4!=0)
            throw new IllegalArgumentException(
                    "Length of Base64 encoded input "
                    +"string is not a multiple of 4.");
        while(iLen > 0&&in[iLen - 1]=='=')
            iLen--;
        int oLen=(iLen * 3) / 4;
        byte[] out=new byte[oLen];
        int ip=0;
        int op=0;
        while(ip <iLen) {
            int i0=in[ip++];
            int i1=in[ip++];
            int i2=ip <iLen ? in[ip++] : 'A';
            int i3=ip <iLen ? in[ip++] : 'A';
            if(i0 > 127 ||i1 > 127 ||i2 > 127 ||i3 > 127)
                throw new IllegalArgumentException(
                        "Illegal character in Base64 encoded data.");
            int b0=map2[i0];
            int b1=map2[i1];
            int b2=map2[i2];
            int b3=map2[i3];
            System.out.println("i0=" + (char)i0 + ",i1=" + (char)i1+ ",i2=" + (char)i2+ ",i3=" + (char)i3+ ",");
            //System.out.println("b0=" + (char)b0 + ",b1=" + (char)b1+ ",b2=" + (char)b2+ ",b3=" + (char)i3+ ",");
            System.out.println("b0=" + b0 + ",b1=" + b1+ ",b2=" + b2+ ",b3=" + i3+ ",");
            if(b0 <0 ||b1 <0 ||b2 <0 ||b3 <0)
                throw new IllegalArgumentException(
                        "Illegal character in Base64 encoded data.");
            int o0=(b0 <<2) |(b1 >>> 4);
            int o1=((b1 & 0xf) <<4) |(b2 >>> 2);
            int o2=((b2 & 3) <<6) | b3;
            out[op++]=(byte) o0;
            if(op <oLen)
                out[op++]=(byte) o1;
            if(op <oLen)
                out[op++]=(byte) o2;
        }
        return out;
    }
 
    private Base64Util() {
 
    }
 
    public static void main(String[] args) {
        try{
        	String pwd = "JuQian&Liuyy@YN*()&^%$#@!~";
        	String key = "4004230131831";
        	System.out.println("String need to be Encoded:[" + pwd + "]");
        	String encodeRes = Base64Util.encodeString(pwd,key);
            System.out.println("Exclusive-OR Str:[" + encodeRes + "]");
//            System.out.println(Base64Util.encodeString("VC"));
//            System.out.println(Base64Util.decodeString("5oKo5aW9"));
            System.out.println("Decode Str:[" + Base64Util.decodeString("g^ffSdvDyZ@CUXl5QFlOKigpJl4lJCNAIX4=",key) + "]");
             
        }
        catch(Exception ex){
            ex.printStackTrace();
        }
    }
}