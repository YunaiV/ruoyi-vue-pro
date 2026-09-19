package cn.iocoder.yudao.module.oa.dal.mysql.vehicle;

import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.mybatis.core.mapper.BaseMapperX;
import cn.iocoder.yudao.framework.mybatis.core.query.LambdaQueryWrapperX;
import cn.iocoder.yudao.module.bpm.enums.task.BpmProcessInstanceStatusEnum;
import cn.iocoder.yudao.module.oa.controller.admin.vehicle.vo.apply.OaVehicleApplyPageReqVO;
import cn.iocoder.yudao.module.oa.dal.dataobject.vehicle.OaVehicleApplyDO;
import cn.iocoder.yudao.module.oa.enums.vehicle.OaVehicleReturnStatusEnum;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import org.apache.ibatis.annotations.Mapper;

import java.time.LocalDateTime;

/**
 * OA 用车申请 Mapper
 *
 * @author 芋道源码
 */
@Mapper
public interface OaVehicleApplyMapper extends BaseMapperX<OaVehicleApplyDO> {

    default Long selectCountByVehicleId(Long id) {
        return selectCount(OaVehicleApplyDO::getVehicleId, id);
    }

    default OaVehicleApplyDO selectByNo(String no) {
        return selectOne(OaVehicleApplyDO::getNo, no);
    }

    default int updateReturnStatusByIdAndReturnStatus(Long id, Integer expectedReturnStatus, Integer returnStatus) {
        return update(new OaVehicleApplyDO().setReturnStatus(returnStatus), new LambdaQueryWrapperX<OaVehicleApplyDO>()
                .eq(OaVehicleApplyDO::getId, id)
                .eq(OaVehicleApplyDO::getStatus, BpmProcessInstanceStatusEnum.APPROVE.getStatus())
                .eq(OaVehicleApplyDO::getReturnStatus, expectedReturnStatus));
    }

    default PageResult<OaVehicleApplyDO> selectPage(Long userId, OaVehicleApplyPageReqVO reqVO) {
        return selectPage(reqVO, new LambdaQueryWrapperX<OaVehicleApplyDO>()
                .eq(OaVehicleApplyDO::getUserId, userId)
                .likeIfPresent(OaVehicleApplyDO::getNo, reqVO.getNo())
                .likeIfPresent(OaVehicleApplyDO::getVehicleNo, reqVO.getVehicleNo())
                .eqIfPresent(OaVehicleApplyDO::getStatus, reqVO.getStatus())
                .eqIfPresent(OaVehicleApplyDO::getReturnStatus, reqVO.getReturnStatus())
                .eqIfPresent(OaVehicleApplyDO::getDeptId, reqVO.getDeptId())
                .betweenIfPresent(OaVehicleApplyDO::getCreateTime, reqVO.getCreateTime())
                .orderByDesc(OaVehicleApplyDO::getId));
    }

    default Long selectCountByVehicleIdAndTimeOverlap(Long vehicleId, Long excludeId,
                                                      LocalDateTime startTime, LocalDateTime endTime) {
        return selectCount(buildActiveQuery()
                .eq(OaVehicleApplyDO::getVehicleId, vehicleId)
                .ne(OaVehicleApplyDO::getId, excludeId)
                .lt(OaVehicleApplyDO::getStartTime, endTime)
                .gt(OaVehicleApplyDO::getEndTime, startTime));
    }

    default LambdaQueryWrapper<OaVehicleApplyDO> buildActiveQuery() {
        return new LambdaQueryWrapperX<OaVehicleApplyDO>()
                .and(query -> query.eq(OaVehicleApplyDO::getStatus, BpmProcessInstanceStatusEnum.RUNNING.getStatus())
                        .or(approved -> approved.eq(OaVehicleApplyDO::getStatus, BpmProcessInstanceStatusEnum.APPROVE.getStatus())
                                .in(OaVehicleApplyDO::getReturnStatus, OaVehicleReturnStatusEnum.PENDING_RETURN.getStatus(), OaVehicleReturnStatusEnum.RETURNING.getStatus())));
    }

}
