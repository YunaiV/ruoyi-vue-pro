package com.somle.wangdian.api.trade;

import java.io.IOException;
import java.io.StringReader;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

import com.somle.wangdian.api.WdtClient;

public class TradeQuery {

	public static void main(String[] args) {
		// TODO Auto-generated method stub
		WdtClient client = new WdtClient("", "", "", "");
		
		Map<String, String> params = new HashMap<String, String>();
		params.put("start_time", "2024-10-23 12:00:00");
		params.put("end_time", "2024-10-23 13:00:00");
		params.put("page_size", "30");
		params.put("page_no", "0");
		
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

	public static void between(LocalDateTime startTime, LocalDateTime endTime) {
		WdtClient client = new WdtClient("", "", "", "");

		Map<String, String> params = new HashMap<String, String>();
		params.put("start_time", startTime.toString());
		params.put("end_time", endTime.toString());
		params.put("page_size", "100");
		params.put("page_no", "0");

		try {
			String response = client.execute("trade_query.php", params);
			Properties properties = new Properties();
			// Add a dummy key-value pair, as Properties expect key-value pairs
			properties.load(new StringReader("key=" + response));
			System.out.println(properties.getProperty("key"));
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

}
