package com.wangdian.api.basic;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import com.wangdian.api.WdtClient;

public class WarehouseQuery {

	public static void main(String[] args) {
		// TODO Auto-generated method stub
		WdtClient client = new WdtClient("", "", "", "");
		Map<String, String> params = new HashMap<String, String>();
		params.put("warehouse_type", "1");
		params.put("start_time", "2018-12-01 00:00:00");
		params.put("end_time", "2018-12-11 00:00:00");

		try {
			String response = client.execute("shop.php", params);
			System.out.println(response);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

}
