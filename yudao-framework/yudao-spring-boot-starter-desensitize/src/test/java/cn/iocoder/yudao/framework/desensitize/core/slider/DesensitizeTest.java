package cn.iocoder.yudao.framework.desensitize.core.slider;

import cn.iocoder.yudao.framework.common.util.json.JsonUtils;
import cn.iocoder.yudao.framework.desensitize.core.regex.annotation.Email;
import cn.iocoder.yudao.framework.desensitize.core.regex.annotation.Regex;
import cn.iocoder.yudao.framework.desensitize.core.slider.annotation.Address;
import cn.iocoder.yudao.framework.desensitize.core.slider.annotation.BankCard;
import cn.iocoder.yudao.framework.desensitize.core.slider.annotation.CarLicense;
import cn.iocoder.yudao.framework.desensitize.core.slider.annotation.ChineseName;
import cn.iocoder.yudao.framework.desensitize.core.slider.annotation.FixedPhone;
import cn.iocoder.yudao.framework.desensitize.core.slider.annotation.IdCard;
import cn.iocoder.yudao.framework.desensitize.core.slider.annotation.Password;
import cn.iocoder.yudao.framework.desensitize.core.slider.annotation.PhoneNumber;
import cn.iocoder.yudao.framework.desensitize.core.slider.annotation.Slider;
import cn.iocoder.yudao.framework.test.core.ut.BaseMockitoUnitTest;
import lombok.Data;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class DesensitizeTest extends BaseMockitoUnitTest {

    @Test
    public void test() {
        DesensitizeDemo desensitizeDemo = new DesensitizeDemo();
        desensitizeDemo.setUserName("芋道源码");
        desensitizeDemo.setBankCard("9988002866797031");
        desensitizeDemo.setCarLicense("粤A66666");
        desensitizeDemo.setFixedPhone("01086551122");
        desensitizeDemo.setIdCard("530321199204074611");
        desensitizeDemo.setPassword("123456");
        desensitizeDemo.setPhoneNumber("13248765917");
        desensitizeDemo.setSlider1("ABCDEFG");
        desensitizeDemo.setSlider2("ABCDEFG");
        desensitizeDemo.setSlider3("ABCDEFG");
        desensitizeDemo.setEmail("1@eamil.com");
        desensitizeDemo.setRegex("你好，我是芋道源码");
        desensitizeDemo.setAddress("北京市海淀区上地十街10号");
        desensitizeDemo.setOrigin("芋道源码");

        DesensitizeDemo d = JsonUtils.parseObject(JsonUtils.toJsonString(desensitizeDemo), DesensitizeDemo.class);
        assertEquals("芋***", d.getUserName());
        assertEquals("998800********31", d.getBankCard());
        assertEquals("粤A6***6", d.getCarLicense());
        assertEquals("0108*****22", d.getFixedPhone());
        assertEquals("530321**********11", d.getIdCard());
        assertEquals("******", d.getPassword());
        assertEquals("132****5917", d.getPhoneNumber());
        assertEquals("#######", d.getSlider1());
        assertEquals("ABC*EFG", d.getSlider2());
        assertEquals("*******", d.getSlider3());
        assertEquals("1****@eamil.com", d.getEmail());
        assertEquals("你好，我是*", d.getRegex());
        assertEquals("北京市海淀区上地十街10号*", d.getAddress());
        assertEquals("芋道源码", d.getOrigin());
    }

    @Data
    public static class DesensitizeDemo {
        @ChineseName
        private String userName;
        @BankCard
        private String bankCard;
        @CarLicense
        private String carLicense;
        @FixedPhone
        private String fixedPhone;
        @IdCard
        private String idCard;
        @Password
        private String password;
        @PhoneNumber
        private String phoneNumber;
        @Slider(prefixKeep = 6,suffixKeep = 1,replacer = "#")
        private String slider1;
        @Slider(prefixKeep = 3,suffixKeep = 3)
        private String slider2;
        @Slider(prefixKeep = 10)
        private String slider3;
        @Email
        private String email;
        @Regex(regex = "芋道源码",replacer = "*")
        private String regex;
        @Address
        private String address;
        private String origin;
    }
}
