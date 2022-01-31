package cn.iocoder.yudao.module.pay.controller.admin.merchant;

import cn.iocoder.yudao.module.pay.controller.admin.merchant.vo.channel.*;
import cn.iocoder.yudao.module.pay.convert.channel.PayChannelConvert;
import cn.iocoder.yudao.module.pay.dal.dataobject.merchant.PayChannelDO;
import cn.iocoder.yudao.module.pay.service.merchant.PayChannelService;
import cn.iocoder.yudao.framework.common.pojo.CommonResult;
import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.excel.core.util.ExcelUtils;
import cn.iocoder.yudao.framework.operatelog.core.annotations.OperateLog;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiImplicitParams;
import io.swagger.annotations.ApiOperation;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletResponse;
import javax.validation.Valid;
import java.io.IOException;
import java.util.Collection;
import java.util.List;

import static cn.iocoder.yudao.framework.common.pojo.CommonResult.success;
import static cn.iocoder.yudao.framework.operatelog.core.enums.OperateTypeEnum.EXPORT;

@Api(tags = "管理后台 - 支付渠道")
@RestController
@RequestMapping("/pay/channel")
@Validated
public class PayChannelController {

    @Resource
    private PayChannelService channelService;

    @PostMapping("/create")
    @ApiOperation("创建支付渠道 ")
    @PreAuthorize("@ss.hasPermission('pay:channel:create')")
    public CommonResult<Long> createChannel(@Valid @RequestBody PayChannelCreateReqVO createReqVO) {
        return success(channelService.createChannel(createReqVO));
    }

    @PutMapping("/update")
    @ApiOperation("更新支付渠道 ")
    @PreAuthorize("@ss.hasPermission('pay:channel:update')")
    public CommonResult<Boolean> updateChannel(@Valid @RequestBody PayChannelUpdateReqVO updateReqVO) {
        channelService.updateChannel(updateReqVO);
        return success(true);
    }

    @DeleteMapping("/delete")
    @ApiOperation("删除支付渠道 ")
    @ApiImplicitParam(name = "id", value = "编号", required = true, dataTypeClass = Long.class)
    @PreAuthorize("@ss.hasPermission('pay:channel:delete')")
    public CommonResult<Boolean> deleteChannel(@RequestParam("id") Long id) {
        channelService.deleteChannel(id);
        return success(true);
    }

    @GetMapping("/get")
    @ApiOperation("获得支付渠道 ")
    @ApiImplicitParam(name = "id", value = "编号", required = true, example = "1024", dataTypeClass = Long.class)
    @PreAuthorize("@ss.hasPermission('pay:channel:query')")
    public CommonResult<PayChannelRespVO> getChannel(@RequestParam("id") Long id) {
        PayChannelDO channel = channelService.getChannel(id);
        return success(PayChannelConvert.INSTANCE.convert(channel));
    }

    @GetMapping("/list")
    @ApiOperation("获得支付渠道列表")
    @ApiImplicitParam(name = "ids", value = "编号列表",
            required = true, example = "1024,2048", dataTypeClass = List.class)
    @PreAuthorize("@ss.hasPermission('pay:channel:query')")
    public CommonResult<List<PayChannelRespVO>> getChannelList(@RequestParam("ids") Collection<Long> ids) {
        List<PayChannelDO> list = channelService.getChannelList(ids);
        return success(PayChannelConvert.INSTANCE.convertList(list));
    }

    @GetMapping("/page")
    @ApiOperation("获得支付渠道分页")
    @PreAuthorize("@ss.hasPermission('pay:channel:query')")
    public CommonResult<PageResult<PayChannelRespVO>> getChannelPage(@Valid PayChannelPageReqVO pageVO) {
        PageResult<PayChannelDO> pageResult = channelService.getChannelPage(pageVO);
        return success(PayChannelConvert.INSTANCE.convertPage(pageResult));
    }

    @GetMapping("/export-excel")
    @ApiOperation("导出支付渠道Excel")
    @PreAuthorize("@ss.hasPermission('pay:channel:export')")
    @OperateLog(type = EXPORT)
    public void exportChannelExcel(@Valid PayChannelExportReqVO exportReqVO,
                                   HttpServletResponse response) throws IOException {
        List<PayChannelDO> list = channelService.getChannelList(exportReqVO);
        // 导出 Excel
        List<PayChannelExcelVO> datas = PayChannelConvert.INSTANCE.convertList02(list);
        ExcelUtils.write(response, "支付渠道.xls", "数据", PayChannelExcelVO.class, datas);
    }

    @GetMapping("/get-channel")
    @ApiOperation("根据条件查询微信支付渠道")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "merchantId", value = "商户编号",
                    required = true, example = "1", dataTypeClass = Long.class),
            @ApiImplicitParam(name = "appId", value = "应用编号",
                    required = true, example = "1", dataTypeClass = Long.class),
            @ApiImplicitParam(name = "code", value = "支付渠道编码",
                    required = true, example = "wx_pub", dataTypeClass = String.class)
    })
    @PreAuthorize("@ss.hasPermission('pay:channel:query')")
    public CommonResult<PayChannelRespVO> getChannel(
            @RequestParam Long merchantId, @RequestParam Long appId, @RequestParam String code) {
        // 獲取渠道
        PayChannelDO channel = channelService.getChannelByConditions(merchantId, appId, code);
        if (channel == null) {
            return success(new PayChannelRespVO());
        }
        // 拼凑数据
        PayChannelRespVO respVo = PayChannelConvert.INSTANCE.convert(channel);
        return success(respVo);
    }

}
