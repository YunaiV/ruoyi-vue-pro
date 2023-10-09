package cn.iocoder.yudao.module.promotion.dal.mysql.seckill.seckillactivity;

import cn.hutool.core.util.ObjectUtil;
import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.mybatis.core.mapper.BaseMapperX;
import cn.iocoder.yudao.framework.mybatis.core.query.LambdaQueryWrapperX;
import cn.iocoder.yudao.module.promotion.controller.admin.seckill.vo.activity.SeckillActivityPageReqVO;
import cn.iocoder.yudao.module.promotion.controller.app.seckill.vo.activity.AppSeckillActivityPageReqVO;
import cn.iocoder.yudao.module.promotion.dal.dataobject.seckill.SeckillActivityDO;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.Collection;
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
     * 更新活动库存
     *
     * @param id    活动编号
     * @param count 扣减的库存数量
     * @return 影响的行数
     */
    default int updateStock(Long id, int count) {
        return update(null, new LambdaUpdateWrapper<SeckillActivityDO>()
                .eq(SeckillActivityDO::getId, id)
                .gt(SeckillActivityDO::getTotalStock, 0)
                .setSql("stock = stock + " + count)
                .setSql("total_stock = total_stock - " + count));
    }

    default PageResult<SeckillActivityDO> selectPage(AppSeckillActivityPageReqVO pageReqVO, Integer status) {
        return selectPage(pageReqVO, new LambdaQueryWrapperX<SeckillActivityDO>()
                .eqIfPresent(SeckillActivityDO::getStatus, status)
                // TODO 芋艿：对 find in set 的想法；
                .apply(ObjectUtil.isNotNull(pageReqVO.getConfigId()), "FIND_IN_SET(" + pageReqVO.getConfigId() + ",config_ids) > 0"));
    }

    // TODO @puhui999：类似 BargainActivityMapper

    /**
     * 获取指定 spu 编号最近参加的活动，每个 spuId 只返回一条记录
     *
     * @param spuIds spu 编号
     * @param status 状态
     * @return 秒杀活动列表
     */
    @Select("<script> " + "SELECT p1.* " +
            "FROM promotion_seckill_activity p1 " +
            "INNER JOIN ( " +
            "  SELECT spu_id, MAX(DISTINCT create_time) AS max_create_time " +
            "  FROM promotion_seckill_activity " +
            "  WHERE spu_id IN " +
            "<foreach collection='spuIds' item='spuId' open='(' separator=',' close=')'>" +
            "    #{spuId}" +
            "</foreach>" +
            "  GROUP BY spu_id " +
            ") p2 " +
            "ON p1.spu_id = p2.spu_id AND p1.create_time = p2.max_create_time AND p1.status = #{status} " +
            "ORDER BY p1.create_time DESC;" +
            " </script>")
    List<SeckillActivityDO> selectListBySpuIdsAndStatus(@Param("spuIds") Collection<Long> spuIds, @Param("status") Integer status);

}
