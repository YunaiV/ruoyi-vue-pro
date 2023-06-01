package cn.iocoder.yudao.module.jl.controller.admin.laboratory;

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
import static cn.iocoder.yudao.framework.common.exception.util.ServiceExceptionUtil.exception;
import static cn.iocoder.yudao.module.jl.enums.ErrorCodeConstants.*;
import static cn.iocoder.yudao.framework.common.pojo.CommonResult.success;

import cn.iocoder.yudao.framework.excel.core.util.ExcelUtils;

import cn.iocoder.yudao.framework.operatelog.core.annotations.OperateLog;
import static cn.iocoder.yudao.framework.operatelog.core.enums.OperateTypeEnum.*;

import cn.iocoder.yudao.module.jl.controller.admin.laboratory.vo.*;
import cn.iocoder.yudao.module.jl.entity.laboratory.CategorySkillUser;
import cn.iocoder.yudao.module.jl.mapper.laboratory.CategorySkillUserMapper;
import cn.iocoder.yudao.module.jl.service.laboratory.CategorySkillUserService;

@Tag(name = "管理后台 - 实验名目的擅长人员")
@RestController
@RequestMapping("/jl/category-skill-user")
@Validated
public class CategorySkillUserController {

    @Resource
    private CategorySkillUserService categorySkillUserService;

    @Resource
    private CategorySkillUserMapper categorySkillUserMapper;

    @PostMapping("/create")
    @Operation(summary = "创建实验名目的擅长人员")
    @PreAuthorize("@ss.hasPermission('jl:category-skill-user:create')")
    public CommonResult<Long> createCategorySkillUser(@Valid @RequestBody CategorySkillUserCreateReqVO createReqVO) {
        return success(categorySkillUserService.createCategorySkillUser(createReqVO));
    }

    @PutMapping("/update")
    @Operation(summary = "更新实验名目的擅长人员")
    @PreAuthorize("@ss.hasPermission('jl:category-skill-user:update')")
    public CommonResult<Boolean> updateCategorySkillUser(@Valid @RequestBody CategorySkillUserUpdateReqVO updateReqVO) {
        categorySkillUserService.updateCategorySkillUser(updateReqVO);
        return success(true);
    }

    @DeleteMapping("/delete")
    @Operation(summary = "通过 ID 删除实验名目的擅长人员")
    @Parameter(name = "id", description = "编号", required = true)
    @PreAuthorize("@ss.hasPermission('jl:category-skill-user:delete')")
    public CommonResult<Boolean> deleteCategorySkillUser(@RequestParam("id") Long id) {
        categorySkillUserService.deleteCategorySkillUser(id);
        return success(true);
    }

    @GetMapping("/get")
    @Operation(summary = "通过 ID 获得实验名目的擅长人员")
    @Parameter(name = "id", description = "编号", required = true, example = "1024")
    @PreAuthorize("@ss.hasPermission('jl:category-skill-user:query')")
    public CommonResult<CategorySkillUserRespVO> getCategorySkillUser(@RequestParam("id") Long id) {
            Optional<CategorySkillUser> categorySkillUser = categorySkillUserService.getCategorySkillUser(id);
        return success(categorySkillUser.map(categorySkillUserMapper::toDto).orElseThrow(() -> exception(CATEGORY_SKILL_USER_NOT_EXISTS)));
    }

    @GetMapping("/page")
    @Operation(summary = "(分页)获得实验名目的擅长人员列表")
    @PreAuthorize("@ss.hasPermission('jl:category-skill-user:query')")
    public CommonResult<PageResult<CategorySkillUserRespVO>> getCategorySkillUserPage(@Valid CategorySkillUserPageReqVO pageVO, @Valid CategorySkillUserPageOrder orderV0) {
        PageResult<CategorySkillUser> pageResult = categorySkillUserService.getCategorySkillUserPage(pageVO, orderV0);
        return success(categorySkillUserMapper.toPage(pageResult));
    }

    @GetMapping("/export-excel")
    @Operation(summary = "导出实验名目的擅长人员 Excel")
    @PreAuthorize("@ss.hasPermission('jl:category-skill-user:export')")
    @OperateLog(type = EXPORT)
    public void exportCategorySkillUserExcel(@Valid CategorySkillUserExportReqVO exportReqVO, HttpServletResponse response) throws IOException {
        List<CategorySkillUser> list = categorySkillUserService.getCategorySkillUserList(exportReqVO);
        // 导出 Excel
        List<CategorySkillUserExcelVO> excelData = categorySkillUserMapper.toExcelList(list);
        ExcelUtils.write(response, "实验名目的擅长人员.xls", "数据", CategorySkillUserExcelVO.class, excelData);
    }

}
