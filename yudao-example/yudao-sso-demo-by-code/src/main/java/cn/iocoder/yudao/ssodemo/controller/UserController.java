package cn.iocoder.yudao.ssodemo.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/user")
public class UserController {

    /**
     * 获得当前登录用户的基本信息
     *
     * @return TODO
     */
    @GetMapping("/get")
    public String getUser() {
        return "";
    }

}
