package com.somle.wangdian.api.basic;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import com.somle.wangdian.api.WdtClient;

public class LogisticsQuery {

	public static void main(String[] args) {
		// TODO Auto-generated method stub
		WdtClient client = new WdtClient("", "", "", "");
		Map<String, String> params = new HashMap<String, String>();
		params.put("logistics_no", "11");

		try {
			String response = client.execute("logistics.php", params);
			System.out.println(response);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

}
