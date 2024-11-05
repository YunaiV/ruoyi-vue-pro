package com.wangdian.api.goods;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import com.alibaba.fastjson.JSON;

import com.wangdian.api.WdtClient;

public class GoodsPush {
	
	@SuppressWarnings("unchecked")
	public static void main(String[] args) {
		// TODO Auto-generated method stub
		WdtClient client = new WdtClient("", "", "", "");

		Map<String, Object>[] goods_list = new Map[1];
		Map<String, Object>[] spec_list = new Map[2];

		
		spec_list[0] = new HashMap<String, Object>();
		spec_list[0].put("spec_no", "ghs2018120503");
		spec_list[0].put("spec_code", "test001_01132");
		spec_list[0].put("spec_name", "test001_01132");
		
		spec_list[1] = new HashMap<String, Object>();
		spec_list[1].put("spec_no", "ghs2018120505");
		spec_list[1].put("spec_code", "test001_01134");
		spec_list[1].put("spec_name", "test001_01133");
		
		goods_list[0] = new HashMap<String,Object>();
		goods_list[0].put("goods_no", "ghs1207");
		goods_list[0].put("goods_type","1");
		goods_list[0].put("goods_name", "stest");
		goods_list[0].put("spec_list", spec_list);
		
		//通过第三方json解析工具类fastjson将map解析成json
		String goods_list_json = JSON.toJSONString(goods_list);
		//System.out.println(goods_list_json);
		
		Map<String, String> params = new HashMap<String, String>(); 
		params.put("goods_list", goods_list_json);
		try {
			String response = client.execute("goods_push.php", params);
			System.out.println(response);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

}
