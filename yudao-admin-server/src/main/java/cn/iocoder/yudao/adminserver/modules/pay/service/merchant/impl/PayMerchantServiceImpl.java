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
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.google.common.annotations.VisibleForTesting;
import org.springframework.stereotype.Service;
import org.springframework.validation.annotation.Validated;

import javax.annotation.Resource;
import java.time.LocalDateTime;
import java.util.Collection;
import java.util.List;

import static cn.iocoder.yudao.coreservice.modules.pay.enums.PayErrorCodeCoreConstants.MERCHANT_NOT_EXISTS;
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
            throw exception(MERCHANT_NOT_EXISTS);
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
        return this.merchantMapper.selectList(new QueryWrapper<PayMerchantDO>()
                .lambda().likeRight(PayMerchantDO::getName, merchantName));
    }

    /**
     * 根据商户名称模糊查询一定数量的商户集合
     *
     * @param merchantName 商户名称
     * @return 商户集合
     */
    @Override
    public List<PayMerchantDO> getMerchantListByNameLimit(String merchantName) {

        LambdaQueryWrapper<PayMerchantDO> queryWrapper = new QueryWrapper<PayMerchantDO>().lambda()
                .select(PayMerchantDO::getId, PayMerchantDO::getName)
                .likeRight(PayMerchantDO::getName, merchantName)
                .last("limit 200");

        return this.merchantMapper.selectList(queryWrapper);
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
            throw exception(MERCHANT_NOT_EXISTS);
        }
    }

    /**
     * 获得指定编号的商户列表
     *
     * @param merchantIds 商户编号数组
     * @return 商户列表
     */
    @Override
    public List<PayMerchantDO> getSimpleMerchants(Collection<Long> merchantIds) {
        return merchantMapper.selectBatchIds(merchantIds);
    }

    /**
     * 根据年月日时分秒毫秒生成商户号
     * @return 商户号
     */
    private String generateMerchantNo(){
       return  "M" + DateUtil.format(LocalDateTime.now(),"yyyyMMddHHmmssSSS");
    }


}
