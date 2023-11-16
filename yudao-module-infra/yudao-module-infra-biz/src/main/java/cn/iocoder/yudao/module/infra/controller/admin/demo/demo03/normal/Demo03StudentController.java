package cn.iocoder.yudao.module.infra.controller.admin.demo.demo03.normal;

import org.springframework.web.bind.annotation.*;
import javax.annotation.Resource;
import org.springframework.validation.annotation.Validated;
import org.springframework.security.access.prepost.PreAuthorize;
import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.Operation;

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

import cn.iocoder.yudao.framework.operatelog.core.annotations.OperateLog;
import static cn.iocoder.yudao.framework.operatelog.core.enums.OperateTypeEnum.*;

import cn.iocoder.yudao.module.infra.controller.admin.demo.demo03.normal.vo.*;
import cn.iocoder.yudao.module.infra.dal.dataobject.demo.demo03.Demo03StudentDO;
import cn.iocoder.yudao.module.infra.dal.dataobject.demo.demo03.Demo03CourseDO;
import cn.iocoder.yudao.module.infra.dal.dataobject.demo.demo03.Demo03GradeDO;
import cn.iocoder.yudao.module.infra.service.demo.demo03.Demo03StudentService;

@Tag(name = "管理后台 - 学生")
@RestController
@RequestMapping("/infra/demo03-student")
@Validated
public class Demo03StudentController {

    @Resource
    private Demo03StudentService demo03StudentService;

    @PostMapping("/create")
    @Operation(summary = "创建学生")
    @PreAuthorize("@ss.hasPermission('infra:demo03-student:create')")
    public CommonResult<Long> createDemo03Student(@Valid @RequestBody Demo03StudentSaveReqVO createReqVO) {
        return success(demo03StudentService.createDemo03Student(createReqVO));
    }

    @PutMapping("/update")
    @Operation(summary = "更新学生")
    @PreAuthorize("@ss.hasPermission('infra:demo03-student:update')")
    public CommonResult<Boolean> updateDemo03Student(@Valid @RequestBody Demo03StudentSaveReqVO updateReqVO) {
        demo03StudentService.updateDemo03Student(updateReqVO);
        return success(true);
    }

    @DeleteMapping("/delete")
    @Operation(summary = "删除学生")
    @Parameter(name = "id", description = "编号", required = true)
    @PreAuthorize("@ss.hasPermission('infra:demo03-student:delete')")
    public CommonResult<Boolean> deleteDemo03Student(@RequestParam("id") Long id) {
        demo03StudentService.deleteDemo03Student(id);
        return success(true);
    }

    @GetMapping("/get")
    @Operation(summary = "获得学生")
    @Parameter(name = "id", description = "编号", required = true, example = "1024")
    @PreAuthorize("@ss.hasPermission('infra:demo03-student:query')")
    public CommonResult<Demo03StudentRespVO> getDemo03Student(@RequestParam("id") Long id) {
        Demo03StudentDO demo03Student = demo03StudentService.getDemo03Student(id);
        return success(BeanUtils.toBean(demo03Student, Demo03StudentRespVO.class));
    }

    @GetMapping("/page")
    @Operation(summary = "获得学生分页")
    @PreAuthorize("@ss.hasPermission('infra:demo03-student:query')")
    public CommonResult<PageResult<Demo03StudentRespVO>> getDemo03StudentPage(@Valid Demo03StudentPageReqVO pageReqVO) {
        PageResult<Demo03StudentDO> pageResult = demo03StudentService.getDemo03StudentPage(pageReqVO);
        return success(BeanUtils.toBean(pageResult, Demo03StudentRespVO.class));
    }

    @GetMapping("/export-excel")
    @Operation(summary = "导出学生 Excel")
    @PreAuthorize("@ss.hasPermission('infra:demo03-student:export')")
    @OperateLog(type = EXPORT)
    public void exportDemo03StudentExcel(@Valid Demo03StudentPageReqVO pageReqVO,
              HttpServletResponse response) throws IOException {
        pageReqVO.setPageSize(PageParam.PAGE_SIZE_NONE);
        List<Demo03StudentDO> list = demo03StudentService.getDemo03StudentPage(pageReqVO).getList();
        // 导出 Excel
        ExcelUtils.write(response, "学生.xls", "数据", Demo03StudentRespVO.class,
                        BeanUtils.toBean(list, Demo03StudentRespVO.class));
    }

    // ==================== 子表（学生课程） ====================

    @GetMapping("/demo03-course/list-by-student-id")
    @Operation(summary = "获得学生课程列表")
    @Parameter(name = "studentId", description = "学生编号")
    @PreAuthorize("@ss.hasPermission('infra:demo03-student:query')")
    public CommonResult<List<Demo03CourseDO>> getDemo03CourseListByStudentId(@RequestParam("studentId") Long studentId) {
        return success(demo03StudentService.getDemo03CourseListByStudentId(studentId));
    }

    // ==================== 子表（学生班级） ====================

    @GetMapping("/demo03-grade/get-by-student-id")
    @Operation(summary = "获得学生班级")
    @Parameter(name = "studentId", description = "学生编号")
    @PreAuthorize("@ss.hasPermission('infra:demo03-student:query')")
    public CommonResult<Demo03GradeDO> getDemo03GradeByStudentId(@RequestParam("studentId") Long studentId) {
        return success(demo03StudentService.getDemo03GradeByStudentId(studentId));
    }

}