package cn.iocoder.yudao.module.pay.controller.admin.merchant;

import cn.hutool.core.collection.CollUtil;
import cn.iocoder.yudao.framework.common.pojo.CommonResult;
import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.common.util.collection.CollectionUtils;
import cn.iocoder.yudao.framework.excel.core.util.ExcelUtils;
import cn.iocoder.yudao.framework.operatelog.core.annotations.OperateLog;
import cn.iocoder.yudao.framework.pay.core.enums.PayChannelEnum;
import cn.iocoder.yudao.module.pay.controller.admin.merchant.vo.app.*;
import cn.iocoder.yudao.module.pay.convert.app.PayAppConvert;
import cn.iocoder.yudao.module.pay.dal.dataobject.merchant.PayAppDO;
import cn.iocoder.yudao.module.pay.dal.dataobject.merchant.PayChannelDO;
import cn.iocoder.yudao.module.pay.dal.dataobject.merchant.PayMerchantDO;
import cn.iocoder.yudao.module.pay.service.merchant.PayAppService;
import cn.iocoder.yudao.module.pay.service.merchant.PayChannelService;
import cn.iocoder.yudao.module.pay.service.merchant.PayMerchantService;
import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.Operation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletResponse;
import javax.validation.Valid;
import java.io.IOException;
import java.util.*;

import static cn.iocoder.yudao.framework.common.pojo.CommonResult.success;
import static cn.iocoder.yudao.framework.operatelog.core.enums.OperateTypeEnum.EXPORT;

@Slf4j
@Tag(name = "管理后台 - 支付应用信息")
@RestController
@RequestMapping("/pay/app")
@Validated
public class PayAppController {

    @Resource
    private PayAppService appService;
    @Resource
    private PayChannelService channelService;
    @Resource
    private PayMerchantService merchantService;

    @PostMapping("/create")
    @Operation(summary = "创建支付应用信息")
    @PreAuthorize("@ss.hasPermission('pay:app:create')")
    public CommonResult<Long> createApp(@Valid @RequestBody PayAppCreateReqVO createReqVO) {
        return success(appService.createApp(createReqVO));
    }

    @PutMapping("/update")
    @Operation(summary = "更新支付应用信息")
    @PreAuthorize("@ss.hasPermission('pay:app:update')")
    public CommonResult<Boolean> updateApp(@Valid @RequestBody PayAppUpdateReqVO updateReqVO) {
        appService.updateApp(updateReqVO);
        return success(true);
    }

    @PutMapping("/update-status")
    @Operation(summary = "更新支付应用状态")
    @PreAuthorize("@ss.hasPermission('pay:app:update')")
    public CommonResult<Boolean> updateAppStatus(@Valid @RequestBody PayAppUpdateStatusReqVO updateReqVO) {
        appService.updateAppStatus(updateReqVO.getId(), updateReqVO.getStatus());
        return success(true);
    }

    @DeleteMapping("/delete")
    @Operation(summary = "删除支付应用信息")
    @Parameter(name = "id", description = "编号", required = true)
    @PreAuthorize("@ss.hasPermission('pay:app:delete')")
    public CommonResult<Boolean> deleteApp(@RequestParam("id") Long id) {
        appService.deleteApp(id);
        return success(true);
    }

    @GetMapping("/get")
    @Operation(summary = "获得支付应用信息")
    @Parameter(name = "id", description = "编号", required = true, example = "1024")
    @PreAuthorize("@ss.hasPermission('pay:app:query')")
    public CommonResult<PayAppRespVO> getApp(@RequestParam("id") Long id) {
        PayAppDO app = appService.getApp(id);
        return success(PayAppConvert.INSTANCE.convert(app));
    }

    @GetMapping("/list")
    @Operation(summary = "获得支付应用信息列表")
    @Parameter(name = "ids", description = "编号列表", required = true, example = "1024,2048")
    @PreAuthorize("@ss.hasPermission('pay:app:query')")
    public CommonResult<List<PayAppRespVO>> getAppList(@RequestParam("ids") Collection<Long> ids) {
        List<PayAppDO> list = appService.getAppList(ids);
        return success(PayAppConvert.INSTANCE.convertList(list));
    }

    @GetMapping("/page")
    @Operation(summary = "获得支付应用信息分页")
    @PreAuthorize("@ss.hasPermission('pay:app:query')")
    public CommonResult<PageResult<PayAppPageItemRespVO>> getAppPage(@Valid PayAppPageReqVO pageVO) {
        // 得到应用分页列表
        PageResult<PayAppDO> pageResult = appService.getAppPage(pageVO);
        if (CollUtil.isEmpty(pageResult.getList())) {
            return success(new PageResult<>(pageResult.getTotal()));
        }

        // 得到所有的应用编号，查出所有的渠道
        Collection<Long> payAppIds = CollectionUtils.convertList(pageResult.getList(), PayAppDO::getId);
        List<PayChannelDO> channels = channelService.getChannelListByAppIds(payAppIds);
        // TODO @aquan：可以基于 appId 简历一个 multiMap。这样下面，直接 get 到之后，CollUtil buildSet 即可
        Iterator<PayChannelDO> iterator = channels.iterator();

        // 得到所有的商户信息
        Collection<Long> merchantIds = CollectionUtils.convertList(pageResult.getList(), PayAppDO::getMerchantId);
        Map<Long, PayMerchantDO> deptMap = merchantService.getMerchantMap(merchantIds);

        // 利用反射将渠道数据复制到返回的数据结构中去
        List<PayAppPageItemRespVO> appList = new ArrayList<>(pageResult.getList().size());
        pageResult.getList().forEach(app -> {
            // 写入应用信息的数据
            PayAppPageItemRespVO respVO = PayAppConvert.INSTANCE.pageConvert(app);
            // 写入商户的数据
            respVO.setPayMerchant(PayAppConvert.INSTANCE.convert(deptMap.get(app.getMerchantId())));
            // 写入支付渠道信息的数据
            Set<String> channelCodes = new HashSet<>(PayChannelEnum.values().length);
            while (iterator.hasNext()) {
                PayChannelDO channelDO = iterator.next();
                if (channelDO.getAppId().equals(app.getId())) {
                    channelCodes.add(channelDO.getCode());
                    iterator.remove();
                }
            }
            respVO.setChannelCodes(channelCodes);
            appList.add(respVO);
        });

        return success(new PageResult<>(appList, pageResult.getTotal()));
    }

    @GetMapping("/export-excel")
    @Operation(summary = "导出支付应用信息 Excel")
    @PreAuthorize("@ss.hasPermission('pay:app:export')")
    @OperateLog(type = EXPORT)
    public void exportAppExcel(@Valid PayAppExportReqVO exportReqVO,
                               HttpServletResponse response) throws IOException {
        List<PayAppDO> list = appService.getAppList(exportReqVO);
        // 导出 Excel
        List<PayAppExcelVO> datas = PayAppConvert.INSTANCE.convertList02(list);
        ExcelUtils.write(response, "支付应用信息.xls", "数据", PayAppExcelVO.class, datas);
    }

    @GetMapping("/list-merchant-id")
    @Operation(summary = "根据商户 ID 查询支付应用信息")
    @Parameter(name = "merchantId", description = "商户ID", required = true, example = "1")
    @PreAuthorize("@ss.hasPermission('pay:merchant:query')")
    public CommonResult<List<PayAppRespVO>> getMerchantListByName(@RequestParam Long merchantId) {
        List<PayAppDO> appListDO = appService.getListByMerchantId(merchantId);
        return success(PayAppConvert.INSTANCE.convertList(appListDO));
    }

}
