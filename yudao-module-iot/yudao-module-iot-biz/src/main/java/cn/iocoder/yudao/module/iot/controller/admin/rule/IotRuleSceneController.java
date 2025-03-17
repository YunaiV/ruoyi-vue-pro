package cn.iocoder.yudao.module.iot.controller.admin.rule;

import cn.iocoder.yudao.module.iot.service.rule.IotRuleSceneService;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import jakarta.annotation.security.PermitAll;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "管理后台 - IoT 规则场景")
@RestController
@RequestMapping("/iot/rule-scene")
@Validated
public class IotRuleSceneController {

    @Resource
    private IotRuleSceneService ruleSceneService;

    @GetMapping("/test")
    @PermitAll
    public void test() {
        ruleSceneService.test();
    }

}
