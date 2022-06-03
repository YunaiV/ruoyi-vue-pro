package cn.iocoder.yudao.module.mp.controller.admin.subscribetext;

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

import cn.iocoder.yudao.module.mp.controller.admin.subscribetext.vo.*;
import cn.iocoder.yudao.module.mp.dal.dataobject.subscribetext.WxSubscribeTextDO;
import cn.iocoder.yudao.module.mp.convert.subscribetext.WxSubscribeTextConvert;
import cn.iocoder.yudao.module.mp.service.subscribetext.WxSubscribeTextService;

@Api(tags = "管理后台 - 关注欢迎语")
@RestController
@RequestMapping("/wechatMp/wx-subscribe-text")
@Validated
public class WxSubscribeTextController {

    @Resource
    private WxSubscribeTextService wxSubscribeTextService;

    @PostMapping("/create")
    @ApiOperation("创建关注欢迎语")
    @PreAuthorize("@ss.hasPermission('wechatMp:wx-subscribe-text:create')")
    public CommonResult<Integer> createWxSubscribeText(@Valid @RequestBody WxSubscribeTextCreateReqVO createReqVO) {
        return success(wxSubscribeTextService.createWxSubscribeText(createReqVO));
    }

    @PutMapping("/update")
    @ApiOperation("更新关注欢迎语")
    @PreAuthorize("@ss.hasPermission('wechatMp:wx-subscribe-text:update')")
    public CommonResult<Boolean> updateWxSubscribeText(@Valid @RequestBody WxSubscribeTextUpdateReqVO updateReqVO) {
        wxSubscribeTextService.updateWxSubscribeText(updateReqVO);
        return success(true);
    }

    @DeleteMapping("/delete")
    @ApiOperation("删除关注欢迎语")
    @ApiImplicitParam(name = "id", value = "编号", required = true, dataTypeClass = Integer.class)
    @PreAuthorize("@ss.hasPermission('wechatMp:wx-subscribe-text:delete')")
    public CommonResult<Boolean> deleteWxSubscribeText(@RequestParam("id") Integer id) {
        wxSubscribeTextService.deleteWxSubscribeText(id);
        return success(true);
    }

    @GetMapping("/get")
    @ApiOperation("获得关注欢迎语")
    @ApiImplicitParam(name = "id", value = "编号", required = true, example = "1024", dataTypeClass = Integer.class)
    @PreAuthorize("@ss.hasPermission('wechatMp:wx-subscribe-text:query')")
    public CommonResult<WxSubscribeTextRespVO> getWxSubscribeText(@RequestParam("id") Integer id) {
        WxSubscribeTextDO wxSubscribeText = wxSubscribeTextService.getWxSubscribeText(id);
        return success(WxSubscribeTextConvert.INSTANCE.convert(wxSubscribeText));
    }

    @GetMapping("/list")
    @ApiOperation("获得关注欢迎语列表")
    @ApiImplicitParam(name = "ids", value = "编号列表", required = true, example = "1024,2048", dataTypeClass = List.class)
    @PreAuthorize("@ss.hasPermission('wechatMp:wx-subscribe-text:query')")
    public CommonResult<List<WxSubscribeTextRespVO>> getWxSubscribeTextList(@RequestParam("ids") Collection<Integer> ids) {
        List<WxSubscribeTextDO> list = wxSubscribeTextService.getWxSubscribeTextList(ids);
        return success(WxSubscribeTextConvert.INSTANCE.convertList(list));
    }

    @GetMapping("/page")
    @ApiOperation("获得关注欢迎语分页")
    @PreAuthorize("@ss.hasPermission('wechatMp:wx-subscribe-text:query')")
    public CommonResult<PageResult<WxSubscribeTextRespVO>> getWxSubscribeTextPage(@Valid WxSubscribeTextPageReqVO pageVO) {
        PageResult<WxSubscribeTextDO> pageResult = wxSubscribeTextService.getWxSubscribeTextPage(pageVO);
        return success(WxSubscribeTextConvert.INSTANCE.convertPage(pageResult));
    }

    @GetMapping("/export-excel")
    @ApiOperation("导出关注欢迎语 Excel")
    @PreAuthorize("@ss.hasPermission('wechatMp:wx-subscribe-text:export')")
    @OperateLog(type = EXPORT)
    public void exportWxSubscribeTextExcel(@Valid WxSubscribeTextExportReqVO exportReqVO,
                                           HttpServletResponse response) throws IOException {
        List<WxSubscribeTextDO> list = wxSubscribeTextService.getWxSubscribeTextList(exportReqVO);
        // 导出 Excel
        List<WxSubscribeTextExcelVO> datas = WxSubscribeTextConvert.INSTANCE.convertList02(list);
        ExcelUtils.write(response, "关注欢迎语.xls", "数据", WxSubscribeTextExcelVO.class, datas);
    }

}
