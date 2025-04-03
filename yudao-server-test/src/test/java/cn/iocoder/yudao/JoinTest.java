package cn.iocoder.yudao;

import cn.iocoder.yudao.framework.common.pojo.CommonResult;
import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.module.wms.controller.admin.warehouse.vo.WmsWarehousePageReqVO;
import cn.iocoder.yudao.test.BaseRestIntegrationTest;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;


@Slf4j
public class JoinTest extends BaseRestIntegrationTest {


    @Test
    public void testWarehousePage() {

        CommonResult result = get("/admin-api/wms/warehouse/page",new WmsWarehousePageReqVO(), PageResult.class);
        System.out.println(result);
        //Assert.equals(1,2);
    }


    public static void main(String[] args) {
        JoinTest joinTest = new JoinTest();
        joinTest.testWarehousePage();
    }



}