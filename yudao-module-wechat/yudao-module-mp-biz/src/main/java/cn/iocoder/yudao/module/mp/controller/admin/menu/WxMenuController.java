package cn.iocoder.yudao.module.mp.controller.admin.menu;

import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;

import org.springframework.validation.annotation.Validated;
import org.springframework.security.access.prepost.PreAuthorize;
import io.swagger.annotations.*;

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

import cn.iocoder.yudao.module.mp.controller.admin.menu.vo.*;
import cn.iocoder.yudao.module.mp.dal.dataobject.menu.WxMenuDO;
import cn.iocoder.yudao.module.mp.convert.menu.WxMenuConvert;
import cn.iocoder.yudao.module.mp.service.menu.WxMenuService;

@Api(tags = "管理后台 - 微信菜单")
@RestController
@RequestMapping("/wechatMp/wx-menu")
@Validated
public class WxMenuController {

    @Resource
    private WxMenuService wxMenuService;

    @PostMapping("/create")
    @ApiOperation("创建微信菜单")
    @PreAuthorize("@ss.hasPermission('wechatMp:wx-menu:create')")
    public CommonResult<Integer> createWxMenu(@Valid @RequestBody WxMenuCreateReqVO createReqVO) {
        return success(wxMenuService.createWxMenu(createReqVO));
    }

    @PutMapping("/update")
    @ApiOperation("更新微信菜单")
    @PreAuthorize("@ss.hasPermission('wechatMp:wx-menu:update')")
    public CommonResult<Boolean> updateWxMenu(@Valid @RequestBody WxMenuUpdateReqVO updateReqVO) {
        wxMenuService.updateWxMenu(updateReqVO);
        return success(true);
    }

    @DeleteMapping("/delete")
    @ApiOperation("删除微信菜单")
    @ApiImplicitParam(name = "id", value = "编号", required = true, dataTypeClass = Integer.class)
    @PreAuthorize("@ss.hasPermission('wechatMp:wx-menu:delete')")
    public CommonResult<Boolean> deleteWxMenu(@RequestParam("id") Integer id) {
        wxMenuService.deleteWxMenu(id);
        return success(true);
    }

    @GetMapping("/get")
    @ApiOperation("获得微信菜单")
    @ApiImplicitParam(name = "id", value = "编号", required = true, example = "1024", dataTypeClass = Integer.class)
    @PreAuthorize("@ss.hasPermission('wechatMp:wx-menu:query')")
    public CommonResult<WxMenuRespVO> getWxMenu(@RequestParam("id") Integer id) {
        WxMenuDO wxMenu = wxMenuService.getWxMenu(id);
        return success(WxMenuConvert.INSTANCE.convert(wxMenu));
    }

    @GetMapping("/list")
    @ApiOperation("获得微信菜单列表")
    @ApiImplicitParam(name = "ids", value = "编号列表", required = true, example = "1024,2048", dataTypeClass = List.class)
    @PreAuthorize("@ss.hasPermission('wechatMp:wx-menu:query')")
    public CommonResult<List<WxMenuRespVO>> getWxMenuList(@RequestParam("ids") Collection<Integer> ids) {
        List<WxMenuDO> list = wxMenuService.getWxMenuList(ids);
        return success(WxMenuConvert.INSTANCE.convertList(list));
    }

    @GetMapping("/page")
    @ApiOperation("获得微信菜单分页")
    @PreAuthorize("@ss.hasPermission('wechatMp:wx-menu:query')")
    public CommonResult<PageResult<WxMenuRespVO>> getWxMenuPage(@Valid WxMenuPageReqVO pageVO) {
        PageResult<WxMenuDO> pageResult = wxMenuService.getWxMenuPage(pageVO);
        return success(WxMenuConvert.INSTANCE.convertPage(pageResult));
    }

    @GetMapping("/export-excel")
    @ApiOperation("导出微信菜单 Excel")
    @PreAuthorize("@ss.hasPermission('wechatMp:wx-menu:export')")
    @OperateLog(type = EXPORT)
    public void exportWxMenuExcel(@Valid WxMenuExportReqVO exportReqVO,
                                  HttpServletResponse response) throws IOException {
        List<WxMenuDO> list = wxMenuService.getWxMenuList(exportReqVO);
        // 导出 Excel
        List<WxMenuExcelVO> datas = WxMenuConvert.INSTANCE.convertList02(list);
        ExcelUtils.write(response, "微信菜单.xls", "数据", WxMenuExcelVO.class, datas);
    }

}
