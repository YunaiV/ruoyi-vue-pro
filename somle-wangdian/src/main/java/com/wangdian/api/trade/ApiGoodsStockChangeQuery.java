package com.wangdian.api.trade;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import com.wangdian.api.WdtClient;

public class ApiGoodsStockChangeQuery {

	public static void main(String[] args) {
		// TODO Auto-generated method stub
		WdtClient client = new WdtClient("", "", "", "");
		Map<String, String> params = new HashMap<String, String>();
		params.put("shop_no", "ghs2test");
		params.put("limit", "100");
		try {
			String response = client.execute("api_goods_stock_change_query.php", params);
			System.out.println(response);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

}
