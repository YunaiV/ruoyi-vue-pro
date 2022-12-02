package cn.iocoder.yudao.module.promotion.service.seckill.seckillactivity;

import java.util.*;
import javax.validation.*;

import cn.iocoder.yudao.module.promotion.controller.admin.seckill.vo.activity.SeckillActivityCreateReqVO;
import cn.iocoder.yudao.module.promotion.controller.admin.seckill.vo.activity.SeckillActivityPageReqVO;
import cn.iocoder.yudao.module.promotion.controller.admin.seckill.vo.activity.SeckillActivityUpdateReqVO;
import cn.iocoder.yudao.module.promotion.dal.dataobject.seckill.seckillactivity.SeckillActivityDO;
import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.module.promotion.dal.dataobject.seckill.seckillactivity.SeckillProductDO;

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
     * 获得秒杀活动列表
     *
     * @param ids 编号
     * @return 秒杀活动列表
     */
    List<SeckillActivityDO> getSeckillActivityList(Collection<Long> ids);

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
     * @param id 活动编号
     * @return 活动商品列表
     */
    List<SeckillProductDO> getSeckillProductListByActivityId(Long id);
}
