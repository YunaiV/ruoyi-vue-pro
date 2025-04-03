package cn.iocoder.yudao;

import cn.iocoder.yudao.framework.common.pojo.CommonResult;
import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.module.wms.controller.admin.warehouse.WmsWarehouseController;
import cn.iocoder.yudao.module.wms.controller.admin.warehouse.vo.WmsWarehousePageReqVO;
import cn.iocoder.yudao.module.wms.controller.admin.warehouse.vo.WmsWarehouseRespVO;
import cn.iocoder.yudao.test.BootApplication;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.context.ApplicationContext;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;

import java.util.HashMap;
import java.util.Map;


@Slf4j
@SpringBootTest(classes = BootApplication.class,webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class JoinTest {

    @Autowired
    private TestRestTemplate restTemplate;

    @Resource
    WmsWarehouseController wmsWarehouseController;
//    @Resource
//    WmsInboundMapper inboundMapper;
//
//    @Resource
//    @Resource
//    WmsInboundItemServiceImpl inboundService;
//    @MockitoBean
//    private ErpProductApi productApi;
//
//
//    @Test
//    void testSubscription() {
//        WmsInboundPageReqVO pageReqVO = new WmsInboundPageReqVO();
//        PageResult<WmsInboundDO> pageResult = inboundMapper.selectPage(pageReqVO);
//        System.out.println();
//    }


    @Resource
    private ApplicationContext applicationContext;


    private String login() {

        HttpHeaders headers = new HttpHeaders();
        headers.add("tenant-id", "50001");

        Map<String,Object> reqVO = new HashMap<>();
        reqVO.put("tenantName","宁波索迈");
        reqVO.put("username","wangdongyu");
        reqVO.put("password","qwe123,./");
        reqVO.put("rememberMe",true);
        HttpEntity<Map> entity = new HttpEntity<>(reqVO, headers);



        ResponseEntity<CommonResult> forEntity1 = restTemplate.postForEntity("/admin-api/system/auth/login",entity, CommonResult.class);
        Map<String,Object> data= (Map<String, Object>) forEntity1.getBody().getData();
        //JSONObject data = jsonObject.getJSONObject("data");
        String accessToken = (String) data.get("accessToken");
        //return accessToken;
        return accessToken;
    }

    @Test
    public void printAllBeans() {

        String accessToken = login();

        HttpHeaders headers = new HttpHeaders();
        headers.add("tenant-id", "50001");
        headers.add("Authorization", "Bearer "+accessToken);

        HttpEntity<WmsWarehousePageReqVO> entity = new HttpEntity<>(new WmsWarehousePageReqVO(), headers);
        //ResponseEntity<CommonResult> forEntity3 = restTemplate.getForEntity("/admin-api/wms/warehouse/page", entity,  CommonResult.class);

        restTemplate.exchange("/admin-api/wms/warehouse/page", HttpMethod.GET,entity,  CommonResult.class);

        CommonResult<PageResult<WmsWarehouseRespVO>> result = wmsWarehouseController.getWarehousePage(new WmsWarehousePageReqVO());
        if(!result.isError()) {
            System.out.println();
        }


        String[] beanNames = applicationContext.getBeanDefinitionNames();
        System.out.println("Beans provided by Spring:");

        for (String beanName : beanNames) {
            try {
                Object bean = applicationContext.getBean(beanName);
                //System.out.println(beanName);
                System.out.println(beanName + "\t\t" + bean.getClass().getName());
            } catch (Exception e) {
                // Handle the exception if the bean cannot be retrieved
                //e.printStackTrace();
                System.err.println("====================== Error "+beanName+" =============================");
            }
        }
    }



}