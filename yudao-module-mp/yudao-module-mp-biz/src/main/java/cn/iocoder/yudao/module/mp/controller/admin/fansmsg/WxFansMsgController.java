package cn.iocoder.yudao.module.mp.controller.admin.fansmsg;

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

import cn.iocoder.yudao.module.mp.controller.admin.fansmsg.vo.*;
import cn.iocoder.yudao.module.mp.dal.dataobject.fansmsg.WxFansMsgDO;
import cn.iocoder.yudao.module.mp.convert.fansmsg.WxFansMsgConvert;
import cn.iocoder.yudao.module.mp.service.fansmsg.WxFansMsgService;

@Api(tags = "管理后台 - 粉丝消息表 ")
@RestController
@RequestMapping("/wechatMp/wx-fans-msg")
@Validated
public class WxFansMsgController {

    @Resource
    private WxFansMsgService wxFansMsgService;

    @PostMapping("/create")
    @ApiOperation("创建粉丝消息表 ")
    @PreAuthorize("@ss.hasPermission('wechatMp:wx-fans-msg:create')")
    public CommonResult<Integer> createWxFansMsg(@Valid @RequestBody WxFansMsgCreateReqVO createReqVO) {
        return success(wxFansMsgService.createWxFansMsg(createReqVO));
    }

    @PutMapping("/update")
    @ApiOperation("更新粉丝消息表 ")
    @PreAuthorize("@ss.hasPermission('wechatMp:wx-fans-msg:update')")
    public CommonResult<Boolean> updateWxFansMsg(@Valid @RequestBody WxFansMsgUpdateReqVO updateReqVO) {
        wxFansMsgService.updateWxFansMsg(updateReqVO);
        return success(true);
    }

    @DeleteMapping("/delete")
    @ApiOperation("删除粉丝消息表 ")
    @ApiImplicitParam(name = "id", value = "编号", required = true, dataTypeClass = Integer.class)
    @PreAuthorize("@ss.hasPermission('wechatMp:wx-fans-msg:delete')")
    public CommonResult<Boolean> deleteWxFansMsg(@RequestParam("id") Integer id) {
        wxFansMsgService.deleteWxFansMsg(id);
        return success(true);
    }

    @GetMapping("/get")
    @ApiOperation("获得粉丝消息表 ")
    @ApiImplicitParam(name = "id", value = "编号", required = true, example = "1024", dataTypeClass = Integer.class)
    @PreAuthorize("@ss.hasPermission('wechatMp:wx-fans-msg:query')")
    public CommonResult<WxFansMsgRespVO> getWxFansMsg(@RequestParam("id") Integer id) {
        WxFansMsgDO wxFansMsg = wxFansMsgService.getWxFansMsg(id);
        return success(WxFansMsgConvert.INSTANCE.convert(wxFansMsg));
    }

    @GetMapping("/list")
    @ApiOperation("获得粉丝消息表 列表")
    @ApiImplicitParam(name = "ids", value = "编号列表", required = true, example = "1024,2048", dataTypeClass = List.class)
    @PreAuthorize("@ss.hasPermission('wechatMp:wx-fans-msg:query')")
    public CommonResult<List<WxFansMsgRespVO>> getWxFansMsgList(@RequestParam("ids") Collection<Integer> ids) {
        List<WxFansMsgDO> list = wxFansMsgService.getWxFansMsgList(ids);
        return success(WxFansMsgConvert.INSTANCE.convertList(list));
    }

    @GetMapping("/page")
    @ApiOperation("获得粉丝消息表 分页")
    @PreAuthorize("@ss.hasPermission('wechatMp:wx-fans-msg:query')")
    public CommonResult<PageResult<WxFansMsgRespVO>> getWxFansMsgPage(@Valid WxFansMsgPageReqVO pageVO) {
        PageResult<WxFansMsgDO> pageResult = wxFansMsgService.getWxFansMsgPage(pageVO);
        return success(WxFansMsgConvert.INSTANCE.convertPage(pageResult));
    }

    @GetMapping("/export-excel")
    @ApiOperation("导出粉丝消息表  Excel")
    @PreAuthorize("@ss.hasPermission('wechatMp:wx-fans-msg:export')")
    @OperateLog(type = EXPORT)
    public void exportWxFansMsgExcel(@Valid WxFansMsgExportReqVO exportReqVO,
                                     HttpServletResponse response) throws IOException {
        List<WxFansMsgDO> list = wxFansMsgService.getWxFansMsgList(exportReqVO);
        // 导出 Excel
        List<WxFansMsgExcelVO> datas = WxFansMsgConvert.INSTANCE.convertList02(list);
        ExcelUtils.write(response, "粉丝消息表 .xls", "数据", WxFansMsgExcelVO.class, datas);
    }

}
