package cn.iocoder.yudao.module.member.controller.admin.level;

import cn.iocoder.yudao.framework.common.pojo.CommonResult;
import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.excel.core.util.ExcelUtils;
import cn.iocoder.yudao.framework.operatelog.core.annotations.OperateLog;
import cn.iocoder.yudao.module.member.controller.admin.level.vo.experience.MemberExperienceLogExcelVO;
import cn.iocoder.yudao.module.member.controller.admin.level.vo.experience.MemberExperienceLogExportReqVO;
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
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletResponse;
import javax.validation.Valid;
import java.io.IOException;
import java.util.Collection;
import java.util.List;

import static cn.iocoder.yudao.framework.common.pojo.CommonResult.success;
import static cn.iocoder.yudao.framework.operatelog.core.enums.OperateTypeEnum.EXPORT;

/**
 * @author owen
 */
@Tag(name = "管理后台 - 会员经验记录")
@RestController
@RequestMapping("/member/experience-log")
@Validated
public class MemberExperienceLogController {

    @Resource
    private MemberExperienceLogService experienceLogService;

    @DeleteMapping("/delete")
    @Operation(summary = "删除会员经验记录")
    @Parameter(name = "id", description = "编号", required = true)
    @PreAuthorize("@ss.hasPermission('member:experience-log:delete')")
    public CommonResult<Boolean> deleteExperienceLog(@RequestParam("id") Long id) {
        experienceLogService.deleteExperienceLog(id);
        return success(true);
    }

    @GetMapping("/get")
    @Operation(summary = "获得会员经验记录")
    @Parameter(name = "id", description = "编号", required = true, example = "1024")
    @PreAuthorize("@ss.hasPermission('member:experience-log:query')")
    public CommonResult<MemberExperienceLogRespVO> getExperienceLog(@RequestParam("id") Long id) {
        MemberExperienceLogDO experienceLog = experienceLogService.getExperienceLog(id);
        return success(MemberExperienceLogConvert.INSTANCE.convert(experienceLog));
    }

    @GetMapping("/list")
    @Operation(summary = "获得会员经验记录列表")
    @Parameter(name = "ids", description = "编号列表", required = true, example = "1024,2048")
    @PreAuthorize("@ss.hasPermission('member:experience-log:query')")
    public CommonResult<List<MemberExperienceLogRespVO>> getExperienceLogList(@RequestParam("ids") Collection<Long> ids) {
        List<MemberExperienceLogDO> list = experienceLogService.getExperienceLogList(ids);
        return success(MemberExperienceLogConvert.INSTANCE.convertList(list));
    }

    @GetMapping("/page")
    @Operation(summary = "获得会员经验记录分页")
    @PreAuthorize("@ss.hasPermission('member:experience-log:query')")
    public CommonResult<PageResult<MemberExperienceLogRespVO>> getExperienceLogPage(@Valid MemberExperienceLogPageReqVO pageVO) {
        PageResult<MemberExperienceLogDO> pageResult = experienceLogService.getExperienceLogPage(pageVO);
        return success(MemberExperienceLogConvert.INSTANCE.convertPage(pageResult));
    }

    @GetMapping("/export-excel")
    @Operation(summary = "导出会员经验记录 Excel")
    @PreAuthorize("@ss.hasPermission('member:experience-log:export')")
    @OperateLog(type = EXPORT)
    public void exportExperienceLogExcel(@Valid MemberExperienceLogExportReqVO exportReqVO,
                                         HttpServletResponse response) throws IOException {
        List<MemberExperienceLogDO> list = experienceLogService.getExperienceLogList(exportReqVO);
        // 导出 Excel
        List<MemberExperienceLogExcelVO> datas = MemberExperienceLogConvert.INSTANCE.convertList02(list);
        ExcelUtils.write(response, "会员经验记录.xls", "数据", MemberExperienceLogExcelVO.class, datas);
    }

}
