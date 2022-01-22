package cn.iocoder.yudao.adminserver.modules.bpm.dal.mysql.oa;

import cn.iocoder.yudao.adminserver.modules.bpm.controller.oa.vo.OALeavePageReqVO;
import cn.iocoder.yudao.adminserver.modules.bpm.dal.dataobject.leave.OALeaveDO;
import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.mybatis.core.mapper.BaseMapperX;
import cn.iocoder.yudao.framework.mybatis.core.query.LambdaQueryWrapperX;
import org.apache.ibatis.annotations.Mapper;

/**
 * 请假申请 Mapper
 *
 * @author jason
 * @author 芋道源码
 */
@Mapper
public interface BpmOALeaveMapper extends BaseMapperX<OALeaveDO> {

    default PageResult<OALeaveDO> selectPage(Long userId, OALeavePageReqVO reqVO) {
        return selectPage(reqVO, new LambdaQueryWrapperX<OALeaveDO>()
                .eqIfPresent(OALeaveDO::getId, userId)
                .eqIfPresent(OALeaveDO::getResult, reqVO.getResult())
                .eqIfPresent(OALeaveDO::getType, reqVO.getType())
                .likeIfPresent(OALeaveDO::getReason, reqVO.getReason())
                .betweenIfPresent(OALeaveDO::getCreateTime, reqVO.getBeginCreateTime(), reqVO.getEndCreateTime())
                .orderByDesc(OALeaveDO::getId));
    }

}
