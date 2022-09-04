package cn.iocoder.yudao.module.coupon.service.CouponTemplete;

import java.util.*;
import javax.validation.*;
import cn.iocoder.yudao.module.coupon.controller.admin.templete.vo.*;
import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.module.coupon.dal.dataobject.CouponTemplete.CouponTempleteDO;

/**
 * 优惠券模板 Service 接口
 *
 * @author wxr
 */
public interface CouponTempleteService {

    /**
     * 创建优惠券模板
     *
     * @param createReqVO 创建信息
     * @return 编号
     */
    Long create(@Valid CouponTempleteCreateReqVO createReqVO);

    /**
     * 更新优惠券模板
     *
     * @param updateReqVO 更新信息
     */
    void update(@Valid CouponTempleteUpdateReqVO updateReqVO);

    /**
     * 删除优惠券模板
     *
     * @param id 编号
     */
    void delete(Long id);

    /**
     * 获得优惠券模板
     *
     * @param id 编号
     * @return 优惠券模板
     */
    CouponTempleteDO get(Long id);

    /**
     * 获得优惠券模板列表
     *
     * @param ids 编号
     * @return 优惠券模板列表
     */
    List<CouponTempleteDO> getList(Collection<Long> ids);

    /**
     * 获得优惠券模板分页
     *
     * @param pageReqVO 分页查询
     * @return 优惠券模板分页
     */
    PageResult<CouponTempleteDO> getPage(CouponTempletePageReqVO pageReqVO);

    /**
     * 获得优惠券模板列表, 用于 Excel 导出
     *
     * @param exportReqVO 查询条件
     * @return 优惠券模板列表
     */
    List<CouponTempleteDO> getList(CouponTempleteExportReqVO exportReqVO);

}
