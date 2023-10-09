package cn.iocoder.yudao.module.promotion.dal.mysql.combination;

import cn.iocoder.yudao.framework.common.pojo.PageParam;
import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.mybatis.core.mapper.BaseMapperX;
import cn.iocoder.yudao.framework.mybatis.core.query.LambdaQueryWrapperX;
import cn.iocoder.yudao.module.promotion.controller.admin.combination.vo.activity.CombinationActivityPageReqVO;
import cn.iocoder.yudao.module.promotion.dal.dataobject.combination.CombinationActivityDO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.Collection;
import java.util.List;

/**
 * 拼团活动 Mapper
 *
 * @author HUIHUI
 */
@Mapper
public interface CombinationActivityMapper extends BaseMapperX<CombinationActivityDO> {

    default PageResult<CombinationActivityDO> selectPage(CombinationActivityPageReqVO reqVO) {
        return selectPage(reqVO, new LambdaQueryWrapperX<CombinationActivityDO>()
                .likeIfPresent(CombinationActivityDO::getName, reqVO.getName())
                .eqIfPresent(CombinationActivityDO::getStatus, reqVO.getStatus())
                .orderByDesc(CombinationActivityDO::getId));
    }

    default List<CombinationActivityDO> selectListByStatus(Integer status) {
        return selectList(CombinationActivityDO::getStatus, status);
    }

    default PageResult<CombinationActivityDO> selectPage(PageParam pageParam, Integer status) {
        return selectPage(pageParam, new LambdaQueryWrapperX<CombinationActivityDO>()
                .eq(CombinationActivityDO::getStatus, status));
    }

    default List<CombinationActivityDO> selectListByStatus(Integer status, Integer count) {
        return selectList(new LambdaQueryWrapperX<CombinationActivityDO>()
                .eq(CombinationActivityDO::getStatus, status)
                .last("LIMIT " + count));
    }

    // TODO @puhui999：类似 BargainActivityMapper

    /**
     * 获取指定 spu 编号最近参加的活动，每个 spuId 只返回一条记录
     *
     * @param spuIds spu 编号
     * @param status 状态
     * @return 拼团活动列表
     */
    @Select("<script> " + "SELECT p1.* " +
            "FROM promotion_combination_activity p1 " +
            "INNER JOIN ( " +
            "  SELECT spu_id, MAX(DISTINCT create_time) AS max_create_time " +
            "  FROM promotion_combination_activity " +
            "  WHERE spu_id IN " +
            "<foreach collection='spuIds' item='spuId' open='(' separator=',' close=')'>" +
            "    #{spuId}" +
            "</foreach>" +
            "  GROUP BY spu_id " +
            ") p2 " +
            "ON p1.spu_id = p2.spu_id AND p1.create_time = p2.max_create_time AND p1.status = #{status} " +
            "ORDER BY p1.create_time DESC;" +
            " </script>")
    List<CombinationActivityDO> selectListBySpuIdsAndStatus(@Param("spuIds") Collection<Long> spuIds, @Param("status") Integer status);


}
