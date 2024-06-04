package cn.iocoder.yudao.module.promotion.controller.app.kefu;

import cn.iocoder.yudao.framework.common.pojo.CommonResult;
import cn.iocoder.yudao.framework.common.util.object.BeanUtils;
import cn.iocoder.yudao.framework.security.core.annotations.PreAuthenticated;
import cn.iocoder.yudao.module.promotion.controller.app.kefu.vo.conversation.AppKeFuConversationRespVO;
import cn.iocoder.yudao.module.promotion.service.kefu.KeFuConversationService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import static cn.iocoder.yudao.framework.common.pojo.CommonResult.success;
import static cn.iocoder.yudao.framework.security.core.util.SecurityFrameworkUtils.getLoginUserId;

@Tag(name = "用户 APP - 客户会话")
@RestController
@RequestMapping("/promotion/kefu-conversation")
@Validated
public class AppKeFuConversationController {

    @Resource
    private KeFuConversationService conversationService;

    @GetMapping("/get")
    @Operation(summary = "获得客服会话")
    @PreAuthenticated
    public CommonResult<AppKeFuConversationRespVO> getDiyPage() {
        return success(BeanUtils.toBean(conversationService.getOrCreateConversation(getLoginUserId()), AppKeFuConversationRespVO.class));
    }

}