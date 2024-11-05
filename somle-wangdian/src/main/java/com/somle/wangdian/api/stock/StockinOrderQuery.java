package com.somle.wangdian.api.stock;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import com.somle.wangdian.api.WdtClient;

public class StockinOrderQuery {

	public static void main(String[] args) {
		// TODO Auto-generated method stub
		WdtClient client = new WdtClient("", "", "", "");
		Map<String, String> params = new HashMap<String, String>();
		params.put("start_time", "2018-12-11 00:00:00");
		params.put("end_time", "2018-12-11 14:00:00");
		params.put("order_type", "3");
		try {
			String response = client.execute("stockin_order_query.php", params);
			System.out.println(response);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

}
