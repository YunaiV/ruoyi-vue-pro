package cn.iocoder.yudao.module.coupon.service.CouponTemplete;

import cn.iocoder.yudao.module.CouponTemplete.enums.CouponTypeEnum;
import cn.iocoder.yudao.module.CouponTemplete.enums.CouponValidityTypeEnum;
import cn.iocoder.yudao.module.coupon.controller.admin.templete.vo.CouponTempleteCreateReqVO;
import cn.iocoder.yudao.module.coupon.controller.admin.templete.vo.CouponTempleteExportReqVO;
import cn.iocoder.yudao.module.coupon.controller.admin.templete.vo.CouponTempletePageReqVO;
import cn.iocoder.yudao.module.coupon.controller.admin.templete.vo.CouponTempleteUpdateReqVO;
import cn.iocoder.yudao.module.coupon.convert.CouponTemplete.CouponTempleteConvert;
import cn.iocoder.yudao.module.coupon.dal.dataobject.CouponTemplete.CouponTempleteDO;
import cn.iocoder.yudao.module.coupon.dal.mysql.CouponTemplete.CouponTempleteMapper;
import org.springframework.stereotype.Service;
import javax.annotation.Resource;
import org.springframework.validation.annotation.Validated;

import java.util.*;
import cn.iocoder.yudao.framework.common.pojo.PageResult;



import static cn.iocoder.yudao.framework.common.exception.util.ServiceExceptionUtil.exception;
import static cn.iocoder.yudao.module.CouponTemplete.enums.ErrorCodeConstants.*;

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
        /* 验证类型、判断必填*/
        checkCouponType(createReqVO);

        /*验证过期类型、判断必填*/
        checkValidityType(createReqVO);



        couponTempleteMapper.insert(couponTempleteDO);
        // 返回
        return couponTempleteDO.getId();
    }

    /*确认优惠券类型*/
    private void checkValidityType(CouponTempleteCreateReqVO createReqVO) {
        Integer validtyType = createReqVO.getValidityType();

        if(CouponValidityTypeEnum.TIME_RANGE_EXPIRTED.getStatus().equals(validtyType)){
            if(createReqVO.getStartUseTime() == null||createReqVO.getEndUseTime() == null){
                throw exception(START_END_TIME_NOT_NULL);
            }
        }else{
            if(createReqVO.getFixedTerm() == null){
                throw exception(FIXED_TERM_NOT_NULL);
            }
        }
    }

    private void checkCouponType(CouponTempleteCreateReqVO createReqVO) {

        String couponType = createReqVO.getType();
        //当type=reward时候，需要添加
        if(couponType.equals(CouponTypeEnum.REWARD.getName())){
            if(createReqVO.getMoney()==null){
                throw exception(MONEY_NOT_NULL);
            }
        }else if(couponType.equals(CouponTypeEnum.DISCOUNT.getName())){
            if(createReqVO.getDiscount()==null){
                throw exception(DISCOUNT_NOT_NULL);
            }
            if(createReqVO.getDiscountLimit()==null){
                throw exception(DISCOUNT_LIMIT_NOT_NULL);
            }
        }else if (couponType.equals(CouponTypeEnum.RANDOW.getName())){
            //当type为radom时需要添加不能为空
            if(createReqVO.getMinMoney()==null||createReqVO.getMaxMoney()==null){
                throw exception(MIN_MAX_NOT_NULL);
            }
        }
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
