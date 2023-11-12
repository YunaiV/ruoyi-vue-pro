package cn.iocoder.yudao.module.infra.controller.admin.demo11;

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

import cn.iocoder.yudao.module.infra.controller.admin.demo11.vo.*;
import cn.iocoder.yudao.module.infra.dal.dataobject.demo11.InfraDemo11StudentDO;
import cn.iocoder.yudao.module.infra.dal.dataobject.demo11.InfraDemo11StudentContactDO;
import cn.iocoder.yudao.module.infra.dal.dataobject.demo11.InfraDemo11StudentTeacherDO;
import cn.iocoder.yudao.module.infra.convert.demo11.InfraDemo11StudentConvert;
import cn.iocoder.yudao.module.infra.service.demo11.InfraDemo11StudentService;

@Tag(name = "管理后台 - 学生")
@RestController
@RequestMapping("/infra/demo11-student")
@Validated
public class InfraDemo11StudentController {

    @Resource
    private InfraDemo11StudentService demo11StudentService;

    @PostMapping("/create")
    @Operation(summary = "创建学生")
    @PreAuthorize("@ss.hasPermission('infra:demo11-student:create')")
    public CommonResult<Long> createDemo11Student(@Valid @RequestBody InfraDemo11StudentCreateReqVO createReqVO) {
        return success(demo11StudentService.createDemo11Student(createReqVO));
    }

    @PutMapping("/update")
    @Operation(summary = "更新学生")
    @PreAuthorize("@ss.hasPermission('infra:demo11-student:update')")
    public CommonResult<Boolean> updateDemo11Student(@Valid @RequestBody InfraDemo11StudentUpdateReqVO updateReqVO) {
        demo11StudentService.updateDemo11Student(updateReqVO);
        return success(true);
    }

    @DeleteMapping("/delete")
    @Operation(summary = "删除学生")
    @Parameter(name = "id", description = "编号", required = true)
    @PreAuthorize("@ss.hasPermission('infra:demo11-student:delete')")
    public CommonResult<Boolean> deleteDemo11Student(@RequestParam("id") Long id) {
        demo11StudentService.deleteDemo11Student(id);
        return success(true);
    }

    @GetMapping("/get")
    @Operation(summary = "获得学生")
    @Parameter(name = "id", description = "编号", required = true, example = "1024")
    @PreAuthorize("@ss.hasPermission('infra:demo11-student:query')")
    public CommonResult<InfraDemo11StudentRespVO> getDemo11Student(@RequestParam("id") Long id) {
        InfraDemo11StudentDO demo11Student = demo11StudentService.getDemo11Student(id);
        return success(InfraDemo11StudentConvert.INSTANCE.convert(demo11Student));
    }

    @GetMapping("/page")
    @Operation(summary = "获得学生分页")
    @PreAuthorize("@ss.hasPermission('infra:demo11-student:query')")
    public CommonResult<PageResult<InfraDemo11StudentRespVO>> getDemo11StudentPage(@Valid InfraDemo11StudentPageReqVO pageVO) {
        PageResult<InfraDemo11StudentDO> pageResult = demo11StudentService.getDemo11StudentPage(pageVO);
        return success(InfraDemo11StudentConvert.INSTANCE.convertPage(pageResult));
    }

    @GetMapping("/export-excel")
    @Operation(summary = "导出学生 Excel")
    @PreAuthorize("@ss.hasPermission('infra:demo11-student:export')")
    @OperateLog(type = EXPORT)
    public void exportDemo11StudentExcel(@Valid InfraDemo11StudentExportReqVO exportReqVO,
              HttpServletResponse response) throws IOException {
        List<InfraDemo11StudentDO> list = demo11StudentService.getDemo11StudentList(exportReqVO);
        // 导出 Excel
        List<InfraDemo11StudentExcelVO> datas = InfraDemo11StudentConvert.INSTANCE.convertList02(list);
        ExcelUtils.write(response, "学生.xls", "数据", InfraDemo11StudentExcelVO.class, datas);
    }

    // ==================== 子表（学生联系人） ====================

    @GetMapping("/demo11-student/list-by-student-id")
    @Operation(summary = "获得学生联系人列表")
    @Parameter(name = "studentId", description = "学生编号")
    @PreAuthorize("@ss.hasPermission('infra:demo11-student:query')")
    public CommonResult<List<InfraDemo11StudentContactDO>> getDemo11StudentContactListByStudentId(@RequestParam("studentId") Long studentId) {
        return success(demo11StudentService.getDemo11StudentContactListByStudentId(studentId));
    }

    // ==================== 子表（学生班主任） ====================

    @GetMapping("/demo11-student/get-by-student-id")
    @Operation(summary = "获得学生班主任")
    @Parameter(name = "studentId", description = "学生编号")
    @PreAuthorize("@ss.hasPermission('infra:demo11-student:query')")
    public CommonResult<InfraDemo11StudentTeacherDO> getDemo11StudentTeacherByStudentId(@RequestParam("studentId") Long studentId) {
        return success(demo11StudentService.getDemo11StudentTeacherByStudentId(studentId));
    }

}