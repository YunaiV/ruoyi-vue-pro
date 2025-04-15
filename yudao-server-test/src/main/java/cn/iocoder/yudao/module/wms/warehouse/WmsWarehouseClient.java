package cn.iocoder.yudao.module.wms.warehouse;

import cn.iocoder.yudao.test.Profile;
import cn.iocoder.yudao.test.RestClient;
import org.springframework.boot.test.web.client.TestRestTemplate;

/**
 * @author: LeeFJ
 * @date: 2025/4/11 8:53
 * @description:
 */
public class WmsWarehouseClient extends RestClient {
    public WmsWarehouseClient(Profile profile, TestRestTemplate testRestTemplate) {
        super(profile, testRestTemplate);
    }
}
