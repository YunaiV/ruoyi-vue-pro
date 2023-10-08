package cn.iocoder.yudao.module.promotion.dal.mysql.bargain;

import cn.iocoder.yudao.framework.common.pojo.PageParam;
import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.mybatis.core.mapper.BaseMapperX;
import cn.iocoder.yudao.framework.mybatis.core.query.LambdaQueryWrapperX;
import cn.iocoder.yudao.module.promotion.controller.admin.bargain.vo.activity.BargainActivityPageReqVO;
import cn.iocoder.yudao.module.promotion.dal.dataobject.bargain.BargainActivityDO;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

import java.time.LocalDateTime;
import java.util.Collection;
import java.util.List;

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

    // TODO @puhui999：一个商品，在统一时间，不会参与多个活动；so 是不是不用 inner join 哈？
    // PS：如果可以参与多个，其实可以这样写 select * from promotion_bargain_activity group by spu_id ORDER BY create_time DESC；通过 group 来过滤
    /**
     * 获取指定 spu 编号最近参加的活动，每个 spuId 只返回一条记录
     *
     * @param spuIds spu 编号
     * @param status 状态
     * @return 砍价活动列表
     */
    @Select("SELECT p1.* " +
            "FROM promotion_bargain_activity p1 " +
            "INNER JOIN ( " +
            "  SELECT spu_id, MAX(DISTINCT(create_time)) AS max_create_time " +
            "  FROM promotion_bargain_activity " +
            "  WHERE spu_id IN #{spuIds} " +
            "  GROUP BY spu_id " +
            ") p2 " +
            "ON p1.spu_id = p2.spu_id AND p1.create_time = p2.max_create_time AND p1.status = #{status} " +
            "ORDER BY p1.create_time DESC;")
    List<BargainActivityDO> selectListBySpuIds(Collection<Long> spuIds, Integer status);

}
