package cn.iocoder.yudao.module.member.controller.admin.point;

import cn.iocoder.yudao.framework.common.pojo.CommonResult;
import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.excel.core.util.ExcelUtils;
import cn.iocoder.yudao.framework.operatelog.core.annotations.OperateLog;
import cn.iocoder.yudao.module.member.controller.admin.point.vo.recrod.*;
import cn.iocoder.yudao.module.member.convert.point.MemberPointRecordConvert;
import cn.iocoder.yudao.module.member.dal.dataobject.point.MemberPointRecordDO;
import cn.iocoder.yudao.module.member.service.point.MemberPointRecordService;
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

@Tag(name = "管理后台 - 用户积分记录")
@RestController
@RequestMapping("/point/record")
@Validated
public class MemberPointRecordController {

    @Resource
    private MemberPointRecordService recordService;

    @PutMapping("/update")
    @Operation(summary = "更新用户积分记录")
    @PreAuthorize("@ss.hasPermission('point:record:update')")
    public CommonResult<Boolean> updateRecord(@Valid @RequestBody MemberPointRecordUpdateReqVO updateReqVO) {
        recordService.updateRecord(updateReqVO);
        return success(true);
    }

    @GetMapping("/get")
    @Operation(summary = "获得用户积分记录")
    @Parameter(name = "id", description = "编号", required = true, example = "1024")
    @PreAuthorize("@ss.hasPermission('point:record:query')")
    public CommonResult<MemberPointRecordRespVO> getRecord(@RequestParam("id") Long id) {
        MemberPointRecordDO record = recordService.getRecord(id);
        return success(MemberPointRecordConvert.INSTANCE.convert(record));
    }

    @GetMapping("/list")
    @Operation(summary = "获得用户积分记录列表")
    @Parameter(name = "ids", description = "编号列表", required = true, example = "1024,2048")
    @PreAuthorize("@ss.hasPermission('point:record:query')")
    public CommonResult<List<MemberPointRecordRespVO>> getRecordList(@RequestParam("ids") Collection<Long> ids) {
        List<MemberPointRecordDO> list = recordService.getRecordList(ids);
        return success(MemberPointRecordConvert.INSTANCE.convertList(list));
    }

    @GetMapping("/page")
    @Operation(summary = "获得用户积分记录分页")
    @PreAuthorize("@ss.hasPermission('point:record:query')")
    public CommonResult<PageResult<MemberPointRecordRespVO>> getRecordPage(@Valid MemberPointRecordPageReqVO pageVO) {
        PageResult<MemberPointRecordDO> pageResult = recordService.getRecordPage(pageVO);
        return success(MemberPointRecordConvert.INSTANCE.convertPage(pageResult));
    }

    @GetMapping("/export-excel")
    @Operation(summary = "导出用户积分记录 Excel")
    @PreAuthorize("@ss.hasPermission('point:record:export')")
    @OperateLog(type = EXPORT)
    public void exportRecordExcel(@Valid MemberPointRecordExportReqVO exportReqVO,
              HttpServletResponse response) throws IOException {
        List<MemberPointRecordDO> list = recordService.getRecordList(exportReqVO);
        // 导出 Excel
        List<MemberPointRecordExcelVO> datas = MemberPointRecordConvert.INSTANCE.convertList02(list);
        ExcelUtils.write(response, "用户积分记录.xls", "数据", MemberPointRecordExcelVO.class, datas);
    }

}
