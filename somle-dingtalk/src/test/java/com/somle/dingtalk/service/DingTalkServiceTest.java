package com.somle.dingtalk.service;

import cn.iocoder.yudao.framework.test.core.ut.SomleBaseSpringTest;
import com.somle.dingtalk.repository.DingTalkTokenRepository;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.springframework.context.annotation.Import;

import java.lang.reflect.Method;

import static org.junit.jupiter.api.Assertions.*;

@Disabled
@Slf4j
@Import({DingTalkService.class})
class DingTalkServiceTest extends SomleBaseSpringTest {
    @Resource
    DingTalkService service;

    @Resource
    DingTalkTokenRepository tokenRepository;



    public static void printGetters(Object obj) {
        Method[] methods = obj.getClass().getMethods();
        for (Method method : methods) {
            if (method.getName().startsWith("get") && method.getParameterCount() == 0) {
                try {
                    Object value = method.invoke(obj);
                    System.out.println(method.getName() + ": " + value);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
    }

    @Test
    void refreshAuth() {
        service.refreshAuth();
    }

    @Test
    void getDepartment() {
        System.out.println(service.getDepartment(1l));
    }


    @Test
    void getUserIds() {
        System.out.println(service.getUserIds(697868464l));
    }

    @Test
    void getUserDetail() {
        var user = service.getUserDetail("manager4397");
        printGetters(user);
        assertEquals("丁晨", user.getName());
    }

//    @Test
//    void testRobot() {
//        var token = "";
//        service.sendRobotMessage("a test",token);
//    }
}