package com.wangdian.api.trade;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.alibaba.fastjson.JSON;
import com.wangdian.api.WdtClient;

public class LogisticsSyncAck {

	public static void main(String[] args) {
		// TODO Auto-generated method stub
WdtClient client = new WdtClient("", "", "", "");
		
		List<Map<String, Object>> logistics_list = new ArrayList<Map<String, Object>>();
		
		Map<String, Object> logistics_1 = new HashMap<String, Object>();
		logistics_1.put("rec_id", 1);
		logistics_1.put("status", 0);
		logistics_1.put("message", "同步成功");
		
		logistics_list.add(logistics_1);
		
		String logistics_list_json = JSON.toJSONString(logistics_list);
		
		Map<String, String> params = new HashMap<String, String>();
		params.put("logistics_list", logistics_list_json);
		try {
			String response = client.execute("logistics_sync_ack.php", params);
			System.out.println(response);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

}
