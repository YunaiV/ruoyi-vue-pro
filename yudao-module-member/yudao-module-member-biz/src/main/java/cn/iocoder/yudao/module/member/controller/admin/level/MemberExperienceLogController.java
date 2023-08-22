package cn.iocoder.yudao.module.member.controller.admin.level;

import cn.iocoder.yudao.framework.common.pojo.CommonResult;
import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.module.member.controller.admin.level.vo.experience.MemberExperienceLogPageReqVO;
import cn.iocoder.yudao.module.member.controller.admin.level.vo.experience.MemberExperienceLogRespVO;
import cn.iocoder.yudao.module.member.convert.level.MemberExperienceLogConvert;
import cn.iocoder.yudao.module.member.dal.dataobject.level.MemberExperienceLogDO;
import cn.iocoder.yudao.module.member.service.level.MemberExperienceLogService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import javax.validation.Valid;

import static cn.iocoder.yudao.framework.common.pojo.CommonResult.success;

// TODO @疯狂：要不 Log 改成 Record，和 PointRecord 保持一致
@Tag(name = "管理后台 - 会员经验记录")
@RestController
@RequestMapping("/member/experience-log")
@Validated
public class MemberExperienceLogController {

    @Resource
    private MemberExperienceLogService experienceLogService;

    @GetMapping("/get")
    @Operation(summary = "获得会员经验记录")
    @Parameter(name = "id", description = "编号", required = true, example = "1024")
    @PreAuthorize("@ss.hasPermission('member:experience-log:query')")
    public CommonResult<MemberExperienceLogRespVO> getExperienceLog(@RequestParam("id") Long id) {
        MemberExperienceLogDO experienceLog = experienceLogService.getExperienceLog(id);
        return success(MemberExperienceLogConvert.INSTANCE.convert(experienceLog));
    }

    @GetMapping("/page")
    @Operation(summary = "获得会员经验记录分页")
    @PreAuthorize("@ss.hasPermission('member:experience-log:query')")
    public CommonResult<PageResult<MemberExperienceLogRespVO>> getExperienceLogPage(@Valid MemberExperienceLogPageReqVO pageVO) {
        PageResult<MemberExperienceLogDO> pageResult = experienceLogService.getExperienceLogPage(pageVO);
        return success(MemberExperienceLogConvert.INSTANCE.convertPage(pageResult));
    }

}
