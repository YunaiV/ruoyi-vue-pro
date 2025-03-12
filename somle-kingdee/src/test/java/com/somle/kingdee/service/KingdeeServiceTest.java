package com.somle.kingdee.service;

import cn.iocoder.yudao.framework.common.util.date.LocalDateTimeUtils;
import cn.iocoder.yudao.framework.common.util.json.JsonUtilsX;
import cn.iocoder.yudao.framework.test.core.ut.SomleBaseSpringTest;
import com.somle.kingdee.model.KingdeePurOrderReqVO;
import com.somle.kingdee.model.KingdeePurRequestReqVO;
import com.somle.kingdee.util.SignatureUtils;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.codec.binary.Base64;
import org.apache.commons.codec.binary.Hex;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.springframework.context.annotation.Import;
import org.springframework.test.annotation.Rollback;

import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.TreeMap;

import static org.junit.jupiter.api.Assertions.assertEquals;

@Disabled
@Slf4j
@Import(KingdeeService.class)
public class KingdeeServiceTest extends SomleBaseSpringTest {
    @Resource
    KingdeeService service;
    // 定义所有的字段名称及对应表
    String[][] fields = {
        {"商品", "bd_material"},
        {"仓库", "bd_store"},
        {"类别", "bd_materialgroup"},
        {"仓库分类", "bd_storegroup"},
        {"品牌分类", "bd_brandgroup"},
        {"仓位", "bd_space"},
        {"品牌", "bd_brand"},
        {"辅助属性", "bd_auxdetail"},
        {"商品标签", "bd_label"},
        {"辅助资料", "bd_auxinfo"},
        {"品标签分类", "bd_labelgroup"},
        {"辅助资料分类", "bd_auxinfotype"},
        {"库存", "inv_inventory_entity"},
        {"物流公司", "bd_logisticscompany"},
        {"部门", "bd_department"},
        {"外部基础资料", "iac_virtualbase"},
        {"职员", "bd_employee"},
        {"科目", "bd_account"},
        {"币别", "bd_currency"},
        {"零售门店", "lsbd_store"},
        {"客户", "bd_customer"},
        {"会员级别", "lsbd_mblevel"},
        {"客户分类", "bd_customergroup"},
        {"会员信息", "lsbd_mb"},
        {"供应商", "bd_supplier"},
        {"门店商品上下架", "lsbd_onoffshelf"},
        {"供应商分类", "bd_suppliergroup"},
        {"零售单据", "store_bill"},
        {"账户", "bd_bank"},
        {"门店零售价", "lsst_retail_price"},
        {"结算方式", "bd_settlementtype"},
        {"收入类别", "bd_paccttype"},
        {"计量单位", "bd_measureunits_new"}
    };

//    @BeforeEach
//    void setUp() {
//        MockitoAnnotations.openMocks(this);
//    }

    @Test
    void getApiString() {
        // Arrange
        String reqMtd = "GET";
        String endUrl = "/jdyconnector/app_management/kingdee_auth_token";
        String nonce = "4427456950";
        String timestamp = "1670305063559";
        TreeMap<String, String> params = new TreeMap<>();
        params.put("app_key", "bVZgAZOv1");
        params.put("app_signature", "MzZlYTk0ODk4MWZlNjdiODNmNWU4YzViNzYxNGM5MTFlOGJkN2NjMzk0MTJkZGNhZGM0NzZhN2YxZDJmOTlkZA==");
        String expectedOutput = "GET\n" + //
            "%2Fjdyconnector%2Fapp_management%2Fkingdee_auth_token\n" + //
            "app_key=bVZgAZOv1&app_signature=MzZlYTk0ODk4MWZlNjdiODNmNWU4YzViNzYxNGM5MTFlOGJkN2NjMzk0MTJkZGNhZGM0NzZhN2YxZDJmOTlkZA%253D%253D\n" + //
            "x-api-nonce:4427456950\n" + //
            "x-api-timestamp:1670305063559\n";

        // Act
        String actualOutput = SignatureUtils.getApiString(reqMtd, endUrl, params, nonce, timestamp);

        // Assert
        assertEquals(expectedOutput, actualOutput);
    }

