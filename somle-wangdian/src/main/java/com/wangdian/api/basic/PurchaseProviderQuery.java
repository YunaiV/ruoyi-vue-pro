package com.wangdian.api.basic;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import com.wangdian.api.WdtClient;

public class PurchaseProviderQuery {

	public static void main(String[] args) {
		// TODO Auto-generated method stub
		WdtClient client = new WdtClient("", "", "", "");
		Map<String, String> params = new HashMap<String, String>();
		params.put("column", "provider_name,address,website,remark,is_disabled,deleted,modified,created");

		try {
			String response = client.execute("purchase_provider_query.php", params);
			System.out.println(response);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

}
