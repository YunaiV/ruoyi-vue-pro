package cn.iocoder.yudao.module.system.controller.admin.student;

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

import cn.iocoder.yudao.framework.common.pojo.PageParam;
import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.common.pojo.CommonResult;
import cn.iocoder.yudao.framework.common.util.object.BeanUtils;
import static cn.iocoder.yudao.framework.common.pojo.CommonResult.success;

import cn.iocoder.yudao.framework.excel.core.util.ExcelUtils;

import cn.iocoder.yudao.framework.apilog.core.annotation.ApiAccessLog;
import static cn.iocoder.yudao.framework.apilog.core.enums.OperateTypeEnum.*;

import cn.iocoder.yudao.module.system.controller.admin.student.vo.*;
import cn.iocoder.yudao.module.system.dal.dataobject.student.StudentDO;
import cn.iocoder.yudao.module.system.dal.dataobject.student.StudentCourseDO;
import cn.iocoder.yudao.module.system.dal.dataobject.student.StudentGradeDO;
import cn.iocoder.yudao.module.system.service.student.StudentService;

@Tag(name = "管理后台 - 学生主子表测试")
@RestController
@RequestMapping("/system/student")
@Validated
public class StudentController {

    @Resource
    private StudentService studentService;

    @PostMapping("/create")
    @Operation(summary = "创建学生主子表测试")
    @PreAuthorize("@ss.hasPermission('system:student:create')")
    public CommonResult<Long> createStudent(@Valid @RequestBody StudentSaveReqVO createReqVO) {
        return success(studentService.createStudent(createReqVO));
    }

    @PutMapping("/update")
    @Operation(summary = "更新学生主子表测试")
    @PreAuthorize("@ss.hasPermission('system:student:update')")
    public CommonResult<Boolean> updateStudent(@Valid @RequestBody StudentSaveReqVO updateReqVO) {
        studentService.updateStudent(updateReqVO);
        return success(true);
    }

    @DeleteMapping("/delete")
    @Operation(summary = "删除学生主子表测试")
    @Parameter(name = "id", description = "编号", required = true)
    @PreAuthorize("@ss.hasPermission('system:student:delete')")
    public CommonResult<Boolean> deleteStudent(@RequestParam("id") Long id) {
        studentService.deleteStudent(id);
        return success(true);
    }

    @DeleteMapping("/delete-list")
    @Parameter(name = "ids", description = "编号", required = true)
    @Operation(summary = "批量删除学生主子表测试")
                @PreAuthorize("@ss.hasPermission('system:student:delete')")
    public CommonResult<Boolean> deleteStudentList(@RequestParam("ids") List<Long> ids) {
        studentService.deleteStudentListByIds(ids);
        return success(true);
    }

    @GetMapping("/get")
    @Operation(summary = "获得学生主子表测试")
    @Parameter(name = "id", description = "编号", required = true, example = "1024")
    @PreAuthorize("@ss.hasPermission('system:student:query')")
    public CommonResult<StudentRespVO> getStudent(@RequestParam("id") Long id) {
        StudentDO student = studentService.getStudent(id);
        return success(BeanUtils.toBean(student, StudentRespVO.class));
    }

    @GetMapping("/page")
    @Operation(summary = "获得学生主子表测试分页")
    @PreAuthorize("@ss.hasPermission('system:student:query')")
    public CommonResult<PageResult<StudentRespVO>> getStudentPage(@Valid StudentPageReqVO pageReqVO) {
        PageResult<StudentDO> pageResult = studentService.getStudentPage(pageReqVO);
        return success(BeanUtils.toBean(pageResult, StudentRespVO.class));
    }

    @GetMapping("/export-excel")
    @Operation(summary = "导出学生主子表测试 Excel")
    @PreAuthorize("@ss.hasPermission('system:student:export')")
    @ApiAccessLog(operateType = EXPORT)
    public void exportStudentExcel(@Valid StudentPageReqVO pageReqVO,
              HttpServletResponse response) throws IOException {
        pageReqVO.setPageSize(PageParam.PAGE_SIZE_NONE);
        List<StudentDO> list = studentService.getStudentPage(pageReqVO).getList();
        // 导出 Excel
        ExcelUtils.write(response, "学生主子表测试.xls", "数据", StudentRespVO.class,
                        BeanUtils.toBean(list, StudentRespVO.class));
    }

    // ==================== 子表（学生课程） ====================

