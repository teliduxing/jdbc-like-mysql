///**
// * 
// */
package com.hermes.ah3.jdbc.util;
//
///**
// * @author wuwl
// *
// */
//public class ConfuseStrUtil {
//	
//	private static int MAXUKPBYTES = 40;
//	private static int SHADOW_LENGTH = 88;
//	
//	//字节原子移位函数——奇偶位互换
//	private static byte OPExchange(byte ch)
//	{
//			byte ch1,ch2;
//	         
//	         ch1 = (byte) (ch & 0x55);      //将1357位置0(奇位)
//	         ch2 = (byte) (ch & 0xAA);      //将0246位置0(偶位)
//	         ch  = (byte) ( ch1<<1 | ch2>>1 );
//	         return ch;
//	}
//
//	//字节原子移位函数——循环左移n位(n<8 且 n>0, 当n=4时是半字节互换)
//	private static byte RnExchange(byte ch, int n)
//	{
//		byte ch1,ch2;
//	         
//	         ch1 = (byte) (ch<<n);    //向左移n位
//	         ch2 = (byte) (ch>>(8-n));//向右移8-n位,留高n位并移置最低处
//	         ch  = (byte) (ch1 | ch2);
//	         return ch;
//	}
//
//	//按需组合使用原子移位函数实现字节数据的充分混淆
//	private static byte ConfuseChar(byte ch)
//	{
//	         ch = OPExchange(ch);      //先奇偶位互换
//	         ch = RnExchange(ch, 3);  //再循环左移3位
//	         return ch;
//	}
//	
//	//还原混淆的字节数据
//	private static byte RestoreChar(byte ch)
//	{
//	         ch = RnExchange(ch, 5);  //先解混淆
//	         ch = OPExchange(ch);      //再奇偶互换还原
//	         return ch;
//	}
//
//
//	//passwd最长取17个字符,超长则截断
//	public static String toShadow(byte[] passwd, byte[] key) throws Exception
//	{
//	         byte ch,len,i,klen;
//	         //char mid[20]={0};
//	         byte mid[] = new byte[MAXUKPBYTES + 1];
//	         byte[] shadow = new byte[SHADOW_LENGTH];
//
//	         //klen = strlen(key);
//	         klen = key.length;
//	         //if((len=strlen(passwd))>17) {
//	         if((len = passwd.length)>MAXUKPBYTES) {
//	                   len = MAXUKPBYTES;
//	                   passwd[len]=0;
//	         }
//	         mid[0] = (byte) len;
//	         System.out.println("passwd:" + new String(passwd));
//	         
//	         
//	         for(i=0; i<MAXUKPBYTES; i++)
//	         {
//	                   //真正起key作用的就是和passwd等长的前几个字符
//	                   mid[i+1] = (byte) (key[i%klen] ^ passwd[i%len]);
//	         }
////	         System.out.println("mid[]: " + new String(mid));
//
//	         for(i=0; i<(MAXUKPBYTES<<1); i++)
//	         {
//	                   ch = ConfuseChar(mid[i>>1]);
//	                   if ((i&1) == 1) { ch>>=4; }
//	                   else { ch&=0x0F; }
//	                   shadow[i] = (byte) (((0x30|ch)<<1)|1);
//	         }
//	         shadow[i] = 0;
//	         
//	         System.out.println("shadow[]: " + new String(shadow));
//	         return new String(shadow);
//	         //return new String(new String(shadow).getBytes("GBK"));
//	}
//	
//	public static String toPasswd(byte[] shadow, byte[] key)
//	{
//        int ch,len,i,klen;
////        char mid[MAXUKPBYTES+1]={0};
//        byte mid[] = new byte[MAXUKPBYTES + 1];
//        byte[] passwd = new byte[SHADOW_LENGTH];
//
//                
////        klen = strlen(key);
//        klen = key.length;
//        for(i=0; i<(MAXUKPBYTES<<1); i++)
//        {
//                  ch = ((shadow[i]>>1) & 0x0F);
//                  if((i&1) == 1) {
//                           mid[i>>1] |= ch<<4;
//                           ch = RestoreChar(mid[i>>1]);
//                           mid[i>>1] = (byte)ch;
//                  }
//                  else { mid[i>>1] |= ch; }
//        }
//
//        len=mid[0];
//        for(i=0; i<len; i++)
//        {
//                  passwd[i] = (byte) (mid[i+1] ^ key[i%klen]);
//        }
//        return new String(passwd);
//}
//
//	
////	private static charToStr(char[] chars) {
////		akií{?k?kí{?i?{?k?{í{?iá{?kéi?y?k?i÷i?i? / 4200056821831
////		aki?{?ké{?{?i?{?k?{í{?iá{?kéi?y?k?i÷i?i? / 1313956821831		
////	}
//
//	/**
//	 * @param args
//	 */
//	public static void main(String[] args) {
//		try {
//			String key = "4004230131831";
//			String password = "xielj";
//			//String confusedPwd = ConfuseStrUtil.toShadow(password.toCharArray(), key.toCharArray());
//			String confusedPwd = ConfuseStrUtil.toShadow(password.getBytes(), key.getBytes());
//			System.out.println(confusedPwd + "," + confusedPwd.length());
//			//String pwd = ConfuseStrUtil.toPasswd(confusedPwd.toCharArray(), key.toCharArray());
//			//String pwd = ConfuseStrUtil.toPasswd(confusedPwd.getBytes(), key.getBytes());
//			//System.out.println(pwd);
//			
//			byte a;
//	        
//	        a=0;
//	        a=3;
//	        a|= 0x40;
//	        
//	        System.out.println(a);
//		} catch (Exception e) {
//			e.printStackTrace();
//		}
//	}
//
//}


