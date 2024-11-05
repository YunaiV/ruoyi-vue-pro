package com.wangdian.api.trade;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.alibaba.fastjson.JSON;
import com.wangdian.api.WdtClient;

public class ApiGoodsStockChangeAck {

	public static void main(String[] args) {
		// TODO Auto-generated method stub
		WdtClient client = new WdtClient("", "", "", "");
		
		List<Map<String, Object>> stock_sync_list = new ArrayList<Map<String, Object>>();
		
		Map<String, Object> stock_sync_1 = new HashMap<String, Object>();
		stock_sync_1.put("rec_id", 1);
		stock_sync_1.put("sync_stock", 100);
		stock_sync_1.put("sstock_change_count", 123);
		
		stock_sync_list.add(stock_sync_1);
		
		String stock_sync_list_json = JSON.toJSONString(stock_sync_list);
		
		Map<String, String> params = new HashMap<String, String>();
		params.put("stock_sync_list", stock_sync_list_json);
		try {
			String response = client.execute("api_goods_stock_change_ack.php", params);
			System.out.println(response);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

}
