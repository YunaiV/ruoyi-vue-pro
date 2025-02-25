package cn.iocoder.yudao.framework.common.util.io;

import org.apache.commons.io.IOUtils;

import java.io.*;

/**
 * Stream转换工具，结合  org.apache.commons.io.IOUtils 和   jodd.io.StreamUtil ; 也可直接使用上述两个工具类
 * 
 * @author LeeFangJie
 */
public class StreamUtil {

	/**
	 * inputStream转outputStream
	 * @param in 输入流
	 * @param out 输出流
	 * @return 输出流
	 * @throws Exception  抛出异常
	 */
	public static OutputStream input2output(InputStream in,OutputStream out) throws Exception {
		if(out==null) out = new ByteArrayOutputStream();
		IOUtils.copy(in, out);
		return out;
	}
	
	/**
	 * inputStream转outputStream
	 * @param in 输入流
	 * @return 输出流
	 * @throws Exception  抛出异常
	 */
	public static OutputStream input2output(InputStream in) throws Exception {
		return input2output(in,null);
	}
 
	/**
	 * 将InputStream转成String
	 * @param in InputStream
	 * @param encoding 编码
	 * @return 字符串
	 * @throws Exception  抛出异常
	 * */
	public static String input2string(InputStream in, String encoding) throws Exception {
		StringWriter writer = new StringWriter();
		IOUtils.copy(in, writer, encoding);
		String content=writer.toString();
		writer.close();
		return content;
	}
	
	/**
	 * 将InputStream转成String
	 * @param in InputStream
	 * @return 字符串
	 * @throws Exception  抛出异常
	 * */
	public static String input2string(InputStream in) throws Exception {
		StringWriter writer = new StringWriter();
		IOUtils.copy(in, writer, "UTF-8");
		return writer.toString();
	}
 
	/**
	 * String转inputStream
	 * @param in 输入字符串
	 * @return 输出流
	 * @throws Exception  抛出异常
	 */
	public static ByteArrayInputStream string2input(String in) throws Exception {
		ByteArrayInputStream input = new ByteArrayInputStream(in.getBytes());
		return input;
	}

	/**
	 * String 转outputStream
	 * @param in 输入字符串
	 * @return 输出流
	 * @throws Exception  抛出异常
	 */
	public static ByteArrayOutputStream string2output(String in) throws Exception {
		return (ByteArrayOutputStream)input2output(string2input(in),new ByteArrayOutputStream());
	}
	
	/**
	 * String 转 outputStream
	 * @param in 输入流
	 * @return 输出 byte 数组
	 * @throws Exception  抛出异常
	 */
	public static final byte[] input2bytes(InputStream in) throws Exception {
		ByteArrayOutputStream swapStream=(ByteArrayOutputStream)input2output(in,new ByteArrayOutputStream());
		byte[] in2b = swapStream.toByteArray();
		return in2b;
	}
 

	/**
	 * byte[]转 input
	 * 
	 * @param bytes 输入字节
	 * @return 输出流
	 * @throws Exception  抛出异常
	 */
	public static ByteArrayInputStream bytes2input(byte[] bytes) throws Exception {
		return new ByteArrayInputStream(bytes);
	}
	
 

}
