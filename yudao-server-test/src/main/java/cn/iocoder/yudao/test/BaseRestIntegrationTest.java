package cn.iocoder.yudao.test;

import cn.iocoder.yudao.framework.common.pojo.CommonResult;
import com.somle.esb.enums.TenantId;
import jakarta.annotation.Resource;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.context.ApplicationContext;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.web.util.DefaultUriBuilderFactory;

import java.util.HashMap;
import java.util.Map;

/**
 * @author: LeeFJ
 * @date: 2025/4/3 16:00
 * @description: 集成测试基类
 */

@SpringBootTest(classes = TestStarter.class,webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class BaseRestIntegrationTest {

    @Resource
    private TestRestTemplate testRestTemplate;

    @Resource
    private ApplicationContext applicationContext;

    private static final ThreadLocal<String> ACCESS_TOKEN = new ThreadLocal<>();


    protected String login() {

        if(testRestTemplate==null) {
            String baseUrl ="http://192.168.10.131:9369";
            DefaultUriBuilderFactory uriBuilderFactory = new DefaultUriBuilderFactory(baseUrl);
            this.testRestTemplate = new TestRestTemplate();
            this.testRestTemplate.setUriTemplateHandler(uriBuilderFactory);
        }

        HttpHeaders headers = new HttpHeaders();
        headers.add("tenant-id", TenantId.DEFAULT.getId()+"");

        Map<String,Object> reqVO = new HashMap<>();
        reqVO.put("tenantName","宁波索迈");
        reqVO.put("username","wangdongyu");
        reqVO.put("password","qwe123,./");
        reqVO.put("rememberMe",true);
        HttpEntity<Map> entity = new HttpEntity<>(reqVO, headers);
        //
        ResponseEntity<CommonResult> forEntity1 = testRestTemplate.postForEntity("/admin-api/system/auth/login",entity, CommonResult.class);
        Map<String,Object> data= (Map<String, Object>) forEntity1.getBody().getData();
        String accessToken=(String) data.get("accessToken");
        ACCESS_TOKEN.set(accessToken);
        return accessToken;
    }

    public HttpHeaders getDefaultHeader() {
        String accessToken = ACCESS_TOKEN.get();
        if(accessToken==null) {
            accessToken = login();
        }
        HttpHeaders headers = new HttpHeaders();
        headers.add("tenant-id", TenantId.DEFAULT.getId()+"");
        headers.add("Authorization", "Bearer "+accessToken);
        return headers;
    }


    public <T> CommonResult<T> get(String url,Object param,Class<T> dataType) {
        HttpHeaders headers = getDefaultHeader();
        HttpEntity<Object> entity = new HttpEntity<>(param, headers);
        ResponseEntity<CommonResult> responseEntity=testRestTemplate.exchange(url, HttpMethod.GET,entity,CommonResult.class);
        return responseEntity.getBody();
    }


    public void printAllBeans() {

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
