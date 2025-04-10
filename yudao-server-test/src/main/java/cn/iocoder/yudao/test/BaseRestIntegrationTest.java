package cn.iocoder.yudao.test;

import cn.hutool.core.bean.BeanUtil;
import cn.iocoder.yudao.framework.common.pojo.CommonResult;
import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.common.util.json.JsonUtilsX;
import com.somle.esb.enums.TenantId;
import jakarta.annotation.Resource;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.context.ApplicationContext;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.util.DefaultUriBuilderFactory;
import org.springframework.web.util.UriComponentsBuilder;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author: LeeFJ
 * @date: 2025/4/3 16:00
 * @description: 集成测试基类
 */

@SpringBootTest(classes = TestStarter.class,webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@Slf4j
public class BaseRestIntegrationTest {

    @Setter
    private Profile profile = Profile.LOCAL;

    @Resource
    private TestRestTemplate testRestTemplate;

    @Resource
    private ApplicationContext applicationContext;

    private static final ThreadLocal<String> ACCESS_TOKEN = new ThreadLocal<>();


    protected String login() {

        if(testRestTemplate==null) {
            String baseUrl =profile.getUrl();
            DefaultUriBuilderFactory uriBuilderFactory = new DefaultUriBuilderFactory(baseUrl);
            this.testRestTemplate = new TestRestTemplate();
            this.testRestTemplate.setUriTemplateHandler(uriBuilderFactory);
        }

        HttpHeaders headers = new HttpHeaders();
        headers.add("tenant-id", TenantId.DEFAULT.getId()+"");

        Map<String,Object> reqVO = new HashMap<>();
        reqVO.put("tenantName","宁波索迈");
        reqVO.put("username",profile.getName());
        reqVO.put("password",profile.getPassword());
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
        url=processUrl(url);
        MultiValueMap<String, String> multiValueMap = new LinkedMultiValueMap<>();
        Map<String,?> map = BeanUtil.beanToMap(param,false,true);
        for (Map.Entry<String, ?> entry : map.entrySet()) {
            multiValueMap.add(entry.getKey(), entry.getValue().toString());
        }
        url = UriComponentsBuilder.fromHttpUrl(url).queryParams(multiValueMap).toUriString();

        HttpHeaders headers = getDefaultHeader();
        HttpEntity<Object> entity = new HttpEntity<>(param, headers);
        ResponseEntity<CommonResult> responseEntity=testRestTemplate.exchange(url, HttpMethod.GET,entity,CommonResult.class);
        log.info("\n\n  GET  {} \n    result: {}\n\n",url,param,responseEntity.getBody());
        return responseEntity.getBody();
    }

    public CommonResult post(String url, Object param) {
        url = processUrl(url);
        HttpHeaders headers = getDefaultHeader();
        HttpEntity<Object> entity = new HttpEntity<>(param, headers);
        ResponseEntity<CommonResult> responseEntity=testRestTemplate.exchange(url, HttpMethod.POST,entity,CommonResult.class);
        log.info("\n\n  POST {} \n    params: {} \n    result: {}\n\n",url, JsonUtilsX.toJsonString(param),responseEntity.getBody());
        return responseEntity.getBody();
    }

    public CommonResult put(String url, Object param) {
        url = processUrl(url);
        HttpHeaders headers = getDefaultHeader();
        HttpEntity<Object> entity = new HttpEntity<>(param, headers);
        ResponseEntity<CommonResult> responseEntity=testRestTemplate.exchange(url, HttpMethod.PUT,entity,CommonResult.class);
        log.info("\n\n  PUT {} \n    params: {} \n    result: {}\n\n",url, JsonUtilsX.toJsonString(param),responseEntity.getBody());
        return responseEntity.getBody();
    }

    private String processUrl(String url) {
        url = url.trim();

        if(url.startsWith("/admin-api/")) {
            return url;
        }

        if(url.startsWith("/")) {
            url = url.substring(1);
        }
        if(!url.startsWith("admin-api/"))
        {
            url="/admin-api/"+url;
        }
        return url;
    }

    public <VO> CommonResult<Long> create(String url, VO param) {
        CommonResult result = post(url,param);
        if(result.isSuccess()) {
           Long id = Long.valueOf(result.getData().toString());
           result.setData(id);
        }
        return result;
    }

    public <VO> CommonResult<Boolean> update(String url, VO param) {
        CommonResult result = put(url,param);
        if(result.isSuccess()) {
            Boolean data = Boolean.valueOf(result.getData().toString());
            result.setData(data);
        }
        return result;
    }

    public <REQ,RESP>  CommonResult<PageResult<RESP>> getPage(String url, REQ reqVO, Class<RESP> respClass) {
        CommonResult result = get(url,reqVO, PageResult.class);
        if(result.isError()) {
            return result;
        }
        List<RESP> voList = new ArrayList<>();
        Map data=(Map) result.getData();
        PageResult pageResult = BeanUtil.toBean(data, PageResult.class);
        result.setData(pageResult);
        List<Map> mapList = pageResult.getList();

        for (Map map : mapList) {
            RESP vo = BeanUtil.toBean(map,respClass);
            voList.add(vo);
        }
        pageResult.setList(voList);
        result.setData(pageResult);
        return result;
    }

    public <REQ,RESP>  CommonResult<List<RESP>> getSimpleList(String url, REQ reqVO, Class<RESP> respClass) {
        CommonResult result = get(url,reqVO, PageResult.class);
        if(result.isError()) {
            return result;
        }
        List<RESP> voList = new ArrayList<>();
        List<Map> data=(List) result.getData();

        for (Map map : data) {
            RESP vo = BeanUtil.toBean(map,respClass);
            voList.add(vo);
        }
        result.setData(voList);
        return result;
    }

    public <RESP>  CommonResult<RESP> getOne(String url, Long id, Class<RESP> respClass) {
        CommonResult result = get(url,Map.of("id",id), PageResult.class);
        if(result.isError()) {
            return result;
        }
        Map data=(Map)result.getData();
        RESP vo = BeanUtil.toBean(data,respClass);
        result.setData(vo);
        return result;
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
