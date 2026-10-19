package cn.iocoder.yudao.module.oa.dal.mysql.leave;

import cn.hutool.core.map.MapUtil;
import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.mybatis.core.mapper.BaseMapperX;
import cn.iocoder.yudao.framework.mybatis.core.query.LambdaQueryWrapperX;
import cn.iocoder.yudao.framework.mybatis.core.query.MPJLambdaWrapperX;
import cn.iocoder.yudao.module.oa.controller.admin.leave.vo.OaLeaveApplyPageReqVO;
import cn.iocoder.yudao.module.oa.dal.dataobject.leave.OaLeaveApplyDO;
import org.apache.ibatis.annotations.Mapper;

import java.time.LocalDateTime;
import java.util.Collection;
import java.util.List;
import java.util.Map;

import static cn.iocoder.yudao.framework.common.util.collection.CollectionUtils.convertList;
import static cn.iocoder.yudao.framework.common.util.collection.CollectionUtils.convertMap;

/**
 * OA 请假申请 Mapper
 *
 * @author 芋道源码
 */
@Mapper
public interface OaLeaveApplyMapper extends BaseMapperX<OaLeaveApplyDO> {

    default Map<Long, Integer> selectDaysMapByCreatorsAndStatusAndStartTime(
            Collection<Long> userIds, Integer status, LocalDateTime[] startTime) {
        List<Map<String, Object>> days = selectMaps(new MPJLambdaWrapperX<OaLeaveApplyDO>()
                .selectAs(OaLeaveApplyDO::getCreator, "userId")
                .selectSum(OaLeaveApplyDO::getDays, "days")
                .in(OaLeaveApplyDO::getCreator, convertList(userIds, String::valueOf))
                .eq(OaLeaveApplyDO::getStatus, status)
                .between(OaLeaveApplyDO::getStartTime, startTime[0], startTime[1])
                .groupBy(OaLeaveApplyDO::getCreator));
        return convertMap(days, row -> MapUtil.getLong(row, "userId"), row -> MapUtil.getInt(row, "days"));
    }

    default PageResult<OaLeaveApplyDO> selectPage(Long userId, OaLeaveApplyPageReqVO reqVO) {
        return selectPage(reqVO, new LambdaQueryWrapperX<OaLeaveApplyDO>()
                .eq(OaLeaveApplyDO::getCreator, userId.toString())
                .likeIfPresent(OaLeaveApplyDO::getTitle, reqVO.getTitle())
                .eqIfPresent(OaLeaveApplyDO::getStatus, reqVO.getStatus())
                .orderByDesc(OaLeaveApplyDO::getCreateTime, OaLeaveApplyDO::getId));
    }

}
