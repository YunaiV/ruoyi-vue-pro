package cn.iocoder.yudao.adminserver.modules.activiti.dal.mysql.oa;

import java.util.*;

import cn.iocoder.yudao.adminserver.modules.activiti.dal.dataobject.oa.OALeaveDO;
import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.mybatis.core.query.QueryWrapperX;
import cn.iocoder.yudao.framework.mybatis.core.mapper.BaseMapperX;
import org.apache.ibatis.annotations.Mapper;
import cn.iocoder.yudao.adminserver.modules.activiti.controller.oa.vo.*;

/**
 * 请假申请 Mapper
 *
 * @author 芋艿
 */
@Mapper
public interface OALeaveMapper extends BaseMapperX<OALeaveDO> {

    default PageResult<OALeaveDO> selectPage(OALeavePageReqVO reqVO) {
        return selectPage(reqVO, new QueryWrapperX<OALeaveDO>()
                .eqIfPresent("process_instance_id", reqVO.getProcessInstanceId())
                .eqIfPresent("status", reqVO.getStatus())
                .eqIfPresent("user_id", reqVO.getUserId())
                .betweenIfPresent("start_time", reqVO.getBeginStartTime(), reqVO.getEndStartTime())
                .betweenIfPresent("end_time", reqVO.getBeginEndTime(), reqVO.getEndEndTime())
                .eqIfPresent("leave_type", reqVO.getLeaveType())
                .eqIfPresent("reason", reqVO.getReason())
                .betweenIfPresent("apply_time", reqVO.getBeginApplyTime(), reqVO.getEndApplyTime())
                .orderByDesc("id")        );
    }

    default List<OALeaveDO> selectList(OALeaveExportReqVO reqVO) {
        return selectList(new QueryWrapperX<OALeaveDO>()
                .eqIfPresent("process_instance_id", reqVO.getProcessInstanceId())
                .eqIfPresent("status", reqVO.getStatus())
                .eqIfPresent("user_id", reqVO.getUserId())
                .betweenIfPresent("start_time", reqVO.getBeginStartTime(), reqVO.getEndStartTime())
                .betweenIfPresent("end_time", reqVO.getBeginEndTime(), reqVO.getEndEndTime())
                .eqIfPresent("leave_type", reqVO.getLeaveType())
                .eqIfPresent("reason", reqVO.getReason())
                .betweenIfPresent("apply_time", reqVO.getBeginApplyTime(), reqVO.getEndApplyTime())
                .orderByDesc("id")        );
    }

}
