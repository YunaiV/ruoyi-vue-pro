package cn.iocoder.yudao.adminserver.modules.pay.controller.channel;

import cn.iocoder.yudao.coreservice.modules.pay.dal.dataobject.merchant.PayChannelDO;
import cn.iocoder.yudao.framework.common.util.json.JsonUtils;
import cn.iocoder.yudao.framework.pay.core.client.impl.wx.WXPayClientConfig;
import cn.iocoder.yudao.framework.pay.core.client.impl.wx.WXPubPayClient;
import org.springframework.util.Assert;
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

import cn.iocoder.yudao.adminserver.modules.pay.controller.channel.vo.*;
import cn.iocoder.yudao.adminserver.modules.pay.convert.channel.PayChannelConvert;
import cn.iocoder.yudao.adminserver.modules.pay.service.channel.PayChannelService;
import org.springframework.web.multipart.MultipartFile;

@Api(tags = "支付渠道")
@RestController
@RequestMapping("/pay/channel")
@Validated
public class PayChannelController {

    @Resource
    private PayChannelService channelService;

    // todo 芋艿 这几个生成的方法是没用到的 您看要不删除了把？ -----start
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
    @ApiImplicitParam(name = "id", value = "编号", required = true)
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

    // todo 芋艿 这几个生成的方法是没用到的 您看要不删除了把？ -----end

    @PostMapping("/parsing-pem")
    @ApiOperation("解析pem证书转换为字符串")
    @PreAuthorize("@ss.hasPermission('pay:channel:parsing')")
    @ApiImplicitParam(name = "file", value = "pem文件", required = true, dataTypeClass = MultipartFile.class)
    public CommonResult<String> parsingPemFile(@RequestParam("file") MultipartFile file) {
        return success(channelService.parsingPemFile(file));
    }

    @PostMapping("/create-wechat")
    @ApiOperation("创建支付渠道 ")
    @PreAuthorize("@ss.hasPermission('pay:channel:create')")
    public CommonResult<Long> createWechatChannel(@Valid @RequestBody PayWechatChannelCreateReqVO reqVO) {
        // 针对于 V2 或者 V3 版本的参数校验
        this.paramAdvanceCheck(reqVO.getWeChatConfig().getApiVersion(),reqVO.getWeChatConfig().getMchKey(),
                reqVO.getWeChatConfig().getPrivateKeyContent(),reqVO.getWeChatConfig().getPrivateCertContent());

        return success(channelService.createWechatChannel(reqVO));
    }

    @GetMapping("/get-wechat")
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
    public CommonResult<PayWeChatChannelRespVO> getWeChatChannel(
            @RequestParam Long merchantId, @RequestParam Long appId, @RequestParam String code) {

        // 獲取渠道
        PayChannelDO channel = channelService.getChannelByConditions(merchantId, appId, code);
        if (channel == null) {
            return success(new PayWeChatChannelRespVO());
        }

        // 拼凑数据
        PayWeChatChannelRespVO respVo = PayChannelConvert.INSTANCE.convert2(channel);
        WXPayClientConfig config = (WXPayClientConfig) channel.getConfig();
        respVo.setWeChatConfig(PayChannelConvert.INSTANCE.configConvert(config));
        return success(respVo);
    }

    @PutMapping("/update-wechat")
    @ApiOperation("更新微信支付渠道 ")
    @PreAuthorize("@ss.hasPermission('pay:channel:update')")
    public CommonResult<Boolean> updateWechatChannel(@Valid @RequestBody PayWechatChannelUpdateReqVO updateReqVO) {

        // 针对于 V2 或者 V3 版本的参数校验
        this.paramAdvanceCheck(updateReqVO.getWeChatConfig().getApiVersion(),updateReqVO.getWeChatConfig().getMchKey(),
                updateReqVO.getWeChatConfig().getPrivateKeyContent(),updateReqVO.getWeChatConfig().getPrivateCertContent());

        channelService.updateWechatChannel(updateReqVO);
        return success(true);
    }

    /**
     * 预检测微信秘钥参数
     * @param version 版本
     * @param mchKey v2版本秘钥
     * @param privateKeyContent  v3版本apiclient_key
     * @param privateCertContent v3版本中apiclient_cert
     */
    private void paramAdvanceCheck(String version, String mchKey, String privateKeyContent, String privateCertContent) {
        // 针对于 V2 或者 V3 版本的参数校验
        if (version.equals(WXPayClientConfig.API_VERSION_V2)) {
            Assert.notNull(mchKey, "v2版本中商户密钥不可为空");
        }
        if (version.equals(WXPayClientConfig.API_VERSION_V3)) {
            Assert.notNull(privateKeyContent, "v3版本apiclient_key.pem不可为空");
            Assert.notNull(privateCertContent, "v3版本中apiclient_cert.pem不可为空");
        }
    }

}
