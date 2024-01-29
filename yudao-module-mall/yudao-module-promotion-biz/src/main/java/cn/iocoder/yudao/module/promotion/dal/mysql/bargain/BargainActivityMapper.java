package cn.iocoder.yudao.module.promotion.dal.mysql.bargain;

import cn.iocoder.yudao.framework.common.pojo.PageParam;
import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.mybatis.core.mapper.BaseMapperX;
import cn.iocoder.yudao.framework.mybatis.core.query.LambdaQueryWrapperX;
import cn.iocoder.yudao.module.promotion.controller.admin.bargain.vo.activity.BargainActivityPageReqVO;
import cn.iocoder.yudao.module.promotion.dal.dataobject.bargain.BargainActivityDO;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import org.apache.ibatis.annotations.Mapper;

import java.time.LocalDateTime;
import java.util.Collection;
import java.util.List;
import java.util.Map;

/**
 * 砍价活动 Mapper
 *
 * @author HUIHUI
 */
@Mapper
public interface BargainActivityMapper extends BaseMapperX<BargainActivityDO> {

    default PageResult<BargainActivityDO> selectPage(BargainActivityPageReqVO reqVO) {
        return selectPage(reqVO, new LambdaQueryWrapperX<BargainActivityDO>()
                .likeIfPresent(BargainActivityDO::getName, reqVO.getName())
                .eqIfPresent(BargainActivityDO::getStatus, reqVO.getStatus())
                .orderByDesc(BargainActivityDO::getId));
    }

    default List<BargainActivityDO> selectListByStatus(Integer status) {
        return selectList(BargainActivityDO::getStatus, status);
    }

    /**
     * 更新活动库存
     *
     * @param id    活动编号
     * @param count 扣减的库存数量
     * @return 影响的行数
     */
    default int updateStock(Long id, int count) {
        // 情况一：增加库存
        if (count > 0) {
            return update(null, new LambdaUpdateWrapper<BargainActivityDO>()
                    .eq(BargainActivityDO::getId, id)
                    .setSql("stock = stock + " + count));
        }
        // 情况二：扣减库存
        count = -count; // 取正
        return update(null, new LambdaUpdateWrapper<BargainActivityDO>()
                .eq(BargainActivityDO::getId, id)
                .ge(BargainActivityDO::getStock, count)
                .setSql("stock = stock - " + count));
    }

    /**
     * 查询处在 now 日期时间且是 status 状态的活动分页
     *
     * @param pageReqVO 分页参数
     * @param status    状态
     * @param now       当前日期时间
     * @return 活动分页
     */
    default PageResult<BargainActivityDO> selectPage(PageParam pageReqVO, Integer status, LocalDateTime now) {
        return selectPage(pageReqVO, new LambdaQueryWrapperX<BargainActivityDO>()
                .eq(BargainActivityDO::getStatus, status)
                .le(BargainActivityDO::getStartTime, now)
                .ge(BargainActivityDO::getEndTime, now));
    }

    /**
     * 查询处在 now 日期时间且是 status 状态的活动分页
     *
     * @param status 状态
     * @param now    当前日期时间
     * @return 活动分页
     */
    default List<BargainActivityDO> selectList(Integer count, Integer status, LocalDateTime now) {
        return selectList(new LambdaQueryWrapperX<BargainActivityDO>()
                .eq(BargainActivityDO::getStatus, status)
                .le(BargainActivityDO::getStartTime, now)
                .ge(BargainActivityDO::getEndTime, now)
                .last("LIMIT " + count));
    }

    /**
     * 查询出指定 spuId 的 spu 参加的活动最接近现在的一条记录。多个的话，一个 spuId 对应一个最近的活动编号
     *
     * @param spuIds spu 编号
     * @param status 状态
     * @return 包含 spuId 和 activityId 的 map 对象列表
     */
    default List<Map<String, Object>> selectSpuIdAndActivityIdMapsBySpuIdsAndStatus(Collection<Long> spuIds, Integer status) {
        return selectMaps(new QueryWrapper<BargainActivityDO>()
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
    default List<BargainActivityDO> selectListByIdsAndDateTimeLt(Collection<Long> ids, LocalDateTime dateTime) {
        return selectList(new LambdaQueryWrapperX<BargainActivityDO>()
                .in(BargainActivityDO::getId, ids)
                .lt(BargainActivityDO::getStartTime, dateTime)
                .gt(BargainActivityDO::getEndTime, dateTime)// 开始时间 < 指定时间 < 结束时间，也就是说获取指定时间段的活动
                .orderByDesc(BargainActivityDO::getCreateTime));
    }

}
