package cn.iocoder.yudao.module.oa.dal.mysql.travel;

import cn.hutool.core.map.MapUtil;
import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.mybatis.core.mapper.BaseMapperX;
import cn.iocoder.yudao.framework.mybatis.core.query.LambdaQueryWrapperX;
import cn.iocoder.yudao.framework.mybatis.core.query.MPJLambdaWrapperX;
import cn.iocoder.yudao.module.oa.controller.admin.travel.vo.apply.OaTravelApplyPageReqVO;
import cn.iocoder.yudao.module.oa.dal.dataobject.travel.OaTravelApplyDO;
import org.apache.ibatis.annotations.Mapper;

import java.time.LocalDateTime;
import java.util.Collection;
import java.util.List;
import java.util.Map;

import static cn.iocoder.yudao.framework.common.util.collection.CollectionUtils.convertList;
import static cn.iocoder.yudao.framework.common.util.collection.CollectionUtils.convertMap;

/**
 * OA 出差申请 Mapper
 *
 * @author 芋道源码
 */
@Mapper
public interface OaTravelApplyMapper extends BaseMapperX<OaTravelApplyDO> {

    default Map<Long, Integer> selectDaysMapByCreatorsAndStatusAndStartTime(
            Collection<Long> userIds, Integer status, LocalDateTime[] startTime) {
        List<Map<String, Object>> days = selectMaps(new MPJLambdaWrapperX<OaTravelApplyDO>()
                .selectAs(OaTravelApplyDO::getCreator, "userId")
                .selectSum(OaTravelApplyDO::getDays, "days")
                .in(OaTravelApplyDO::getCreator, convertList(userIds, String::valueOf))
                .eq(OaTravelApplyDO::getStatus, status)
                .between(OaTravelApplyDO::getStartTime, startTime[0], startTime[1])
                .groupBy(OaTravelApplyDO::getCreator));
        return convertMap(days, row -> MapUtil.getLong(row, "userId"), row -> MapUtil.getInt(row, "days"));
    }

    default PageResult<OaTravelApplyDO> selectPage(Long userId, OaTravelApplyPageReqVO reqVO) {
        return selectPage(reqVO, new LambdaQueryWrapperX<OaTravelApplyDO>()
                .eq(OaTravelApplyDO::getCreator, userId.toString())
                .likeIfPresent(OaTravelApplyDO::getNo, reqVO.getNo())
                .eqIfPresent(OaTravelApplyDO::getStatus, reqVO.getStatus())
                .eqIfPresent(OaTravelApplyDO::getDeptId, reqVO.getDeptId())
                .eqIfPresent(OaTravelApplyDO::getReimburseStatus, reqVO.getReimburseStatus())
                .betweenIfPresent(OaTravelApplyDO::getCreateTime, reqVO.getCreateTime())
                .orderByDesc(OaTravelApplyDO::getId));
    }

    default OaTravelApplyDO selectByNo(String no) {
        return selectOne(OaTravelApplyDO::getNo, no);
    }

    default List<OaTravelApplyDO> selectListByCreatorAndStatus(Long userId, Integer status) {
        return selectList(new LambdaQueryWrapperX<OaTravelApplyDO>()
                .eq(OaTravelApplyDO::getCreator, userId.toString()).eq(OaTravelApplyDO::getStatus, status)
                .orderByDesc(OaTravelApplyDO::getId));
    }

}