public class ConfuseStrUtil {
	
	private static int MAXUKPBYTES = 20;
	
	public static byte OPExchange(byte ch)
	{
		byte ch1,ch2;
			 
		ch1 = (byte) (ch & 0x55);      //将1357位置0(奇位)
		ch2 = (byte) (ch & 0xAA);      //将0246位置0(偶位)
		ch  = (byte) ((ch1<<1 | ch2>>>1) & 0xFF);
		return ch;
	}

	public static byte RnExchange(byte ch, int n)
	{
		byte ch1,ch2;
			 
		ch1 = (byte) ((ch<<n));    //向左移n位
		ch2 = (byte) ((ch>>>(8-n)));//向右移8-n位,留高n位并移置最低处
		ch  = (byte) ((ch1 | ch2));
		return ch;
	}

	private static byte Confusebyte(byte ch)
	{
		ch = OPExchange(ch);      //先奇偶位互换
		ch = RnExchange(ch, 3);  //再循环左移3位
		return ch;
	}
	
	private static byte Restorebyte(byte ch)
	{
		ch = RnExchange(ch, 5);  //先解混淆
		ch = OPExchange(ch);      //再奇偶互换还原
		return ch;
	}

	public static String toShadow(byte[] passwd, byte[] key) throws Exception
	{
		int ch,len,i,klen;
		byte mid[] = new byte[MAXUKPBYTES + 1];
		byte[] shadow = new byte[44];

		klen = key.length;
		if((len = passwd.length)>MAXUKPBYTES) {
			len = MAXUKPBYTES;
			passwd[len]=0;
		}
		mid[0] = (byte)(len & 0xFF);
		 
		for(i=0; i<MAXUKPBYTES; i++)
		{
			mid[i+1] = (byte) ((key[i%klen] ^ passwd[i%len]) & 0xFF);
		}

		for(i=0; i<(MAXUKPBYTES<<1); i++)
		{
			ch = Confusebyte(mid[i>>>1]);
			if ((i&1) == 1) { 
				ch = ((ch>>>4)&0x0F);
			}
			else { 
				ch = (0x0F & ch); 
			}
			shadow[i] = (byte) ((((0x30|ch)<<1)|1)&0xFF);
		}
		//System.out.print("\n");
		shadow[i] = 0;
		 
		//	期待输出   akimkgkkkekeygkgki{o{gia{ekike{eieko{k{o	         
		//System.out.println("shadow[]: " + new String(shadow));

		return new String(shadow);
	}
	
	public static String toPasswd(byte[] shadow, byte[] key)
	{
		int ch,len,i,klen;
//        byte mid[MAXUKPBYTES+1]={0};
		byte mid[] = new byte[MAXUKPBYTES + 1];
		byte[] passwd = new byte[44];

//        klen = strlen(key);
		klen = key.length;
		for(i=0; i<(MAXUKPBYTES<<1); i++)
		{
				  ch = ((shadow[i]>>>1) & 0x0F);
				  if((i&1) == 1) {
						   mid[i>>>1] |= ch<<4;
						   ch = Restorebyte(mid[i>>>1]);
						   mid[i>>>1] = (byte)ch;
				  }
				  else { mid[i>>>1] |= ch; }
		}

		len=mid[0];
		for(i=0; i<len; i++)
		{
				  passwd[i] = (byte) (mid[i+1] ^ key[i%klen]);
		}
		return new String(passwd);
	}

	
	

	public static void main(String[] args) {
		try {
			String key = "4004230131831";
			String password = "xielj";
			
			byte[] pwdBytes = new byte[255];
			for(int i = 0; i < 255; i++) {
				//System.out.println(pwdBytes[i]);
				pwdBytes[i] = (byte)(i&0xFF);
				//System.out.println("i:" + pwdBytes[i] + ",r:" + (ConfuseStrUtil.OPExchange(pwdBytes[i]) & 0xff));
				System.out.println("i:" + pwdBytes[i] + ",r:" + (ConfuseStrUtil.RnExchange(pwdBytes[i],3) & 0xff));
			}
			
//			String confusedPwd = ConfuseStrUtil.toShadow(password.getBytes(), key.getBytes());
//			System.out.println("[" + confusedPwd + "]," + confusedPwd.length());
//			String pwd = ConfuseStrUtil.toPasswd(confusedPwd.getBytes(), key.getBytes());
//			System.out.println(pwd);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

}

