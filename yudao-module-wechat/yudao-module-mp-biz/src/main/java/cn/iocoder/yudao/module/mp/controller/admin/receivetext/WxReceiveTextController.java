package cn.iocoder.yudao.module.mp.controller.admin.receivetext;

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

import cn.iocoder.yudao.module.mp.controller.admin.receivetext.vo.*;
import cn.iocoder.yudao.module.mp.dal.dataobject.receivetext.WxReceiveTextDO;
import cn.iocoder.yudao.module.mp.convert.receivetext.WxReceiveTextConvert;
import cn.iocoder.yudao.module.mp.service.receivetext.WxReceiveTextService;

@Api(tags = "管理后台 - 回复关键字")
@RestController
@RequestMapping("/wechatMp/wx-receive-text")
@Validated
public class WxReceiveTextController {

    @Resource
    private WxReceiveTextService wxReceiveTextService;

    @PostMapping("/create")
    @ApiOperation("创建回复关键字")
    @PreAuthorize("@ss.hasPermission('wechatMp:wx-receive-text:create')")
    public CommonResult<Integer> createWxReceiveText(@Valid @RequestBody WxReceiveTextCreateReqVO createReqVO) {
        return success(wxReceiveTextService.createWxReceiveText(createReqVO));
    }

    @PutMapping("/update")
    @ApiOperation("更新回复关键字")
    @PreAuthorize("@ss.hasPermission('wechatMp:wx-receive-text:update')")
    public CommonResult<Boolean> updateWxReceiveText(@Valid @RequestBody WxReceiveTextUpdateReqVO updateReqVO) {
        wxReceiveTextService.updateWxReceiveText(updateReqVO);
        return success(true);
    }

    @DeleteMapping("/delete")
    @ApiOperation("删除回复关键字")
    @ApiImplicitParam(name = "id", value = "编号", required = true, dataTypeClass = Integer.class)
    @PreAuthorize("@ss.hasPermission('wechatMp:wx-receive-text:delete')")
    public CommonResult<Boolean> deleteWxReceiveText(@RequestParam("id") Integer id) {
        wxReceiveTextService.deleteWxReceiveText(id);
        return success(true);
    }

    @GetMapping("/get")
    @ApiOperation("获得回复关键字")
    @ApiImplicitParam(name = "id", value = "编号", required = true, example = "1024", dataTypeClass = Integer.class)
    @PreAuthorize("@ss.hasPermission('wechatMp:wx-receive-text:query')")
    public CommonResult<WxReceiveTextRespVO> getWxReceiveText(@RequestParam("id") Integer id) {
        WxReceiveTextDO wxReceiveText = wxReceiveTextService.getWxReceiveText(id);
        return success(WxReceiveTextConvert.INSTANCE.convert(wxReceiveText));
    }

    @GetMapping("/list")
    @ApiOperation("获得回复关键字列表")
    @ApiImplicitParam(name = "ids", value = "编号列表", required = true, example = "1024,2048", dataTypeClass = List.class)
    @PreAuthorize("@ss.hasPermission('wechatMp:wx-receive-text:query')")
    public CommonResult<List<WxReceiveTextRespVO>> getWxReceiveTextList(@RequestParam("ids") Collection<Integer> ids) {
        List<WxReceiveTextDO> list = wxReceiveTextService.getWxReceiveTextList(ids);
        return success(WxReceiveTextConvert.INSTANCE.convertList(list));
    }

    @GetMapping("/page")
    @ApiOperation("获得回复关键字分页")
    @PreAuthorize("@ss.hasPermission('wechatMp:wx-receive-text:query')")
    public CommonResult<PageResult<WxReceiveTextRespVO>> getWxReceiveTextPage(@Valid WxReceiveTextPageReqVO pageVO) {
        PageResult<WxReceiveTextDO> pageResult = wxReceiveTextService.getWxReceiveTextPage(pageVO);
        return success(WxReceiveTextConvert.INSTANCE.convertPage(pageResult));
    }

    @GetMapping("/export-excel")
    @ApiOperation("导出回复关键字 Excel")
    @PreAuthorize("@ss.hasPermission('wechatMp:wx-receive-text:export')")
    @OperateLog(type = EXPORT)
    public void exportWxReceiveTextExcel(@Valid WxReceiveTextExportReqVO exportReqVO,
                                         HttpServletResponse response) throws IOException {
        List<WxReceiveTextDO> list = wxReceiveTextService.getWxReceiveTextList(exportReqVO);
        // 导出 Excel
        List<WxReceiveTextExcelVO> datas = WxReceiveTextConvert.INSTANCE.convertList02(list);
        ExcelUtils.write(response, "回复关键字.xls", "数据", WxReceiveTextExcelVO.class, datas);
    }

}
