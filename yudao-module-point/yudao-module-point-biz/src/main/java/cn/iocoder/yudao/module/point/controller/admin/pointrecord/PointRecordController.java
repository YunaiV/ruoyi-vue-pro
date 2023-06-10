package cn.iocoder.yudao.module.point.controller.admin.pointrecord;

import org.springframework.web.bind.annotation.*;
import javax.annotation.Resource;
import org.springframework.validation.annotation.Validated;
import org.springframework.security.access.prepost.PreAuthorize;
import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.Operation;

import javax.validation.constraints.*;
import javax.validation.*;
import javax.servlet.http.*;
import java.util.*;
import java.io.IOException;

import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.common.pojo.CommonResult;
import static cn.iocoder.yudao.framework.common.pojo.CommonResult.success;

import cn.iocoder.yudao.framework.excel.core.util.ExcelUtils;

import cn.iocoder.yudao.framework.operatelog.core.annotations.OperateLog;
import static cn.iocoder.yudao.framework.operatelog.core.enums.OperateTypeEnum.*;

import cn.iocoder.yudao.module.point.controller.admin.pointrecord.vo.*;
import cn.iocoder.yudao.module.point.dal.dataobject.pointrecord.PointRecordDO;
import cn.iocoder.yudao.module.point.convert.pointrecord.PointRecordConvert;
import cn.iocoder.yudao.module.point.service.pointrecord.PointRecordService;

@Tag(name = "管理后台 - 用户积分记录")
@RestController
@RequestMapping("/point/record")
@Validated
public class PointRecordController {

    @Resource
    private PointRecordService recordService;

    @PostMapping("/create")
    @Operation(summary = "创建用户积分记录")
    @PreAuthorize("@ss.hasPermission('point:record:create')")
    public CommonResult<Long> createRecord(@Valid @RequestBody PointRecordCreateReqVO createReqVO) {
        return success(recordService.createRecord(createReqVO));
    }

    @PutMapping("/update")
    @Operation(summary = "更新用户积分记录")
    @PreAuthorize("@ss.hasPermission('point:record:update')")
    public CommonResult<Boolean> updateRecord(@Valid @RequestBody PointRecordUpdateReqVO updateReqVO) {
        recordService.updateRecord(updateReqVO);
        return success(true);
    }

    @DeleteMapping("/delete")
    @Operation(summary = "删除用户积分记录")
    @Parameter(name = "id", description = "编号", required = true)
    @PreAuthorize("@ss.hasPermission('point:record:delete')")
    public CommonResult<Boolean> deleteRecord(@RequestParam("id") Long id) {
        recordService.deleteRecord(id);
        return success(true);
    }

    @GetMapping("/get")
    @Operation(summary = "获得用户积分记录")
    @Parameter(name = "id", description = "编号", required = true, example = "1024")
    @PreAuthorize("@ss.hasPermission('point:record:query')")
    public CommonResult<PointRecordRespVO> getRecord(@RequestParam("id") Long id) {
        PointRecordDO record = recordService.getRecord(id);
        return success(PointRecordConvert.INSTANCE.convert(record));
    }

    @GetMapping("/list")
    @Operation(summary = "获得用户积分记录列表")
    @Parameter(name = "ids", description = "编号列表", required = true, example = "1024,2048")
    @PreAuthorize("@ss.hasPermission('point:record:query')")
    public CommonResult<List<PointRecordRespVO>> getRecordList(@RequestParam("ids") Collection<Long> ids) {
        List<PointRecordDO> list = recordService.getRecordList(ids);
        return success(PointRecordConvert.INSTANCE.convertList(list));
    }

    @GetMapping("/page")
    @Operation(summary = "获得用户积分记录分页")
    @PreAuthorize("@ss.hasPermission('point:record:query')")
    public CommonResult<PageResult<PointRecordRespVO>> getRecordPage(@Valid PointRecordPageReqVO pageVO) {
        PageResult<PointRecordDO> pageResult = recordService.getRecordPage(pageVO);
        return success(PointRecordConvert.INSTANCE.convertPage(pageResult));
    }

    @GetMapping("/export-excel")
    @Operation(summary = "导出用户积分记录 Excel")
    @PreAuthorize("@ss.hasPermission('point:record:export')")
    @OperateLog(type = EXPORT)
    public void exportRecordExcel(@Valid PointRecordExportReqVO exportReqVO,
              HttpServletResponse response) throws IOException {
        List<PointRecordDO> list = recordService.getRecordList(exportReqVO);
        // 导出 Excel
        List<PointRecordExcelVO> datas = PointRecordConvert.INSTANCE.convertList02(list);
        ExcelUtils.write(response, "用户积分记录.xls", "数据", PointRecordExcelVO.class, datas);
    }

}
