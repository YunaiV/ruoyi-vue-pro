package cn.iocoder.yudao.module.weapp.controller.app.appslist;

import cn.iocoder.yudao.framework.common.pojo.CommonResult;
import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.common.util.object.BeanUtils;
import cn.iocoder.yudao.framework.security.core.annotations.PreAuthenticated;
import cn.iocoder.yudao.module.weapp.controller.admin.appslist.vo.AppsListPageReqVO;
import cn.iocoder.yudao.module.weapp.controller.admin.appslist.vo.AppsListRespVO;
import cn.iocoder.yudao.module.weapp.controller.app.appslist.vo.AppAppsListRespVO;
import cn.iocoder.yudao.module.weapp.dal.dataobject.appslist.AppsListDO;
import cn.iocoder.yudao.module.weapp.service.appslist.AppsListService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import javax.validation.Valid;

import java.util.List;

import static cn.iocoder.yudao.framework.common.pojo.CommonResult.success;

@Tag(name = "小程序 - 小功能导航站")
@RestController
@RequestMapping("/weapp/apps-list")
@Validated
public class AppAppsListController {

    @Resource
    private AppsListService appsListService;



    @PostMapping("/banner/get")
    @Operation(summary = "获取banner小程序清单")
    @PreAuthenticated
    public CommonResult<List<AppAppsListRespVO>> getBannerAppsList(){
        List<AppsListDO> appsList = appsListService.getBannerAppList();
        return success(BeanUtils.toBean(appsList, AppAppsListRespVO.class));
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

}