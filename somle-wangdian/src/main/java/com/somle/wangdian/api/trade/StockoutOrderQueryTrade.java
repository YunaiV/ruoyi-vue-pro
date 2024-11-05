package com.somle.wangdian.api.trade;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import com.somle.wangdian.api.WdtClient;

public class StockoutOrderQueryTrade {

	public static void main(String[] args) {
		// TODO Auto-generated method stub
		WdtClient client = new WdtClient("", "", "", "");
		Map<String, String> params = new HashMap<String, String>();
		params.put("shop_no", "ghs2test");
		params.put("start_time", "2018-12-01 00:00:00");
		params.put("end_time", "2018-12-010 00:00:00");
		try {
			String response = client.execute("refund_query.php", params);
			System.out.println(response);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

}
