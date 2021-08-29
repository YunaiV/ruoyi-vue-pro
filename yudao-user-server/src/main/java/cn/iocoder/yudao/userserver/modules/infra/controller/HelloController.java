package cn.iocoder.yudao.userserver.modules.infra.controller;

import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * @author weir
 */
@Slf4j
@RestController
public class HelloController {

    @RequestMapping("/user/hello")
    public String hello(String hello) {
        return "echo + " + hello;
    }
}
