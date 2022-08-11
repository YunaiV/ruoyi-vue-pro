package cn.iocoder.yudao.module.coupon.service.CouponTemplete;

import cn.iocoder.yudao.module.coupon.controller.admin.coupontemplete.vo.CouponTempleteCreateReqVO;
import cn.iocoder.yudao.module.coupon.controller.admin.coupontemplete.vo.CouponTempleteExportReqVO;
import cn.iocoder.yudao.module.coupon.controller.admin.coupontemplete.vo.CouponTempletePageReqVO;
import cn.iocoder.yudao.module.coupon.controller.admin.coupontemplete.vo.CouponTempleteUpdateReqVO;
import cn.iocoder.yudao.module.coupon.convert.CouponTemplete.CouponTempleteConvert;
import cn.iocoder.yudao.module.coupon.dal.dataobject.CouponTemplete.CouponTempleteDO;
import cn.iocoder.yudao.module.coupon.dal.mysql.CouponTemplete.CouponTempleteMapper;
import cn.iocoder.yudao.module.coupon.service.CouponTemplete.CouponTempleteService;
import org.springframework.stereotype.Service;
import javax.annotation.Resource;
import org.springframework.validation.annotation.Validated;

import java.util.*;
import cn.iocoder.yudao.framework.common.pojo.PageResult;



import static cn.iocoder.yudao.framework.common.exception.util.ServiceExceptionUtil.exception;
import static cn.iocoder.yudao.module.CouponTemplete.enums.ErrorCodeConstants.COUPON_TEMPLETE_NOT_EXISTS;

/**
 * 优惠券模板 Service 实现类
 *
 * @author wxr
 */
@Service
@Validated
public class CouponTempleteServiceImpl implements CouponTempleteService {

    @Resource
    private CouponTempleteMapper couponTempleteMapper;

    @Override
    public Long create(CouponTempleteCreateReqVO createReqVO) {
        // 插入
        CouponTempleteDO couponTempleteDO = CouponTempleteConvert.INSTANCE.convert(createReqVO);
        couponTempleteMapper.insert(couponTempleteDO);
        // 返回
        return couponTempleteDO.getId();
    }

    @Override
    public void update(CouponTempleteUpdateReqVO updateReqVO) {
        // 校验存在
        this.validateExists(updateReqVO.getId());
        // 更新
        CouponTempleteDO updateObj = CouponTempleteConvert.INSTANCE.convert(updateReqVO);
        couponTempleteMapper.updateById(updateObj);
    }

    @Override
    public void delete(Long id) {
        // 校验存在
        this.validateExists(id);
        // 删除
        couponTempleteMapper.deleteById(id);
    }

    private void validateExists(Long id) {
        if (couponTempleteMapper.selectById(id) == null) {
            throw exception(COUPON_TEMPLETE_NOT_EXISTS);
        }
    }

    @Override
    public CouponTempleteDO get(Long id) {
        return couponTempleteMapper.selectById(id);
    }

    @Override
    public List<CouponTempleteDO> getList(Collection<Long> ids) {
        return couponTempleteMapper.selectBatchIds(ids);
    }

    @Override
    public PageResult<CouponTempleteDO> getPage(CouponTempletePageReqVO pageReqVO) {
        return couponTempleteMapper.selectPage(pageReqVO);
    }

    @Override
    public List<CouponTempleteDO> getList(CouponTempleteExportReqVO exportReqVO) {
        return couponTempleteMapper.selectList(exportReqVO);
    }

}