    @Test
    void hmac256() {
        // Arrange
        String clientSecret = "f2adcfef73369bfc4e1384677d38a0ff";
        String apiString = "GET\n" + //
            "%2Fjdyconnector%2Fapp_management%2Fkingdee_auth_token\n" + //
            "app_key=bVZgAZOv1&app_signature=MzZlYTk0ODk4MWZlNjdiODNmNWU4YzViNzYxNGM5MTFlOGJkN2NjMzk0MTJkZGNhZGM0NzZhN2YxZDJmOTlkZA%253D%253D\n" + //
            "x-api-nonce:4427456950\n" + //
            "x-api-timestamp:1670305063559\n";
        String expectedOutput1 = "91be9b41b23da27c5a788028de71f5e09e9565e4c5a25f21cf9a07df68b50d51";
        String expectedOutput2 = "OTFiZTliNDFiMjNkYTI3YzVhNzg4MDI4ZGU3MWY1ZTA5ZTk1NjVlNGM1YTI1ZjIxY2Y5YTA3ZGY2OGI1MGQ1MQ==";

        // Act
        String actualOutput1 = Hex.encodeHexString(SignatureUtils.hmac256(clientSecret, apiString));
        String actualOutput2 = Base64.encodeBase64String(actualOutput1.getBytes());

        // Assert
        assertEquals(expectedOutput1, actualOutput1);
        assertEquals(expectedOutput2, actualOutput2);

    }

    // @Test
    // void getAppSignature() {
    //     // Arrange
    //     String appKey = "abc";
    //     String appSecret = "abc123";
    //     String expectedOutput1 = "ZDljMTI3NGIyNTE1MTRkYzlkNjc1MDNhYjUzMzgzNWMyY2M4YTdjMzdmNmM3YTVlNDkxMTkzNjdiOTFjNzUyZQ==";

    //     // Act
    //     String actualOutput1 = KingdeeClient.getAppSignature(appSecret, appKey);

    //     // Assert
    //     assertEquals(expectedOutput1, actualOutput1);

    // }

    @Test
    void refreshAuth() {
        service.refreshAuths();
    }

    @Test
    public void testGetAuxInfoType() {
        var client = service.getClients().get(0);
        var vo = new KingdeePurRequestReqVO();
        log.info(client.getAuxInfoTypeByNumber("BM").toString());
    }


    @Test
    public void testGetPurRequest() {
        var client = service.getClients().get(0);
        var vo = KingdeePurRequestReqVO.builder()
            .createStartTime(LocalDateTimeUtils.toTimestamp(LocalDateTime.now().minusDays(1)))
            .createEndTime(LocalDateTimeUtils.toTimestamp(LocalDateTime.now()))
            .build();
        log.info(JsonUtilsX.toJsonString(client.getPurRequest(vo)));
    }

    @Test
    public void testGetPurOrder() {
        var start = LocalDateTime.of(2024, 10, 1, 0, 0, 0).atZone(ZoneId.systemDefault()).toInstant();
        var end = LocalDateTime.of(2024, 11, 1, 0, 0, 0).atZone(ZoneId.systemDefault()).toInstant();
        var client = service.getClients().get(0);
        var vo = KingdeePurOrderReqVO.builder()
            .createStartTime(Timestamp.from(start))
            .createEndTime(Timestamp.from(end))
            .build();
        log.info(JsonUtilsX.toJSONObject(vo).toString());
        log.info(client.getPurOrder(vo).get(0).toString());
    }

    @Test
    @Rollback(false)
    public void testGetCustomField() {
        var client = service.getClients().get(0);
        client.refreshAuth();
        for (String[] field : fields) {
            log.info(client.getCustomField(field[1]).toString());
        }
//        log.info(client.getCustomField("").toString());
    }

    @Test
    public void testGetCustomFieldByDisplayName() {
        var client = service.getClients().get(0);
        log.info(client.getCustomFieldByDisplayName("bd_material", "部门").toString());
    }
}
