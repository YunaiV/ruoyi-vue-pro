package com.somle.wangdian.api.goods;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import com.alibaba.fastjson.JSON;
import com.somle.wangdian.api.WdtClient;

public class ApiGoodsSpecPush {

	@SuppressWarnings("unchecked")
	public static void main(String[] args) {
		// TODO Auto-generated method stub
		WdtClient client = new WdtClient("", "", "", "");

		Map<String, Object> api_goods_info = new HashMap<String,Object>();
		Map<String, Object>[] goods_list = new Map[1];

		goods_list[0] = new HashMap<String,Object>();
		goods_list[0].put("goods_id", "20151009100903");
		goods_list[0].put("spec_id","20151009100903");
		goods_list[0].put("goods_no", "stest");
		goods_list[0].put("spec_no", "stes12");
		goods_list[0].put("status", "1");
		
		api_goods_info.put("platform_id", "127");
		api_goods_info.put("shop_no", "lx2test");
		api_goods_info.put("goods_list", goods_list);
		
		//通过第三方json解析工具类fastjson将map解析成json
		String api_goods_info_json = JSON.toJSONString(api_goods_info);
		//System.out.println(goods_list_json);
		
		Map<String, String> params = new HashMap<String, String>(); 

		params.put("api_goods_info", api_goods_info_json);
		try {
			String response = client.execute("api_goodsspec_push.php", params);
			System.out.println(response);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

}
