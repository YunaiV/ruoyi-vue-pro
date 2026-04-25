package cn.iocoder.yudao.module.ai.controller.admin.image;

import cn.iocoder.yudao.framework.common.pojo.CommonResult;
import cn.iocoder.yudao.module.ai.controller.admin.image.vo.fashion.AiFashionSmartChatReqVO;
import cn.iocoder.yudao.module.ai.controller.admin.image.vo.fashion.AiFashionSmartChatRespVO;
import cn.iocoder.yudao.module.ai.service.image.AiFashionConversationService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import static cn.iocoder.yudao.framework.common.pojo.CommonResult.success;
import static cn.iocoder.yudao.framework.security.core.util.SecurityFrameworkUtils.getLoginUserId;

/**
 * 管理后台 - AI 服装设计智能交互
 *
 * <p>提供"一句话出款"的智能交互能力，支持自然语言描述、多轮修改、批量生成。</p>
 *
 * @author deepay
 */
@Tag(name = "管理后台 - AI 服装设计智能交互")
@RestController
@RequestMapping("/ai/fashion/smart")
@Validated
@Slf4j
public class AiFashionSmartController {

    @Resource
    private AiFashionConversationService conversationService;

    @PostMapping("/chat")
    @Operation(summary = "智能对话（一句话出款）",
            description = "支持单字/短语/完整句子，自动解析意图并创建设计任务；返回任务ID及系统理解说明")
    @PreAuthorize("@ss.hasPermission('ai:image:fashion:create')")
    public CommonResult<AiFashionSmartChatRespVO> chat(@Valid @RequestBody AiFashionSmartChatReqVO reqVO) {
        AiFashionSmartChatRespVO resp = conversationService.chat(getLoginUserId(), reqVO);
        return success(resp);
    }

}
