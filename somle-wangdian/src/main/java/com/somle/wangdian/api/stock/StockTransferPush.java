package com.somle.wangdian.api.stock;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.alibaba.fastjson.JSON;
import com.somle.wangdian.api.WdtClient;

public class StockTransferPush {

	public static void main(String[] args) {
		// TODO Auto-generated method stub
		WdtClient client = new WdtClient("", "", "", "");
		
		Map<String, Object> transfer_info = new HashMap<String, Object>();
		List<Map<String, Object>> skus = new ArrayList<Map<String, Object>>();
		
		Map<String, Object> sku_1 = new HashMap<String, Object>();
		sku_1.put("spec_no", "ghs201812070212123");
		sku_1.put("num", "1");
		
		skus.add(sku_1);
		
		transfer_info.put("from_warehouse_no", "ghs2test");
		transfer_info.put("to_warehouse_no", "lx2test");
		transfer_info.put("outer_no", "ghs201812101205");
		transfer_info.put("skus", skus);
		
		String transfer_info_json = JSON.toJSONString(transfer_info);
		//System.out.println(purchase_info_json);
		
		Map<String, String> params = new HashMap<String, String>();
		params.put("transfer_info", transfer_info_json);
		try {
			String response = client.execute("stock_transfer_push.php", params);
			System.out.println(response);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

}
