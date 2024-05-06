package cn.iocoder.yudao.module.weapp.controller.admin.appsclass;

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

import cn.iocoder.yudao.framework.operatelog.core.annotations.OperateLog;
import static cn.iocoder.yudao.framework.operatelog.core.enums.OperateTypeEnum.*;

import cn.iocoder.yudao.module.weapp.controller.admin.appsclass.vo.*;
import cn.iocoder.yudao.module.weapp.dal.dataobject.appsclass.AppsClassDO;
import cn.iocoder.yudao.module.weapp.service.appsclass.AppsClassService;

@Tag(name = "管理后台 - 小程序分类")
@RestController
@RequestMapping("/weapp/apps-class")
@Validated
public class AppsClassController {

    @Resource
    private AppsClassService appsClassService;

    @PostMapping("/create")
    @Operation(summary = "创建小程序分类")
    @PreAuthorize("@ss.hasPermission('weapp:apps-class:create')")
    public CommonResult<Integer> createAppsClass(@Valid @RequestBody AppsClassSaveReqVO createReqVO) {
        return success(appsClassService.createAppsClass(createReqVO));
    }

    @PutMapping("/update")
    @Operation(summary = "更新小程序分类")
    @PreAuthorize("@ss.hasPermission('weapp:apps-class:update')")
    public CommonResult<Boolean> updateAppsClass(@Valid @RequestBody AppsClassSaveReqVO updateReqVO) {
        appsClassService.updateAppsClass(updateReqVO);
        return success(true);
    }

    @DeleteMapping("/delete")
    @Operation(summary = "删除小程序分类")
    @Parameter(name = "id", description = "编号", required = true)
    @PreAuthorize("@ss.hasPermission('weapp:apps-class:delete')")
    public CommonResult<Boolean> deleteAppsClass(@RequestParam("id") Integer id) {
        appsClassService.deleteAppsClass(id);
        return success(true);
    }

    @GetMapping("/get")
    @Operation(summary = "获得小程序分类")
    @Parameter(name = "id", description = "编号", required = true, example = "1024")
    @PreAuthorize("@ss.hasPermission('weapp:apps-class:query')")
    public CommonResult<AppsClassRespVO> getAppsClass(@RequestParam("id") Integer id) {
        AppsClassDO appsClass = appsClassService.getAppsClass(id);
        return success(BeanUtils.toBean(appsClass, AppsClassRespVO.class));
    }

    @GetMapping("/page")
    @Operation(summary = "获得小程序分类分页")
    @PreAuthorize("@ss.hasPermission('weapp:apps-class:query')")
    public CommonResult<PageResult<AppsClassRespVO>> getAppsClassPage(@Valid AppsClassPageReqVO pageReqVO) {
        PageResult<AppsClassDO> pageResult = appsClassService.getAppsClassPage(pageReqVO);
        return success(BeanUtils.toBean(pageResult, AppsClassRespVO.class));
    }

    @GetMapping("/export-excel")
    @Operation(summary = "导出小程序分类 Excel")
    @PreAuthorize("@ss.hasPermission('weapp:apps-class:export')")
    @OperateLog(type = EXPORT)
    public void exportAppsClassExcel(@Valid AppsClassPageReqVO pageReqVO,
              HttpServletResponse response) throws IOException {
        pageReqVO.setPageSize(PageParam.PAGE_SIZE_NONE);
        List<AppsClassDO> list = appsClassService.getAppsClassPage(pageReqVO).getList();
        // 导出 Excel
        ExcelUtils.write(response, "小程序分类.xls", "数据", AppsClassRespVO.class,
                        BeanUtils.toBean(list, AppsClassRespVO.class));
    }

}