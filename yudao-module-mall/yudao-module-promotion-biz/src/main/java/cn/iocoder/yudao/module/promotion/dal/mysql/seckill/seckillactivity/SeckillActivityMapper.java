package cn.iocoder.yudao.module.promotion.dal.mysql.seckill.seckillactivity;

import cn.hutool.core.lang.Assert;
import cn.hutool.core.util.ObjectUtil;
import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.mybatis.core.mapper.BaseMapperX;
import cn.iocoder.yudao.framework.mybatis.core.query.LambdaQueryWrapperX;
import cn.iocoder.yudao.module.promotion.controller.admin.seckill.vo.activity.SeckillActivityPageReqVO;
import cn.iocoder.yudao.module.promotion.controller.app.seckill.vo.activity.AppSeckillActivityPageReqVO;
import cn.iocoder.yudao.module.promotion.dal.dataobject.seckill.SeckillActivityDO;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.time.LocalDateTime;
import java.util.Collection;
import java.util.List;
import java.util.Map;

/**
 * 秒杀活动 Mapper
 *
 * @author halfninety
 */
@Mapper
public interface SeckillActivityMapper extends BaseMapperX<SeckillActivityDO> {

    default PageResult<SeckillActivityDO> selectPage(SeckillActivityPageReqVO reqVO) {
        return selectPage(reqVO, new LambdaQueryWrapperX<SeckillActivityDO>()
                .likeIfPresent(SeckillActivityDO::getName, reqVO.getName())
                .eqIfPresent(SeckillActivityDO::getStatus, reqVO.getStatus())
                .betweenIfPresent(SeckillActivityDO::getCreateTime, reqVO.getCreateTime())
                .apply(ObjectUtil.isNotNull(reqVO.getConfigId()), "FIND_IN_SET(" + reqVO.getConfigId() + ", config_ids) > 0")
                .orderByDesc(SeckillActivityDO::getId));
    }

    default List<SeckillActivityDO> selectListByStatus(Integer status) {
        return selectList(new LambdaQueryWrapperX<SeckillActivityDO>()
                .eqIfPresent(SeckillActivityDO::getStatus, status));
    }

    /**
     * 更新活动库存(减少)
     *
     * @param id    活动编号
     * @param count 扣减的库存数量(正数)
     * @return 影响的行数
     */
    default int updateStockDecr(Long id, int count) {
        Assert.isTrue(count > 0);
        return update(null, new LambdaUpdateWrapper<SeckillActivityDO>()
                .eq(SeckillActivityDO::getId, id)
                .ge(SeckillActivityDO::getStock, count)
                .setSql("stock = stock - " + count));
    }

    /**
     * 更新活动库存（增加）
     *
     * @param id    活动编号
     * @param count 增加的库存数量(正数)
     * @return 影响的行数
     */
    default int updateStockIncr(Long id, int count) {
        Assert.isTrue(count > 0);
        return update(null, new LambdaUpdateWrapper<SeckillActivityDO>()
                .eq(SeckillActivityDO::getId, id)
                .setSql("stock = stock + " + count));
    }

    default PageResult<SeckillActivityDO> selectPage(AppSeckillActivityPageReqVO pageReqVO, Integer status) {
        return selectPage(pageReqVO, new LambdaQueryWrapperX<SeckillActivityDO>()
                .eqIfPresent(SeckillActivityDO::getStatus, status)
                .apply(ObjectUtil.isNotNull(pageReqVO.getConfigId()), "FIND_IN_SET(" + pageReqVO.getConfigId() + ",config_ids) > 0"));
    }

    /**
     * 查询出指定 spuId 的 spu 参加的活动最接近现在的一条记录。多个的话，一个 spuId 对应一个最近的活动编号
     *
     * @param spuIds spu 编号
     * @param status 状态
     * @return 包含 spuId 和 activityId 的 map 对象列表
     */
    default List<Map<String, Object>> selectSpuIdAndActivityIdMapsBySpuIdsAndStatus(@Param("spuIds") Collection<Long> spuIds, @Param("status") Integer status) {
        return selectMaps(new QueryWrapper<SeckillActivityDO>()
                .select("spu_id AS spuId, MAX(DISTINCT(id)) AS activityId") // 时间越大 id 也越大 直接用 id
                .in("spu_id", spuIds)
                .eq("status", status)
                .groupBy("spu_id"));
    }

    /**
     * 获取指定活动编号的活动列表且
     * 开始时间和结束时间小于给定时间 dateTime 的活动列表
     *
     * @param ids      活动编号
     * @param dateTime 指定日期
     * @return 活动列表
     */
    default List<SeckillActivityDO> selectListByIdsAndDateTimeLt(Collection<Long> ids, LocalDateTime dateTime) {
        return selectList(new LambdaQueryWrapperX<SeckillActivityDO>()
                .in(SeckillActivityDO::getId, ids)
                .lt(SeckillActivityDO::getStartTime, dateTime)
                .gt(SeckillActivityDO::getEndTime, dateTime)// 开始时间 < 指定时间 < 结束时间，也就是说获取指定时间段的活动
                .orderByDesc(SeckillActivityDO::getCreateTime));
    }

}
