package com.wangdian.api.refund;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import com.wangdian.api.WdtClient;

public class RefundQuery {

	public static void main(String[] args) {
		// TODO Auto-generated method stub
		WdtClient client = new WdtClient("", "", "", "");
		Map<String, String> params = new HashMap<String, String>();
		params.put("process_status", "90");
		params.put("start_time", "2018-12-01 00:00:00");
		params.put("end_time", "2018-12-10 00:00:00");
		try {
			String response = client.execute("refund_query.php", params);
			System.out.println(response);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

}
