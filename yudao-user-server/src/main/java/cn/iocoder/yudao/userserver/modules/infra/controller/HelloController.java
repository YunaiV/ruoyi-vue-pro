package cn.iocoder.yudao.userserver.modules.infra.controller;

import cn.iocoder.yudao.framework.security.core.annotations.PreAuthenticated;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.access.annotation.Secured;
import org.springframework.security.access.prepost.PreAuthorize;
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

    @RequestMapping("/user/info")
    @PreAuthenticated
    public String xx() {
        return "none";
    }

}
