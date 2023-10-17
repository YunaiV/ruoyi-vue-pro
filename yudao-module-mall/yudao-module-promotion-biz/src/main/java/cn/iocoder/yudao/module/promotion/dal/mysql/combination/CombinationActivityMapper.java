package cn.iocoder.yudao.module.promotion.dal.mysql.combination;

import cn.iocoder.yudao.framework.common.pojo.PageParam;
import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.mybatis.core.mapper.BaseMapperX;
import cn.iocoder.yudao.framework.mybatis.core.query.LambdaQueryWrapperX;
import cn.iocoder.yudao.module.promotion.controller.admin.combination.vo.activity.CombinationActivityPageReqVO;
import cn.iocoder.yudao.module.promotion.dal.dataobject.combination.CombinationActivityDO;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.Collection;
import java.util.List;
import java.util.Map;

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

    /**
     * 查询出指定 spuId 的 spu 参加的活动最接近现在的一条记录。多个的话，一个 spuId 对应一个最近的活动编号
     * @param spuIds spu 编号
     * @param status 状态
     * @return 包含 spuId 和 activityId 的 map 对象列表
     */
    default List<Map<String, Object>> selectSpuIdAndActivityIdMapsBySpuIdsAndStatus(@Param("spuIds") Collection<Long> spuIds, @Param("status") Integer status) {
        return selectMaps(new QueryWrapper<CombinationActivityDO>()
                .select("spu_id AS spuId, MAX(DISTINCT(id)) AS activityId") // 时间越大 id 也越大 直接用 id
                .in("spu_id", spuIds)
                .eq("status", status)
                .groupBy("spu_id"));
    }

    default List<CombinationActivityDO> selectListByIds(Collection<Long> ids) {
        return selectList(new LambdaQueryWrapperX<CombinationActivityDO>()
                .in(CombinationActivityDO::getId, ids)
                .orderByDesc(CombinationActivityDO::getCreateTime));
    }

}
