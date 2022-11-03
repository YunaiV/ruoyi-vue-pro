package cn.iocoder.yudao.module.coupon.service.coupon;

import cn.hutool.core.bean.BeanUtil;
import cn.iocoder.yudao.framework.security.core.util.SecurityFrameworkUtils;
import cn.iocoder.yudao.module.coupon.dal.dataobject.CouponTemplete.CouponTempleteDO;
import cn.iocoder.yudao.module.coupon.dal.mysql.CouponTemplete.CouponTempleteMapper;
import org.springframework.stereotype.Service;
import javax.annotation.Resource;
import org.springframework.validation.annotation.Validated;

import java.util.*;
import cn.iocoder.yudao.module.coupon.controller.admin.coupon.vo.*;
import cn.iocoder.yudao.module.coupon.dal.dataobject.coupon.CouponDO;
import cn.iocoder.yudao.framework.common.pojo.PageResult;

import cn.iocoder.yudao.module.coupon.convert.coupon.CouponConvert;
import cn.iocoder.yudao.module.coupon.dal.mysql.coupon.CouponMapper;

import static cn.iocoder.yudao.framework.common.exception.util.ServiceExceptionUtil.exception;
import static cn.iocoder.yudao.module.CouponTemplete.enums.ErrorCodeConstants.COUPON_NOT_EXISTS;

/**
 * 优惠券 Service 实现类
 *
 * @author wxr
 */
@Service
@Validated
public class CouponServiceImpl implements CouponService {

    @Resource
    private CouponMapper couponMapper;

    @Resource
    private CouponTempleteMapper couponTempleteMapper;

    public Long create(CouponCreateReqVO createReqVO) {
        // 插入
        CouponDO couponDO = CouponConvert.INSTANCE.convert(createReqVO);
        couponMapper.insert(couponDO);
        // 返回
        return couponDO.getId();
    }


    /**
     *todo 获取用户id收获优惠券
     *@author:wxr
     *@date:2022-08-13 3:11
     *@Description
     */
    @Override
    public Long create(Long templateId) {
        Long userid = SecurityFrameworkUtils.getLoginUserId();
        CouponDO couponDO = CouponDO.builder().memberId(userid).build();
        CouponTempleteDO couponTempleteDO = couponTempleteMapper.selectById(templateId);
        //todo 缺少判空
        BeanUtil.copyProperties(couponTempleteDO,couponTempleteDO);
        couponMapper.insert(couponDO);
        return couponDO.getId();
    }

    @Override
    public void update(CouponUpdateReqVO updateReqVO) {
        // 校验存在
        this.validateExists(updateReqVO.getId());
        // 更新
        CouponDO updateObj = CouponConvert.INSTANCE.convert(updateReqVO);
        couponMapper.updateById(updateObj);
    }

    @Override
    public void delete(Long id) {
        // 校验存在
        this.validateExists(id);
        // 删除
        couponMapper.deleteById(id);
    }

    private void validateExists(Long id) {
        if (couponMapper.selectById(id) == null) {
            throw exception(COUPON_NOT_EXISTS);
        }
    }

    @Override
    public CouponDO get(Long id) {
        return couponMapper.selectById(id);
    }

    @Override
    public List<CouponDO> getList(Collection<Long> ids) {
        return couponMapper.selectBatchIds(ids);
    }

    @Override
    public PageResult<CouponDO> getPage(CouponPageReqVO pageReqVO) {
        return couponMapper.selectPage(pageReqVO);
    }

    @Override
    public List<CouponDO> getList(CouponExportReqVO exportReqVO) {
        return couponMapper.selectList(exportReqVO);
    }

}
