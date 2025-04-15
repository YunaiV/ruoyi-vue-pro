package cn.iocoder.yudao.test;

import jakarta.annotation.Resource;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.context.ApplicationContext;
import org.springframework.web.util.DefaultUriBuilderFactory;

import java.util.HashMap;
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

    private static Map<Class<? extends RestClient>,RestClient> CLIENTS = new HashMap<>();

    public <C extends RestClient> C getClient(Class<C> clazz) {

        if(this.testRestTemplate==null) {
            String baseUrl =profile.getUrl();
            DefaultUriBuilderFactory uriBuilderFactory = new DefaultUriBuilderFactory(baseUrl);
            this.testRestTemplate = new TestRestTemplate();
            this.testRestTemplate.setUriTemplateHandler(uriBuilderFactory);
        }


        RestClient client = CLIENTS.get(clazz);
        if(client==null) {
            try {
                client = clazz.getConstructor(Profile.class, TestRestTemplate.class).newInstance(profile, testRestTemplate);
                CLIENTS.put(clazz, client);
            } catch (Exception e){
                throw new RuntimeException(e);
            }
        }
        return (C)client;
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
