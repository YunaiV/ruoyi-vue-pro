package com.somle.wangdian.api;

import java.io.IOException;
import java.io.StringReader;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

public class Demo {

	public static void main(String[] args) {
        //sid appkey secret url
		WdtClient client = new WdtClient("", "", "", "");


		Map<String, String> params = new HashMap<String, String>();
		try {
			String response = client.execute("trade_query.php", params);
			Properties properties = new Properties();
			// Add a dummy key-value pair, as Properties expect key-value pairs
			properties.load(new StringReader("key=" + response));
			System.out.println(properties.getProperty("key"));
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

}
