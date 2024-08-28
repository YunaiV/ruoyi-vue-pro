package cn.iocoder.yudao.module.ai.controller.admin.knowledge;

import cn.iocoder.yudao.framework.common.pojo.CommonResult;
import cn.iocoder.yudao.module.ai.controller.admin.knowledge.vo.AiKnowledgeCreateMyReqVO;
import cn.iocoder.yudao.module.ai.controller.admin.knowledge.vo.AiKnowledgeUpdateMyReqVO;
import cn.iocoder.yudao.module.ai.service.knowledge.AiKnowledgeService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;

import static cn.iocoder.yudao.framework.common.pojo.CommonResult.success;
import static cn.iocoder.yudao.framework.security.core.util.SecurityFrameworkUtils.getLoginUserId;

@Tag(name = "管理后台 - AI 知识库")
@RestController
@RequestMapping("/ai/knowledge")
public class AiKnowledgeController {

    @Resource
    private AiKnowledgeService knowledgeBaseService;

    @PostMapping("/create-my")
    @Operation(summary = "创建【我的】知识库")
    public CommonResult<Long> createKnowledgeMy(@RequestBody @Valid AiKnowledgeCreateMyReqVO createReqVO) {
        return success(knowledgeBaseService.createKnowledgeMy(createReqVO, getLoginUserId()));
    }


    @PutMapping("/update-my")
    @Operation(summary = "更新【我的】知识库")
    public CommonResult<Boolean> updateKnowledgeMy(@RequestBody @Valid AiKnowledgeUpdateMyReqVO updateReqVO) {
        knowledgeBaseService.updateKnowledgeMy(updateReqVO, getLoginUserId());
        return success(true);
    }


}
