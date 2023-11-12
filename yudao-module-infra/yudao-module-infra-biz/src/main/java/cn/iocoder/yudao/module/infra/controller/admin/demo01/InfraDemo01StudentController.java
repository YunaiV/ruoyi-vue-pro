package cn.iocoder.yudao.module.infra.controller.admin.demo01;

import cn.iocoder.yudao.framework.common.pojo.CommonResult;
import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.excel.core.util.ExcelUtils;
import cn.iocoder.yudao.framework.operatelog.core.annotations.OperateLog;
import cn.iocoder.yudao.module.infra.controller.admin.demo01.vo.*;
import cn.iocoder.yudao.module.infra.convert.demo01.InfraDemo01StudentConvert;
import cn.iocoder.yudao.module.infra.dal.dataobject.demo01.InfraDemo01StudentDO;
import cn.iocoder.yudao.module.infra.service.demo01.InfraDemo01StudentService;
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
import java.util.List;

import static cn.iocoder.yudao.framework.common.pojo.CommonResult.success;
import static cn.iocoder.yudao.framework.operatelog.core.enums.OperateTypeEnum.EXPORT;

@Tag(name = "管理后台 - 学生")
@RestController
@RequestMapping("/infra/demo01-student")
@Validated
public class InfraDemo01StudentController {

    @Resource
    private InfraDemo01StudentService demo01StudentService;

    @PostMapping("/create")
    @Operation(summary = "创建学生")
    @PreAuthorize("@ss.hasPermission('infra:demo01-student:create')")
    public CommonResult<Long> createDemo01Student(@Valid @RequestBody InfraDemo01StudentCreateReqVO createReqVO) {
        return success(demo01StudentService.createDemo01Student(createReqVO));
    }

    @PutMapping("/update")
    @Operation(summary = "更新学生")
    @PreAuthorize("@ss.hasPermission('infra:demo01-student:update')")
    public CommonResult<Boolean> updateDemo01Student(@Valid @RequestBody InfraDemo01StudentUpdateReqVO updateReqVO) {
        demo01StudentService.updateDemo01Student(updateReqVO);
        return success(true);
    }

    @DeleteMapping("/delete")
    @Operation(summary = "删除学生")
    @Parameter(name = "id", description = "编号", required = true)
    @PreAuthorize("@ss.hasPermission('infra:demo01-student:delete')")
    public CommonResult<Boolean> deleteDemo01Student(@RequestParam("id") Long id) {
        demo01StudentService.deleteDemo01Student(id);
        return success(true);
    }

    @GetMapping("/get")
    @Operation(summary = "获得学生")
    @Parameter(name = "id", description = "编号", required = true, example = "1024")
    @PreAuthorize("@ss.hasPermission('infra:demo01-student:query')")
    public CommonResult<InfraDemo01StudentRespVO> getDemo01Student(@RequestParam("id") Long id) {
        InfraDemo01StudentDO demo01Student = demo01StudentService.getDemo01Student(id);
        return success(InfraDemo01StudentConvert.INSTANCE.convert(demo01Student));
    }

    @GetMapping("/page")
    @Operation(summary = "获得学生分页")
    @PreAuthorize("@ss.hasPermission('infra:demo01-student:query')")
    public CommonResult<PageResult<InfraDemo01StudentRespVO>> getDemo01StudentPage(@Valid InfraDemo01StudentPageReqVO pageVO) {
        PageResult<InfraDemo01StudentDO> pageResult = demo01StudentService.getDemo01StudentPage(pageVO);
        return success(InfraDemo01StudentConvert.INSTANCE.convertPage(pageResult));
    }

    @GetMapping("/export-excel")
    @Operation(summary = "导出学生 Excel")
    @PreAuthorize("@ss.hasPermission('infra:demo01-student:export')")
    @OperateLog(type = EXPORT)
    public void exportDemo01StudentExcel(@Valid InfraDemo01StudentExportReqVO exportReqVO,
              HttpServletResponse response) throws IOException {
        List<InfraDemo01StudentDO> list = demo01StudentService.getDemo01StudentList(exportReqVO);
        // 导出 Excel
        List<InfraDemo01StudentExcelVO> datas = InfraDemo01StudentConvert.INSTANCE.convertList02(list);
        ExcelUtils.write(response, "学生.xls", "数据", InfraDemo01StudentExcelVO.class, datas);
    }

}