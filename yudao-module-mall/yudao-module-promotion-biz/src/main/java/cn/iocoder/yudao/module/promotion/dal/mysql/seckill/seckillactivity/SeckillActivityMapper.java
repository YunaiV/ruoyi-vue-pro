package cn.iocoder.yudao.module.promotion.dal.mysql.seckill.seckillactivity;

import cn.hutool.core.lang.Assert;
import cn.hutool.core.util.ObjectUtil;
import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.mybatis.core.mapper.BaseMapperX;
import cn.iocoder.yudao.framework.mybatis.core.query.LambdaQueryWrapperX;
import cn.iocoder.yudao.module.promotion.controller.admin.seckill.vo.activity.SeckillActivityPageReqVO;
import cn.iocoder.yudao.module.promotion.controller.app.seckill.vo.activity.AppSeckillActivityPageReqVO;
import cn.iocoder.yudao.module.promotion.dal.dataobject.seckill.SeckillActivityDO;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import org.apache.ibatis.annotations.Mapper;

import java.time.LocalDateTime;
import java.util.List;

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

    default PageResult<SeckillActivityDO> selectPage(AppSeckillActivityPageReqVO pageReqVO, Integer status, LocalDateTime dateTime) {
        return selectPage(pageReqVO, new LambdaQueryWrapperX<SeckillActivityDO>()
                .eqIfPresent(SeckillActivityDO::getStatus, status)
                .lt(SeckillActivityDO::getStartTime, dateTime)
                .gt(SeckillActivityDO::getEndTime, dateTime)// 开始时间 < 指定时间 < 结束时间，也就是说获取指定时间段的活动
                .apply(ObjectUtil.isNotNull(pageReqVO.getConfigId()), "FIND_IN_SET(" + pageReqVO.getConfigId() + ",config_ids) > 0"));
    }

    default SeckillActivityDO selectBySpuIdAndStatusAndNow(Long spuId, Integer status) {
        LocalDateTime now = LocalDateTime.now();
        return selectOne(new LambdaQueryWrapperX<SeckillActivityDO>()
                .eq(SeckillActivityDO::getSpuId, spuId)
                .eq(SeckillActivityDO::getStatus, status)
                .lt(SeckillActivityDO::getStartTime, now)
                .gt(SeckillActivityDO::getEndTime, now)); // 开始时间 < now < 结束时间，也就是说获取指定时间段的活动
    }

}
