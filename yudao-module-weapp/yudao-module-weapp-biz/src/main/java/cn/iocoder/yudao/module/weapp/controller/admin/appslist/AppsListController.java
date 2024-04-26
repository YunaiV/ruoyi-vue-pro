package cn.iocoder.yudao.module.weapp.controller.admin.appslist;

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

import cn.iocoder.yudao.module.weapp.controller.admin.appslist.vo.*;
import cn.iocoder.yudao.module.weapp.dal.dataobject.appslist.AppsListDO;
import cn.iocoder.yudao.module.weapp.service.appslist.AppsListService;

@Tag(name = "管理后台 - 小程序清单")
@RestController
@RequestMapping("/weapp/apps-list")
@Validated
public class AppsListController {

    @Resource
    private AppsListService appsListService;

    @PostMapping("/create")
    @Operation(summary = "创建小程序清单")
    @PreAuthorize("@ss.hasPermission('weapp:apps-list:create')")
    public CommonResult<Long> createAppsList(@Valid @RequestBody AppsListSaveReqVO createReqVO) {
        return success(appsListService.createAppsList(createReqVO));
    }

    @PutMapping("/update")
    @Operation(summary = "更新小程序清单")
    @PreAuthorize("@ss.hasPermission('weapp:apps-list:update')")
    public CommonResult<Boolean> updateAppsList(@Valid @RequestBody AppsListSaveReqVO updateReqVO) {
        appsListService.updateAppsList(updateReqVO);
        return success(true);
    }

    @DeleteMapping("/delete")
    @Operation(summary = "删除小程序清单")
    @Parameter(name = "id", description = "编号", required = true)
    @PreAuthorize("@ss.hasPermission('weapp:apps-list:delete')")
    public CommonResult<Boolean> deleteAppsList(@RequestParam("id") Long id) {
        appsListService.deleteAppsList(id);
        return success(true);
    }

    @GetMapping("/get")
    @Operation(summary = "获得小程序清单")
    @Parameter(name = "id", description = "编号", required = true, example = "1024")
    @PreAuthorize("@ss.hasPermission('weapp:apps-list:query')")
    public CommonResult<AppsListRespVO> getAppsList(@RequestParam("id") Long id) {
        AppsListDO appsList = appsListService.getAppsList(id);
        return success(BeanUtils.toBean(appsList, AppsListRespVO.class));
    }

    @GetMapping("/page")
    @Operation(summary = "获得小程序清单分页")
    @PreAuthorize("@ss.hasPermission('weapp:apps-list:query')")
    public CommonResult<PageResult<AppsListRespVO>> getAppsListPage(@Valid AppsListPageReqVO pageReqVO) {
        PageResult<AppsListDO> pageResult = appsListService.getAppsListPage(pageReqVO);
        return success(BeanUtils.toBean(pageResult, AppsListRespVO.class));
    }

    @GetMapping("/export-excel")
    @Operation(summary = "导出小程序清单 Excel")
    @PreAuthorize("@ss.hasPermission('weapp:apps-list:export')")
    @OperateLog(type = EXPORT)
    public void exportAppsListExcel(@Valid AppsListPageReqVO pageReqVO,
              HttpServletResponse response) throws IOException {
        pageReqVO.setPageSize(PageParam.PAGE_SIZE_NONE);
        List<AppsListDO> list = appsListService.getAppsListPage(pageReqVO).getList();
        // 导出 Excel
        ExcelUtils.write(response, "小程序清单.xls", "数据", AppsListRespVO.class,
                        BeanUtils.toBean(list, AppsListRespVO.class));
    }

}