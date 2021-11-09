package cn.iocoder.yudao.adminserver.modules.pay.service.app.impl;

import cn.hutool.core.util.StrUtil;
import cn.iocoder.yudao.adminserver.modules.pay.service.merchant.PayMerchantService;
import cn.iocoder.yudao.coreservice.modules.pay.dal.dataobject.merchant.PayAppDO;
import cn.iocoder.yudao.coreservice.modules.pay.dal.dataobject.merchant.PayMerchantDO;
import cn.iocoder.yudao.framework.common.enums.CommonStatusEnum;
import cn.iocoder.yudao.framework.common.util.collection.CollectionUtils;
import com.google.common.annotations.VisibleForTesting;
import org.springframework.stereotype.Service;
import javax.annotation.Resource;
import org.springframework.validation.annotation.Validated;

import java.util.*;
import cn.iocoder.yudao.adminserver.modules.pay.controller.app.vo.*;
import cn.iocoder.yudao.framework.common.pojo.PageResult;

import cn.iocoder.yudao.adminserver.modules.pay.convert.app.PayAppConvert;
import cn.iocoder.yudao.adminserver.modules.pay.dal.mysql.app.PayAppMapper;
import cn.iocoder.yudao.adminserver.modules.pay.service.app.PayAppService;

import static cn.iocoder.yudao.framework.common.exception.util.ServiceExceptionUtil.exception;
import static cn.iocoder.yudao.coreservice.modules.pay.enums.PayErrorCodeCoreConstants.*;

/**
 * 支付应用信息 Service 实现类
 *
 * @author aquan
 */
@Service
@Validated
public class PayAppServiceImpl implements PayAppService {

    @Resource
    private PayAppMapper appMapper;

    /**
     * 商户 service 组件
     */
    @Resource
    private PayMerchantService merchantService;

    @Override
    public Long createApp(PayAppCreateReqVO createReqVO) {
        // 插入
        PayAppDO app = PayAppConvert.INSTANCE.convert(createReqVO);
        appMapper.insert(app);
        // 返回
        return app.getId();
    }

    @Override
    public void updateApp(PayAppUpdateReqVO updateReqVO) {
        // 校验存在
        this.validateAppExists(updateReqVO.getId());
        // 更新
        PayAppDO updateObj = PayAppConvert.INSTANCE.convert(updateReqVO);
        appMapper.updateById(updateObj);
    }

    @Override
    public void deleteApp(Long id) {
        // 校验存在
        this.validateAppExists(id);
        // 删除
        appMapper.deleteById(id);
    }

    private void validateAppExists(Long id) {
        if (appMapper.selectById(id) == null) {
            throw exception(APP_NOT_EXISTS);
        }
    }

    @Override
    public PayAppDO getApp(Long id) {
        return appMapper.selectById(id);
    }

    @Override
    public List<PayAppDO> getAppList(Collection<Long> ids) {
        return appMapper.selectBatchIds(ids);
    }

    @Override
    public PageResult<PayAppDO> getAppPage(PayAppPageReqVO pageReqVO) {
        return appMapper.selectPage(pageReqVO,this.getMerchantCondition(pageReqVO.getMerchantName()));
    }

    @Override
    public List<PayAppDO> getAppList(PayAppExportReqVO exportReqVO) {
        return appMapper.selectList(exportReqVO);
    }

    /**
     * 获取商户编号集合，根据商户名称模糊查询得到所有的商户编号集合
     * @param merchantName 商户名称
     * @return 商户编号集合
     */
    private Set<Long> getMerchantCondition(String merchantName) {
        if (StrUtil.isBlank(merchantName)) {
            return Collections.emptySet();
        }
        return CollectionUtils.convertSet(merchantService.getMerchantListByName(merchantName), PayMerchantDO::getId);
    }

    /**
     * 修改应用信息状态
     *
     * @param id     应用编号
     * @param status 状态{@link CommonStatusEnum}
     */
    @Override
    public void updateAppStatus(Long id, Integer status) {
        // 校验商户存在
        this.checkAppExists(id);
        // 更新状态
        PayAppDO app = new PayAppDO();
        app.setId(id);
        app.setStatus(status);
        appMapper.updateById(app);
    }


    /**
     * 检查商户是否存在
     * @param id 商户编号
     */
    @VisibleForTesting
    public void checkAppExists(Long id) {

        if (id == null) {
            return;
        }
        PayAppDO payApp = appMapper.selectById(id);
        if (payApp == null) {
            throw exception(APP_NOT_EXISTS);
        }
    }
}
