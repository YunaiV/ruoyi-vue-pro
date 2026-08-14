package cn.iocoder.yudao.module.hrm.controller.admin.recruit;

import cn.iocoder.yudao.framework.common.pojo.CommonResult;
import cn.iocoder.yudao.module.hrm.controller.admin.recruit.vo.config.HrmRecruitEliminateReasonSaveReqVO;
import cn.iocoder.yudao.module.hrm.service.recruit.config.HrmRecruitConfigService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import javax.annotation.Resource;
import javax.validation.Valid;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

import static cn.iocoder.yudao.framework.common.pojo.CommonResult.success;

@Tag(name = "管理后台 - HRM 招聘设置")
@RestController
@RequestMapping("/hrm/recruit/config")
@Validated
public class HrmRecruitConfigController {

    @Resource
    private HrmRecruitConfigService recruitConfigService;

    @PostMapping("/eliminate-reason/save")
    @Operation(summary = "保存招聘淘汰原因")
    @PreAuthorize("@ss.hasPermission('hrm:recruit:config:update')")
    public CommonResult<Boolean> saveRecruitEliminateReason(
            @Valid @RequestBody HrmRecruitEliminateReasonSaveReqVO saveReqVO) {
        recruitConfigService.saveRecruitEliminateReason(saveReqVO);
        return success(true);
    }

    @GetMapping("/eliminate-reason/list")
    @Operation(summary = "获得招聘淘汰原因列表")
    @PreAuthorize("@ss.hasPermission('hrm:recruit:config:query') "
            + "or @ss.hasPermission('hrm:recruit:candidate:update')")
    public CommonResult<List<String>> getRecruitEliminateReasonList() {
        return success(recruitConfigService.getRecruitEliminateReasonList());
    }

}
