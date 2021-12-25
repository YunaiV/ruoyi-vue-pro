package cn.iocoder.yudao.adminserver.modules.pay.service.merchant.impl;

import cn.hutool.core.date.DateUtil;
import cn.iocoder.yudao.adminserver.modules.pay.controller.merchant.vo.PayMerchantCreateReqVO;
import cn.iocoder.yudao.adminserver.modules.pay.controller.merchant.vo.PayMerchantExportReqVO;
import cn.iocoder.yudao.adminserver.modules.pay.controller.merchant.vo.PayMerchantPageReqVO;
import cn.iocoder.yudao.adminserver.modules.pay.controller.merchant.vo.PayMerchantUpdateReqVO;
import cn.iocoder.yudao.adminserver.modules.pay.convert.merchant.PayMerchantConvert;
import cn.iocoder.yudao.adminserver.modules.pay.dal.mysql.merchant.PayMerchantMapper;
import cn.iocoder.yudao.adminserver.modules.pay.service.merchant.PayMerchantService;
import cn.iocoder.yudao.coreservice.modules.pay.dal.dataobject.merchant.PayMerchantDO;
import cn.iocoder.yudao.framework.common.pojo.PageResult;
import com.google.common.annotations.VisibleForTesting;
import org.springframework.stereotype.Service;
import org.springframework.validation.annotation.Validated;

import javax.annotation.Resource;
import java.time.LocalDateTime;
import java.util.Collection;
import java.util.List;

import static cn.iocoder.yudao.coreservice.modules.pay.enums.PayErrorCodeCoreConstants.PAY_MERCHANT_NOT_EXISTS;
import static cn.iocoder.yudao.framework.common.exception.util.ServiceExceptionUtil.exception;

/**
 * 支付商户信息 Service 实现类
 *
 * @author aquan
 */
@Service
@Validated
public class PayMerchantServiceImpl implements PayMerchantService {

    @Resource
    private PayMerchantMapper merchantMapper;

    @Override
    public Long createMerchant(PayMerchantCreateReqVO createReqVO) {
        // 插入
        PayMerchantDO merchant = PayMerchantConvert.INSTANCE.convert(createReqVO);
        merchant.setNo(this.generateMerchantNo());
        merchantMapper.insert(merchant);
        // 返回
        return merchant.getId();
    }

    @Override
    public void updateMerchant(PayMerchantUpdateReqVO updateReqVO) {
        // 校验存在
        this.validateMerchantExists(updateReqVO.getId());
        // 更新
        PayMerchantDO updateObj = PayMerchantConvert.INSTANCE.convert(updateReqVO);
        merchantMapper.updateById(updateObj);
    }

    @Override
    public void deleteMerchant(Long id) {
        // 校验存在
        this.validateMerchantExists(id);
        // 删除
        merchantMapper.deleteById(id);
    }

    private void validateMerchantExists(Long id) {
        if (merchantMapper.selectById(id) == null) {
            throw exception(PAY_MERCHANT_NOT_EXISTS);
        }
    }

    @Override
    public PayMerchantDO getMerchant(Long id) {
        return merchantMapper.selectById(id);
    }

    @Override
    public List<PayMerchantDO> getMerchantList(Collection<Long> ids) {
        return merchantMapper.selectBatchIds(ids);
    }

    @Override
    public PageResult<PayMerchantDO> getMerchantPage(PayMerchantPageReqVO pageReqVO) {
        return merchantMapper.selectPage(pageReqVO);
    }

    @Override
    public List<PayMerchantDO> getMerchantList(PayMerchantExportReqVO exportReqVO) {
        return merchantMapper.selectList(exportReqVO);
    }

    // TODO @aquan：接口上已经有注释，这里不用在有啦
    /**
     * 修改商户状态
     *
     * @param id     商户编号
     * @param status 状态
     */
    @Override
    public void updateMerchantStatus(Long id, Integer status) {
        // 校验商户存在
        this.checkMerchantExists(id);
        // 更新状态
        PayMerchantDO merchant = new PayMerchantDO();
        merchant.setId(id);
        merchant.setStatus(status);
        merchantMapper.updateById(merchant);
    }

    /**
     * 根据商户名称模糊查询商户集合
     *
     * @param merchantName 商户名称
     * @return 商户集合
     */
    @Override
    public List<PayMerchantDO> getMerchantListByName(String merchantName) {
        return this.merchantMapper.getMerchantListByName(merchantName);
    }

    /**
     * 检查商户是否存在
     * @param id 商户编号
     */
    @VisibleForTesting
    public void checkMerchantExists(Long id) {
        if (id == null) {
            return;
        }
        PayMerchantDO merchant = merchantMapper.selectById(id);
        if (merchant == null) {
            throw exception(PAY_MERCHANT_NOT_EXISTS);
        }
    }


    // TODO @芋艿：后续增加下合适的算法
    /**
     * 根据年月日时分秒毫秒生成商户号
     *
     * @return 商户号
     */
    private String generateMerchantNo(){
       return  "M" + DateUtil.format(LocalDateTime.now(),"yyyyMMddHHmmssSSS");
    }

}
