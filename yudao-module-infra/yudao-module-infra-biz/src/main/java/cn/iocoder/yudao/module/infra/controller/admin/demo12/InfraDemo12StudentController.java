package cn.iocoder.yudao.module.infra.controller.admin.demo12;

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

import cn.iocoder.yudao.module.infra.controller.admin.demo12.vo.*;
import cn.iocoder.yudao.module.infra.dal.dataobject.demo12.InfraDemo12StudentDO;
import cn.iocoder.yudao.module.infra.dal.dataobject.demo12.InfraDemo12StudentContactDO;
import cn.iocoder.yudao.module.infra.dal.dataobject.demo12.InfraDemo12StudentTeacherDO;
import cn.iocoder.yudao.module.infra.convert.demo12.InfraDemo12StudentConvert;
import cn.iocoder.yudao.module.infra.service.demo12.InfraDemo12StudentService;

@Tag(name = "管理后台 - 学生")
@RestController
@RequestMapping("/infra/demo12-student")
@Validated
public class InfraDemo12StudentController {

    @Resource
    private InfraDemo12StudentService demo12StudentService;

    @PostMapping("/create")
    @Operation(summary = "创建学生")
    @PreAuthorize("@ss.hasPermission('infra:demo12-student:create')")
    public CommonResult<Long> createDemo12Student(@Valid @RequestBody InfraDemo12StudentCreateReqVO createReqVO) {
        return success(demo12StudentService.createDemo12Student(createReqVO));
    }

    @PutMapping("/update")
    @Operation(summary = "更新学生")
    @PreAuthorize("@ss.hasPermission('infra:demo12-student:update')")
    public CommonResult<Boolean> updateDemo12Student(@Valid @RequestBody InfraDemo12StudentUpdateReqVO updateReqVO) {
        demo12StudentService.updateDemo12Student(updateReqVO);
        return success(true);
    }

    @DeleteMapping("/delete")
    @Operation(summary = "删除学生")
    @Parameter(name = "id", description = "编号", required = true)
    @PreAuthorize("@ss.hasPermission('infra:demo12-student:delete')")
    public CommonResult<Boolean> deleteDemo12Student(@RequestParam("id") Long id) {
        demo12StudentService.deleteDemo12Student(id);
        return success(true);
    }

    @GetMapping("/get")
    @Operation(summary = "获得学生")
    @Parameter(name = "id", description = "编号", required = true, example = "1024")
    @PreAuthorize("@ss.hasPermission('infra:demo12-student:query')")
    public CommonResult<InfraDemo12StudentRespVO> getDemo12Student(@RequestParam("id") Long id) {
        InfraDemo12StudentDO demo12Student = demo12StudentService.getDemo12Student(id);
        return success(InfraDemo12StudentConvert.INSTANCE.convert(demo12Student));
    }

    @GetMapping("/page")
    @Operation(summary = "获得学生分页")
    @PreAuthorize("@ss.hasPermission('infra:demo12-student:query')")
    public CommonResult<PageResult<InfraDemo12StudentRespVO>> getDemo12StudentPage(@Valid InfraDemo12StudentPageReqVO pageVO) {
        PageResult<InfraDemo12StudentDO> pageResult = demo12StudentService.getDemo12StudentPage(pageVO);
        return success(InfraDemo12StudentConvert.INSTANCE.convertPage(pageResult));
    }

    @GetMapping("/export-excel")
    @Operation(summary = "导出学生 Excel")
    @PreAuthorize("@ss.hasPermission('infra:demo12-student:export')")
    @OperateLog(type = EXPORT)
    public void exportDemo12StudentExcel(@Valid InfraDemo12StudentExportReqVO exportReqVO,
              HttpServletResponse response) throws IOException {
        List<InfraDemo12StudentDO> list = demo12StudentService.getDemo12StudentList(exportReqVO);
        // 导出 Excel
        List<InfraDemo12StudentExcelVO> datas = InfraDemo12StudentConvert.INSTANCE.convertList02(list);
        ExcelUtils.write(response, "学生.xls", "数据", InfraDemo12StudentExcelVO.class, datas);
    }

    // ==================== 子表（学生联系人） ====================

    @GetMapping("/demo12-student/list-by-student-id")
    @Operation(summary = "获得学生联系人列表")
    @Parameter(name = "studentId", description = "学生编号")
    @PreAuthorize("@ss.hasPermission('infra:demo12-student:query')")
    public CommonResult<List<InfraDemo12StudentContactDO>> getDemo12StudentContactListByStudentId(@RequestParam("studentId") Long studentId) {
        return success(demo12StudentService.getDemo12StudentContactListByStudentId(studentId));
    }

    // ==================== 子表（学生班主任） ====================

    @GetMapping("/demo12-student/get-by-student-id")
    @Operation(summary = "获得学生班主任")
    @Parameter(name = "studentId", description = "学生编号")
    @PreAuthorize("@ss.hasPermission('infra:demo12-student:query')")
    public CommonResult<InfraDemo12StudentTeacherDO> getDemo12StudentTeacherByStudentId(@RequestParam("studentId") Long studentId) {
        return success(demo12StudentService.getDemo12StudentTeacherByStudentId(studentId));
    }

}