    @GetMapping("/student-course/page")
    @Operation(summary = "获得学生课程分页")
    @Parameter(name = "studentId", description = "学生编号")
    @PreAuthorize("@ss.hasPermission('system:student:query')")
    public CommonResult<PageResult<StudentCourseDO>> getStudentCoursePage(PageParam pageReqVO,
                                                                                        @RequestParam("studentId") Long studentId) {
        return success(studentService.getStudentCoursePage(pageReqVO, studentId));
    }

    @PostMapping("/student-course/create")
    @Operation(summary = "创建学生课程")
    @PreAuthorize("@ss.hasPermission('system:student:create')")
    public CommonResult<Long> createStudentCourse(@Valid @RequestBody StudentCourseDO studentCourse) {
        return success(studentService.createStudentCourse(studentCourse));
    }

    @PutMapping("/student-course/update")
    @Operation(summary = "更新学生课程")
    @PreAuthorize("@ss.hasPermission('system:student:update')")
    public CommonResult<Boolean> updateStudentCourse(@Valid @RequestBody StudentCourseDO studentCourse) {
        studentService.updateStudentCourse(studentCourse);
        return success(true);
    }

    @DeleteMapping("/student-course/delete")
    @Parameter(name = "id", description = "编号", required = true)
    @Operation(summary = "删除学生课程")
    @PreAuthorize("@ss.hasPermission('system:student:delete')")
    public CommonResult<Boolean> deleteStudentCourse(@RequestParam("id") Long id) {
        studentService.deleteStudentCourse(id);
        return success(true);
    }

    @DeleteMapping("/student-course/delete-list")
    @Parameter(name = "ids", description = "编号", required = true)
    @Operation(summary = "批量删除学生课程")
    @PreAuthorize("@ss.hasPermission('system:student:delete')")
    public CommonResult<Boolean> deleteStudentCourseList(@RequestParam("ids") List<Long> ids) {
        studentService.deleteStudentCourseListByIds(ids);
        return success(true);
    }

	@GetMapping("/student-course/get")
	@Operation(summary = "获得学生课程")
	@Parameter(name = "id", description = "编号", required = true)
    @PreAuthorize("@ss.hasPermission('system:student:query')")
	public CommonResult<StudentCourseDO> getStudentCourse(@RequestParam("id") Long id) {
	    return success(studentService.getStudentCourse(id));
	}

    // ==================== 子表（学生班级） ====================

    @GetMapping("/student-grade/page")
    @Operation(summary = "获得学生班级分页")
    @Parameter(name = "studentId", description = "学生编号")
    @PreAuthorize("@ss.hasPermission('system:student:query')")
    public CommonResult<PageResult<StudentGradeDO>> getStudentGradePage(PageParam pageReqVO,
                                                                                        @RequestParam("studentId") Long studentId) {
        return success(studentService.getStudentGradePage(pageReqVO, studentId));
    }

    @PostMapping("/student-grade/create")
    @Operation(summary = "创建学生班级")
    @PreAuthorize("@ss.hasPermission('system:student:create')")
    public CommonResult<Long> createStudentGrade(@Valid @RequestBody StudentGradeDO studentGrade) {
        return success(studentService.createStudentGrade(studentGrade));
    }

    @PutMapping("/student-grade/update")
    @Operation(summary = "更新学生班级")
    @PreAuthorize("@ss.hasPermission('system:student:update')")
    public CommonResult<Boolean> updateStudentGrade(@Valid @RequestBody StudentGradeDO studentGrade) {
        studentService.updateStudentGrade(studentGrade);
        return success(true);
    }

    @DeleteMapping("/student-grade/delete")
    @Parameter(name = "id", description = "编号", required = true)
    @Operation(summary = "删除学生班级")
    @PreAuthorize("@ss.hasPermission('system:student:delete')")
    public CommonResult<Boolean> deleteStudentGrade(@RequestParam("id") Long id) {
        studentService.deleteStudentGrade(id);
        return success(true);
    }

    @DeleteMapping("/student-grade/delete-list")
    @Parameter(name = "ids", description = "编号", required = true)
    @Operation(summary = "批量删除学生班级")
    @PreAuthorize("@ss.hasPermission('system:student:delete')")
    public CommonResult<Boolean> deleteStudentGradeList(@RequestParam("ids") List<Long> ids) {
        studentService.deleteStudentGradeListByIds(ids);
        return success(true);
    }

	@GetMapping("/student-grade/get")
	@Operation(summary = "获得学生班级")
	@Parameter(name = "id", description = "编号", required = true)
    @PreAuthorize("@ss.hasPermission('system:student:query')")
	public CommonResult<StudentGradeDO> getStudentGrade(@RequestParam("id") Long id) {
	    return success(studentService.getStudentGrade(id));
	}

}