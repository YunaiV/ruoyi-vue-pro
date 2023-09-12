package cn.iocoder.yudao.module.promotion.service.seckill;

import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.module.promotion.api.seckill.dto.SeckillActivityUpdateStockReqDTO;
import cn.iocoder.yudao.module.promotion.controller.admin.seckill.vo.activity.SeckillActivityCreateReqVO;
import cn.iocoder.yudao.module.promotion.controller.admin.seckill.vo.activity.SeckillActivityPageReqVO;
import cn.iocoder.yudao.module.promotion.controller.admin.seckill.vo.activity.SeckillActivityUpdateReqVO;
import cn.iocoder.yudao.module.promotion.dal.dataobject.seckill.seckillactivity.SeckillActivityDO;
import cn.iocoder.yudao.module.promotion.dal.dataobject.seckill.seckillactivity.SeckillProductDO;

import javax.validation.Valid;
import java.util.Collection;
import java.util.List;

/**
 * 秒杀活动 Service 接口
 *
 * @author halfninety
 */
public interface SeckillActivityService {

    /**
     * 创建秒杀活动
     *
     * @param createReqVO 创建信息
     * @return 编号
     */
    Long createSeckillActivity(@Valid SeckillActivityCreateReqVO createReqVO);

    /**
     * 更新秒杀活动
     *
     * @param updateReqVO 更新信息
     */
    void updateSeckillActivity(@Valid SeckillActivityUpdateReqVO updateReqVO);

    /**
     * 更新秒杀活动
     *
     * @param activityDO 秒杀活动
     */
    void updateSeckillActivity(SeckillActivityDO activityDO);

    /**
     * 更新秒杀库存
     *
     * @param updateStockReqDTO 更新信息
     */
    void updateSeckillStock(SeckillActivityUpdateStockReqDTO updateStockReqDTO);
    /**
     * 更新秒杀活动商品
     *
     * @param productList 活动商品列表
     */
    void updateSeckillActivityProductList(List<SeckillProductDO> productList);

    /**
     * 关闭秒杀活动
     *
     * @param id 编号
     */
    void closeSeckillActivity(Long id);

    /**
     * 删除秒杀活动
     *
     * @param id 编号
     */
    void deleteSeckillActivity(Long id);

    /**
     * 获得秒杀活动
     *
     * @param id 编号
     * @return 秒杀活动
     */
    SeckillActivityDO getSeckillActivity(Long id);

    /**
     * 获得秒杀活动分页
     *
     * @param pageReqVO 分页查询
     * @return 秒杀活动分页
     */
    PageResult<SeckillActivityDO> getSeckillActivityPage(SeckillActivityPageReqVO pageReqVO);

    /**
     * 通过活动编号获取活动商品
     *
     * @param activityId 活动编号
     * @return 活动商品列表
     */
    List<SeckillProductDO> getSeckillProductListByActivityId(Long activityId);

    /**
     * 通过活动编号获取活动商品
     *
     * @param activityIds 活动编号
     * @return 活动商品列表
     */
    List<SeckillProductDO> getSeckillProductListByActivityId(Collection<Long> activityIds);

}
