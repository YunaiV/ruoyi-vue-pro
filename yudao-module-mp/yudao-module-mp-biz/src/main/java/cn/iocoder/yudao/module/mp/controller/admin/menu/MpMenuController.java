package cn.iocoder.yudao.module.mp.controller.admin.menu;

import cn.iocoder.yudao.module.mp.convert.menu.MpMenuConvert;
import cn.iocoder.yudao.module.mp.dal.dataobject.menu.MpMenuDO;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;

import org.springframework.validation.annotation.Validated;
import org.springframework.security.access.prepost.PreAuthorize;
import io.swagger.annotations.*;

import javax.validation.*;

import cn.iocoder.yudao.framework.common.pojo.CommonResult;

import static cn.iocoder.yudao.framework.common.pojo.CommonResult.success;

import cn.iocoder.yudao.module.mp.controller.admin.menu.vo.*;
import cn.iocoder.yudao.module.mp.service.menu.MpMenuService;

@Api(tags = "管理后台 - 微信菜单")
@RestController
@RequestMapping("/mp/menu")
@Validated
public class MpMenuController {

    @Resource
    private MpMenuService mpMenuService;

    @PostMapping("/save")
    @ApiOperation("保存微信菜单")
    @PreAuthorize("@ss.hasPermission('mp:menu:save')")
    public CommonResult<Long> saveMenu(@Valid @RequestBody MpMenuSaveReqVO createReqVO) {
        return success(mpMenuService.saveMenu(createReqVO));
    }

    @DeleteMapping("/delete")
    @ApiOperation("删除微信菜单")
    @ApiImplicitParam(name = "id", value = "编号", required = true, dataTypeClass = Long.class)
    @PreAuthorize("@ss.hasPermission('mp:menu:delete')")
    public CommonResult<Boolean> deleteMenu(@RequestParam("id") Long id) {
        mpMenuService.deleteMenu(id);
        return success(true);
    }

    @GetMapping("/get")
    @ApiOperation("获得微信菜单")
    @ApiImplicitParam(name = "id", value = "编号", required = true, example = "1024", dataTypeClass = Long.class)
    @PreAuthorize("@ss.hasPermission('mp:menu:query')")
    public CommonResult<MpMenuRespVO> getMenu(@RequestParam("id") Long id) {
        MpMenuDO menu = mpMenuService.getMenu(id);
        return success(MpMenuConvert.INSTANCE.convert(menu));
    }

}
