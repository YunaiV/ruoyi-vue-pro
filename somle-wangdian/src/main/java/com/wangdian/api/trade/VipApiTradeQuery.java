package com.wangdian.api.trade;

import com.wangdian.api.WdtClient;

import java.io.IOException;
import java.io.StringReader;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

public class VipApiTradeQuery {

	public static void main(String[] args) {
		// TODO Auto-generated method stub
		WdtClient client = new WdtClient("", "", "", "");
		
		Map<String, String> params = new HashMap<String, String>();
		params.put("start_time", "2024-10-24 10:00:00");
		params.put("end_time", "2024-10-24 10:46:00");
		params.put("page_size", "30");
		params.put("page_no", "0");
		
		try {
			String response = client.execute("vip_api_trade_query.php", params);
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
