package cn.iocoder.yudao.framework.common.util.io;

import org.apache.commons.io.IOUtils;

import java.io.*;

/**
 * Stream转换工具，结合  org.apache.commons.io.IOUtils 和   jodd.io.StreamUtil ; 也可直接使用上述两个工具类
 * 
 * @author LeeFangJie
 */
public class StreamUtils {

	/**
	 * inputStream转outputStream
	 * @param in 输入流
	 * @param out 输出流
	 * @return 输出流
	 * @throws Exception  抛出异常
	 */
	public static OutputStream inputToOutput(InputStream in,OutputStream out) throws Exception {
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
	public static OutputStream inputToOutput(InputStream in) throws Exception {
		return inputToOutput(in,null);
	}
 
	/**
	 * 将InputStream转成String
	 * @param in InputStream
	 * @param encoding 编码
	 * @return 字符串
	 * @throws Exception  抛出异常
	 * */
	public static String inputToString(InputStream in, String encoding) throws Exception {
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
	public static String inputToString(InputStream in) throws Exception {
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
	public static ByteArrayInputStream stringToInput(String in) throws Exception {
		ByteArrayInputStream input = new ByteArrayInputStream(in.getBytes());
		return input;
	}

	/**
	 * String 转outputStream
	 * @param in 输入字符串
	 * @return 输出流
	 * @throws Exception  抛出异常
	 */
	public static ByteArrayOutputStream stringToOutput(String in) throws Exception {
		return (ByteArrayOutputStream)inputToOutput(stringToInput(in),new ByteArrayOutputStream());
	}
	
	/**
	 * String 转 outputStream
	 * @param in 输入流
	 * @return 输出 byte 数组
	 * @throws Exception  抛出异常
	 */
	public static final byte[] inputToBytes(InputStream in) throws Exception {
		ByteArrayOutputStream swapStream=(ByteArrayOutputStream)inputToOutput(in,new ByteArrayOutputStream());
		byte[] inToByte = swapStream.toByteArray();
		return inToByte;
	}
 

	/**
	 * byte[]转 input
	 * 
	 * @param bytes 输入字节
	 * @return 输出流
	 * @throws Exception  抛出异常
	 */
	public static ByteArrayInputStream bytesToInput(byte[] bytes) throws Exception {
		return new ByteArrayInputStream(bytes);
	}
	
 

}
