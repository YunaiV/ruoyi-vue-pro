package cn.iocoder.yudao.module.srm.service.purchase.payment.term;

import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.common.util.object.BeanUtils;
import cn.iocoder.yudao.module.srm.controller.admin.purchase.payment.term.vo.SrmPaymentTermPageReqVO;
import cn.iocoder.yudao.module.srm.controller.admin.purchase.payment.term.vo.SrmPaymentTermSaveReqVO;
import cn.iocoder.yudao.module.srm.dal.dataobject.purchase.payment.term.SrmPaymentTermDO;
import cn.iocoder.yudao.module.srm.dal.mysql.purchase.payment.term.SrmPaymentTermMapper;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Service;
import org.springframework.validation.annotation.Validated;

import java.util.List;

import static cn.iocoder.yudao.framework.common.exception.util.ServiceExceptionUtil.exception;
import static cn.iocoder.yudao.module.srm.enums.SrmErrorCodeConstants.PAYMENT_TERM_NOT_EXISTS;

/**
 * 付款条款 Service 实现类
 *
 * @author wdy
 */
@Service
@Validated
public class SrmPaymentTermServiceImpl implements SrmPaymentTermService {

    @Resource
    private SrmPaymentTermMapper paymentTermMapper;

    @Override
    public Long createPaymentTerm(SrmPaymentTermSaveReqVO createReqVO) {
        // 插入
        SrmPaymentTermDO paymentTerm = BeanUtils.toBean(createReqVO, SrmPaymentTermDO.class);
        paymentTermMapper.insert(paymentTerm);
        // 返回
        return paymentTerm.getId();
    }

    @Override
    public void updatePaymentTerm(SrmPaymentTermSaveReqVO updateReqVO) {
        // 校验存在
        validatePaymentTermExists(updateReqVO.getId());
        // 更新
        SrmPaymentTermDO updateObj = BeanUtils.toBean(updateReqVO, SrmPaymentTermDO.class);
        paymentTermMapper.updateById(updateObj);
    }

    @Override
    public void deletePaymentTerm(Long id) {
        // 校验存在
        validatePaymentTermExists(id);
        // 删除
        paymentTermMapper.deleteById(id);
    }

    private void validatePaymentTermExists(Long id) {
        if (paymentTermMapper.selectById(id) == null) {
            throw exception(PAYMENT_TERM_NOT_EXISTS);
        }
    }

    @Override
    public SrmPaymentTermDO getPaymentTerm(Long id) {
        return paymentTermMapper.selectById(id);
    }

    @Override
    public PageResult<SrmPaymentTermDO> getPaymentTermPage(SrmPaymentTermPageReqVO pageReqVO) {
        return paymentTermMapper.selectPage(pageReqVO);
    }

    @Override
    public List<SrmPaymentTermDO> getPaymentTermList() {
        return paymentTermMapper.selectList();
    }
}