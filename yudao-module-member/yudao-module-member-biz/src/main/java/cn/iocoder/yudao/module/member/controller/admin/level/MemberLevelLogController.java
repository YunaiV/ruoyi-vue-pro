package cn.iocoder.yudao.module.member.controller.admin.level;

import cn.iocoder.yudao.framework.common.pojo.CommonResult;
import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.excel.core.util.ExcelUtils;
import cn.iocoder.yudao.framework.operatelog.core.annotations.OperateLog;
import cn.iocoder.yudao.module.member.controller.admin.level.vo.log.MemberLevelLogExcelVO;
import cn.iocoder.yudao.module.member.controller.admin.level.vo.log.MemberLevelLogExportReqVO;
import cn.iocoder.yudao.module.member.controller.admin.level.vo.log.MemberLevelLogPageReqVO;
import cn.iocoder.yudao.module.member.controller.admin.level.vo.log.MemberLevelLogRespVO;
import cn.iocoder.yudao.module.member.convert.level.MemberLevelLogConvert;
import cn.iocoder.yudao.module.member.dal.dataobject.level.MemberLevelLogDO;
import cn.iocoder.yudao.module.member.service.level.MemberLevelLogService;
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
@Tag(name = "管理后台 - 会员等级记录")
@RestController
@RequestMapping("/member/level-log")
@Validated
public class MemberLevelLogController {

    @Resource
    private MemberLevelLogService levelLogService;

    @DeleteMapping("/delete")
    @Operation(summary = "删除会员等级记录")
    @Parameter(name = "id", description = "编号", required = true)
    @PreAuthorize("@ss.hasPermission('member:level-log:delete')")
    public CommonResult<Boolean> deleteLevelLog(@RequestParam("id") Long id) {
        levelLogService.deleteLevelLog(id);
        return success(true);
    }

    @GetMapping("/get")
    @Operation(summary = "获得会员等级记录")
    @Parameter(name = "id", description = "编号", required = true, example = "1024")
    @PreAuthorize("@ss.hasPermission('member:level-log:query')")
    public CommonResult<MemberLevelLogRespVO> getLevelLog(@RequestParam("id") Long id) {
        MemberLevelLogDO levelLog = levelLogService.getLevelLog(id);
        return success(MemberLevelLogConvert.INSTANCE.convert(levelLog));
    }

    @GetMapping("/list")
    @Operation(summary = "获得会员等级记录列表")
    @Parameter(name = "ids", description = "编号列表", required = true, example = "1024,2048")
    @PreAuthorize("@ss.hasPermission('member:level-log:query')")
    public CommonResult<List<MemberLevelLogRespVO>> getLevelLogList(@RequestParam("ids") Collection<Long> ids) {
        List<MemberLevelLogDO> list = levelLogService.getLevelLogList(ids);
        return success(MemberLevelLogConvert.INSTANCE.convertList(list));
    }

    @GetMapping("/page")
    @Operation(summary = "获得会员等级记录分页")
    @PreAuthorize("@ss.hasPermission('member:level-log:query')")
    public CommonResult<PageResult<MemberLevelLogRespVO>> getLevelLogPage(@Valid MemberLevelLogPageReqVO pageVO) {
        PageResult<MemberLevelLogDO> pageResult = levelLogService.getLevelLogPage(pageVO);
        return success(MemberLevelLogConvert.INSTANCE.convertPage(pageResult));
    }

    @GetMapping("/export-excel")
    @Operation(summary = "导出会员等级记录 Excel")
    @PreAuthorize("@ss.hasPermission('member:level-log:export')")
    @OperateLog(type = EXPORT)
    public void exportLevelLogExcel(@Valid MemberLevelLogExportReqVO exportReqVO,
                                    HttpServletResponse response) throws IOException {
        List<MemberLevelLogDO> list = levelLogService.getLevelLogList(exportReqVO);
        // 导出 Excel
        List<MemberLevelLogExcelVO> datas = MemberLevelLogConvert.INSTANCE.convertList02(list);
        ExcelUtils.write(response, "会员等级记录.xls", "数据", MemberLevelLogExcelVO.class, datas);
    }

}
