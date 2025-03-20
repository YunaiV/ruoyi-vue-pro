package cn.iocoder.yudao.module.ai.controller.admin.creation;

import cn.iocoder.yudao.framework.common.pojo.CommonResult;
import cn.iocoder.yudao.framework.common.util.object.BeanUtils;
import cn.iocoder.yudao.module.ai.controller.admin.creation.vo.style.BackgroundTemplateReqVO;
import cn.iocoder.yudao.module.ai.controller.admin.creation.vo.style.BackgroundTemplateResVO;
import cn.iocoder.yudao.module.ai.dal.dataobject.creation.AiartStyleBackgroundTemplateDO;
import cn.iocoder.yudao.module.ai.service.creation.AiartStyleService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import jakarta.validation.Valid;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

import static cn.iocoder.yudao.framework.common.pojo.CommonResult.success;

@Tag(name = "管理后台 - 艺术创作模版")
@RestController
@RequestMapping("/ai/artstyle")
@Validated
public class AiartStyleController {

    @Resource
    private AiartStyleService aiartStyleService;

    @PostMapping("/backgroundtemplate-list")
    @Operation(summary = "获取商品背景模版列表")
    @PreAuthorize("@ss.hasPermission('ai:artstyle-background:query')")
    public CommonResult<List<BackgroundTemplateResVO>> queryBackgroundStyle(@Valid @RequestBody BackgroundTemplateReqVO backgroundTemplateReqVO) {
        List<AiartStyleBackgroundTemplateDO> res = aiartStyleService.queryBackgroundStyle(backgroundTemplateReqVO);
        return success(BeanUtils.toBean(res,BackgroundTemplateResVO.class));
    }
}
