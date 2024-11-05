package com.wangdian.api.refund;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import com.wangdian.api.WdtClient;

public class StockinRefundQuery {

	public static void main(String[] args) {
		// TODO Auto-generated method stub
		WdtClient client = new WdtClient("", "", "", "");
		Map<String, String> params = new HashMap<String, String>();
		//params.put("shop_no", "ghs2test");
		params.put("start_time", "2018-12-04 00:00:00");
		params.put("end_time", "2018-12-11 00:00:00");
		try {
			String response = client.execute("stockin_order_query_refund.php", params);
			System.out.println(response);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

}
