package cn.iocoder.yudao.module.pay.service.app;

import cn.hutool.core.util.StrUtil;
import cn.iocoder.yudao.framework.common.enums.CommonStatusEnum;
import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.mybatis.core.query.LambdaQueryWrapperX;
import cn.iocoder.yudao.module.pay.controller.admin.app.vo.PayAppCreateReqVO;
import cn.iocoder.yudao.module.pay.controller.admin.app.vo.PayAppPageReqVO;
import cn.iocoder.yudao.module.pay.controller.admin.app.vo.PayAppUpdateReqVO;
import cn.iocoder.yudao.module.pay.convert.app.PayAppConvert;
import cn.iocoder.yudao.module.pay.dal.dataobject.app.PayAppDO;
import cn.iocoder.yudao.module.pay.dal.mysql.app.PayAppMapper;
import cn.iocoder.yudao.module.pay.enums.ErrorCodeConstants;
import cn.iocoder.yudao.module.pay.service.order.PayOrderService;
import cn.iocoder.yudao.module.pay.service.refund.PayRefundService;
import jakarta.annotation.Resource;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;
import org.springframework.validation.annotation.Validated;

import java.util.Collection;
import java.util.List;
import java.util.Objects;

import static cn.iocoder.yudao.framework.common.exception.util.ServiceExceptionUtil.exception;
import static cn.iocoder.yudao.module.pay.enums.ErrorCodeConstants.*;

/**
 * 支付应用 Service 实现类
 *
 * @author aquan
 */
@Service
@Validated
public class PayAppServiceImpl implements PayAppService {

    @Resource
    private PayAppMapper appMapper;

    @Resource
    @Lazy // 延迟加载，避免循环依赖报错
    private PayOrderService orderService;
    @Resource
    @Lazy // 延迟加载，避免循环依赖报错
    private PayRefundService refundService;

    @Override
    public Long createApp(PayAppCreateReqVO createReqVO) {
        // 验证appKey是否重复
        validateAppKeyDuplicate(null, createReqVO.getAppKey());
        // 插入
        PayAppDO app = PayAppConvert.INSTANCE.convert(createReqVO);
        appMapper.insert(app);
        // 返回
        return app.getId();
    }

    @Override
    public void updateApp(PayAppUpdateReqVO updateReqVO) {
        // 校验存在
        validateAppExists(updateReqVO.getId());
        // 验证appKey是否重复
        validateAppKeyDuplicate(updateReqVO.getId(), updateReqVO.getAppKey());
        // 更新
        PayAppDO updateObj = PayAppConvert.INSTANCE.convert(updateReqVO);
        appMapper.updateById(updateObj);
    }

    @Override
    public void updateAppStatus(Long id, Integer status) {
        // 校验商户存在
        validateAppExists(id);
        // 更新状态
        appMapper.updateById(new PayAppDO().setId(id).setStatus(status));
    }

    @Override
    public void deleteApp(Long id) {
        // 校验存在
        validateAppExists(id);
        // 校验关联数据是否存在
        if (orderService.getOrderCountByAppId(id) > 0) {
            throw exception(APP_EXIST_ORDER_CANT_DELETE);
        }
        if (refundService.getRefundCountByAppId(id) > 0) {
            throw exception(APP_EXIST_REFUND_CANT_DELETE);
        }

        // 删除
        appMapper.deleteById(id);
    }

    private void validateAppExists(Long id) {
        if (appMapper.selectById(id) == null) {
            throw exception(APP_NOT_FOUND);
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
    public List<PayAppDO> getAppList() {
        return appMapper.selectList();
    }

    @Override
    public PageResult<PayAppDO> getAppPage(PayAppPageReqVO pageReqVO) {
        return appMapper.selectPage(pageReqVO);
    }

    @Override
    public PayAppDO validPayApp(Long appId) {
        PayAppDO app = appMapper.selectById(appId);
        // 校验支付应用数据是否存在以及可用
        return validatePayAppDO(app);
    }

    @Override
    public PayAppDO validPayApp(String appKey) {
        PayAppDO app = appMapper.selectByAppKey(appKey);
        // 校验支付应用数据是否存在以及可用
        return validatePayAppDO(app);
    }

    /**
     * 校验支付应用实体的有效性
     * 主要包括存在性检查和禁用状态检查
     *
     * @param app 待校验的支付应用实体
     * @return 校验通过的支付应用实体
     * @throws IllegalArgumentException 如果支付应用实体不存在或已被禁用
     */
    private PayAppDO validatePayAppDO(PayAppDO app) {
        // 校验是否存在
        if (app == null) {
            throw exception(ErrorCodeConstants.APP_NOT_FOUND);
        }
        // 校验是否禁用
        if (CommonStatusEnum.DISABLE.getStatus().equals(app.getStatus())) {
            throw exception(ErrorCodeConstants.APP_IS_DISABLE);
        }
        return app;
    }


    /**
     * 校验应用密钥是否重复
     * 在新增或更新支付应用时，确保应用密钥（appKey）的唯一性
     * 如果是在新增情况下，检查数据库中是否已存在相同的appKey
     * 如果是在更新情况下，检查数据库中是否存在除当前应用外的其他应用使用了相同的appKey
     *
     * @param payAppId  支付应用的ID，更新时使用，新增时可能为null
     * @param payAppKey 支付应用的密钥，用于校验是否重复
     * @throws RuntimeException 如果发现appKey重复，抛出运行时异常
     */
    private void validateAppKeyDuplicate(Long payAppId, String payAppKey) {
        // 新增时，校验appKey是否重复
        if (Objects.isNull(payAppId) && StrUtil.isNotBlank(payAppKey)) {
            if (appMapper.selectCount(PayAppDO::getAppKey, payAppKey) > 0) {
                throw exception(APP_KEY_EXISTS);
            }
            // 更新时，校验appKey是否重复
        } else if (Objects.nonNull(payAppId) && StrUtil.isNotBlank(payAppKey)) {
            LambdaQueryWrapperX<PayAppDO> queryWrapper = new LambdaQueryWrapperX<>();
            queryWrapper.eq(PayAppDO::getAppKey, payAppKey)
                    .ne(PayAppDO::getId, payAppId);
            if (appMapper.selectCount(queryWrapper) > 0) {
                throw exception(APP_KEY_EXISTS);
            }
        }
    }

}
