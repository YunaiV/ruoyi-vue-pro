package cn.iocoder.yudao.module.mp.controller.admin.fansmsgres;

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

import cn.iocoder.yudao.module.mp.controller.admin.fansmsgres.vo.*;
import cn.iocoder.yudao.module.mp.dal.dataobject.fansmsgres.WxFansMsgResDO;
import cn.iocoder.yudao.module.mp.convert.fansmsgres.WxFansMsgResConvert;
import cn.iocoder.yudao.module.mp.service.fansmsgres.WxFansMsgResService;

@Api(tags = "管理后台 - 回复粉丝消息历史表 ")
@RestController
@RequestMapping("/wechatMp/wx-fans-msg-res")
@Validated
public class WxFansMsgResController {

    @Resource
    private WxFansMsgResService wxFansMsgResService;

    @PostMapping("/create")
    @ApiOperation("创建回复粉丝消息历史表 ")
    @PreAuthorize("@ss.hasPermission('wechatMp:wx-fans-msg-res:create')")
    public CommonResult<Integer> createWxFansMsgRes(@Valid @RequestBody WxFansMsgResCreateReqVO createReqVO) {
        return success(wxFansMsgResService.createWxFansMsgRes(createReqVO));
    }

    @PutMapping("/update")
    @ApiOperation("更新回复粉丝消息历史表 ")
    @PreAuthorize("@ss.hasPermission('wechatMp:wx-fans-msg-res:update')")
    public CommonResult<Boolean> updateWxFansMsgRes(@Valid @RequestBody WxFansMsgResUpdateReqVO updateReqVO) {
        wxFansMsgResService.updateWxFansMsgRes(updateReqVO);
        return success(true);
    }

    @DeleteMapping("/delete")
    @ApiOperation("删除回复粉丝消息历史表 ")
    @ApiImplicitParam(name = "id", value = "编号", required = true, dataTypeClass = Integer.class)
    @PreAuthorize("@ss.hasPermission('wechatMp:wx-fans-msg-res:delete')")
    public CommonResult<Boolean> deleteWxFansMsgRes(@RequestParam("id") Integer id) {
        wxFansMsgResService.deleteWxFansMsgRes(id);
        return success(true);
    }

    @GetMapping("/get")
    @ApiOperation("获得回复粉丝消息历史表 ")
    @ApiImplicitParam(name = "id", value = "编号", required = true, example = "1024", dataTypeClass = Integer.class)
    @PreAuthorize("@ss.hasPermission('wechatMp:wx-fans-msg-res:query')")
    public CommonResult<WxFansMsgResRespVO> getWxFansMsgRes(@RequestParam("id") Integer id) {
        WxFansMsgResDO wxFansMsgRes = wxFansMsgResService.getWxFansMsgRes(id);
        return success(WxFansMsgResConvert.INSTANCE.convert(wxFansMsgRes));
    }

    @GetMapping("/list")
    @ApiOperation("获得回复粉丝消息历史表 列表")
    @ApiImplicitParam(name = "ids", value = "编号列表", required = true, example = "1024,2048", dataTypeClass = List.class)
    @PreAuthorize("@ss.hasPermission('wechatMp:wx-fans-msg-res:query')")
    public CommonResult<List<WxFansMsgResRespVO>> getWxFansMsgResList(@RequestParam("ids") Collection<Integer> ids) {
        List<WxFansMsgResDO> list = wxFansMsgResService.getWxFansMsgResList(ids);
        return success(WxFansMsgResConvert.INSTANCE.convertList(list));
    }

    @GetMapping("/page")
    @ApiOperation("获得回复粉丝消息历史表 分页")
    @PreAuthorize("@ss.hasPermission('wechatMp:wx-fans-msg-res:query')")
    public CommonResult<PageResult<WxFansMsgResRespVO>> getWxFansMsgResPage(@Valid WxFansMsgResPageReqVO pageVO) {
        PageResult<WxFansMsgResDO> pageResult = wxFansMsgResService.getWxFansMsgResPage(pageVO);
        return success(WxFansMsgResConvert.INSTANCE.convertPage(pageResult));
    }

    @GetMapping("/export-excel")
    @ApiOperation("导出回复粉丝消息历史表  Excel")
    @PreAuthorize("@ss.hasPermission('wechatMp:wx-fans-msg-res:export')")
    @OperateLog(type = EXPORT)
    public void exportWxFansMsgResExcel(@Valid WxFansMsgResExportReqVO exportReqVO,
                                        HttpServletResponse response) throws IOException {
        List<WxFansMsgResDO> list = wxFansMsgResService.getWxFansMsgResList(exportReqVO);
        // 导出 Excel
        List<WxFansMsgResExcelVO> datas = WxFansMsgResConvert.INSTANCE.convertList02(list);
        ExcelUtils.write(response, "回复粉丝消息历史表 .xls", "数据", WxFansMsgResExcelVO.class, datas);
    }

}
