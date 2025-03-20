package cn.iocoder.yudao.module.ai.controller.admin.creation;

import cn.iocoder.yudao.framework.common.pojo.CommonResult;
import cn.iocoder.yudao.module.ai.controller.admin.creation.vo.style.AiartReplaceBackgroundReqVO;
import cn.iocoder.yudao.module.ai.controller.admin.creation.vo.style.AiartReplaceBackgroundResVO;
import cn.iocoder.yudao.module.ai.service.creation.AiartCreationService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import jakarta.validation.Valid;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import static cn.iocoder.yudao.framework.common.pojo.CommonResult.success;
import static cn.iocoder.yudao.framework.security.core.util.SecurityFrameworkUtils.getLoginUserId;

@Tag(name = "管理后台 - 艺术创作")
@RestController
@RequestMapping("/ai/artcreation")
@Validated
public class AiartCreationController {

    @Resource
    private AiartCreationService aiartCreationService;

    @Operation(summary = "商品图替换背景")
    @PostMapping("/replacebackground")
    public CommonResult<AiartReplaceBackgroundResVO> replaceBackground(@Valid @RequestBody AiartReplaceBackgroundReqVO replaceBackgroundReqVO) {
        return success(aiartCreationService.replaceBackground(getLoginUserId(), replaceBackgroundReqVO));
    }


}