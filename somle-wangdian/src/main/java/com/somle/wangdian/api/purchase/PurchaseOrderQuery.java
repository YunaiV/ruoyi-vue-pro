package com.somle.wangdian.api.purchase;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import com.somle.wangdian.api.WdtClient;

public class PurchaseOrderQuery {

	public static void main(String[] args) {
		// TODO Auto-generated method stub
		WdtClient client = new WdtClient("", "", "", "");
		Map<String, String> params = new HashMap<String, String>();
		params.put("start_time", "2018-12-01 00:00:00");
		params.put("end_time", "2018-12-06 00:00:00");
		try {
			String response = client.execute("purchase_order_query.php", params);
			System.out.println(response);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

}
