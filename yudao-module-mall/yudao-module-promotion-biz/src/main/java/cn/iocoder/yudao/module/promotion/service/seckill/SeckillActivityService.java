package cn.iocoder.yudao.module.promotion.service.seckill;

import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.module.promotion.api.seckill.dto.SeckillValidateJoinRespDTO;
import cn.iocoder.yudao.module.promotion.controller.admin.seckill.vo.activity.SeckillActivityCreateReqVO;
import cn.iocoder.yudao.module.promotion.controller.admin.seckill.vo.activity.SeckillActivityPageReqVO;
import cn.iocoder.yudao.module.promotion.controller.admin.seckill.vo.activity.SeckillActivityUpdateReqVO;
import cn.iocoder.yudao.module.promotion.controller.app.seckill.vo.activity.AppSeckillActivityPageReqVO;
import cn.iocoder.yudao.module.promotion.dal.dataobject.seckill.SeckillActivityDO;
import cn.iocoder.yudao.module.promotion.dal.dataobject.seckill.SeckillProductDO;
import jakarta.validation.Valid;

import java.time.LocalDateTime;
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
     * 更新秒杀库存（减少）
     *
     * @param id    活动编号
     * @param skuId sku 编号
     * @param count 数量（正数）
     */
    void updateSeckillStockDecr(Long id, Long skuId, Integer count);

    /**
     * 更新秒杀库存（增加）
     *
     * @param id    活动编号
     * @param skuId sku 编号
     * @param count 数量（正数）
     */
    void updateSeckillStockIncr(Long id, Long skuId, Integer count);

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
    List<SeckillProductDO> getSeckillProductListByActivityIds(Collection<Long> activityIds);

    /**
     * 通过活动时段编号获取指定 status 的秒杀活动
     *
     * @param configId 时段配置编号
     * @param status   状态
     * @return 秒杀活动列表
     */
    List<SeckillActivityDO> getSeckillActivityListByConfigIdAndStatus(Long configId, Integer status);

    /**
     * 通过活动时段获取秒杀活动
     *
     * @param pageReqVO 请求
     * @return 秒杀活动列表
     */
    PageResult<SeckillActivityDO> getSeckillActivityAppPageByConfigId(AppSeckillActivityPageReqVO pageReqVO);

    /**
     * 校验是否参与秒杀商品
     *
     * 如果校验失败，则抛出业务异常
     *
     * @param activityId 活动编号
     * @param skuId      SKU 编号
     * @param count      数量
     * @return 秒杀信息
     */
    SeckillValidateJoinRespDTO validateJoinSeckill(Long activityId, Long skuId, Integer count);

    /**
     * 获取指定 spu 编号最近参加的活动，每个 spuId 只返回一条记录
     *
     * @param spuIds   spu 编号
     * @param status   状态
     * @param dateTime 日期时间
     * @return 秒杀活动列表
     */
    List<SeckillActivityDO> getSeckillActivityBySpuIdsAndStatusAndDateTimeLt(Collection<Long> spuIds, Integer status, LocalDateTime dateTime);

    /**
     * 获得拼团活动列表
     *
     * @param ids 拼团活动编号数组
     * @return 拼团活动的列表
     */
    List<SeckillActivityDO> getSeckillActivityListByIds(Collection<Long> ids);

}
