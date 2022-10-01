package cn.iocoder.yudao.ssodemo.controller;

import cn.iocoder.yudao.ssodemo.client.UserClient;
import cn.iocoder.yudao.ssodemo.client.dto.CommonResult;
import cn.iocoder.yudao.ssodemo.client.dto.user.UserInfoRespDTO;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;

@RestController
@RequestMapping("/user")
public class UserController {

    @Resource
    private UserClient userClient;

    /**
     * 获得当前登录用户的基本信息
     *
     * @return 用户信息；注意，实际项目中，最好创建对应的 ResponseVO 类，只返回必要的字段
     */
    @GetMapping("/get")
    public CommonResult<UserInfoRespDTO> getUser() {
        return userClient.getUser();
    }

}
