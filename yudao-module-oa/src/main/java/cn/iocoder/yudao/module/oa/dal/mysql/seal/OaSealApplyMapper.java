package cn.iocoder.yudao.module.oa.dal.mysql.seal;

import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.mybatis.core.mapper.BaseMapperX;
import cn.iocoder.yudao.framework.mybatis.core.query.LambdaQueryWrapperX;
import cn.iocoder.yudao.module.bpm.enums.task.BpmProcessInstanceStatusEnum;
import cn.iocoder.yudao.module.oa.controller.admin.seal.vo.apply.OaSealApplyPageReqVO;
import cn.iocoder.yudao.module.oa.dal.dataobject.seal.OaSealApplyDO;
import cn.iocoder.yudao.module.oa.enums.seal.OaSealUseModeEnum;
import org.apache.ibatis.annotations.Mapper;

import java.time.LocalDateTime;
import java.util.List;

/**
 * OA 用印申请 Mapper
 *
 * @author 芋道源码
 */
@Mapper
public interface OaSealApplyMapper extends BaseMapperX<OaSealApplyDO> {

    default Long selectCountBySealId(Long id) {
        return selectCount(OaSealApplyDO::getSealId, id);
    }

    default OaSealApplyDO selectByNo(String no) {
        return selectOne(OaSealApplyDO::getNo, no);
    }

    default PageResult<OaSealApplyDO> selectPage(Long userId, OaSealApplyPageReqVO reqVO) {
        LambdaQueryWrapperX<OaSealApplyDO> query = new LambdaQueryWrapperX<OaSealApplyDO>()
                .eq(OaSealApplyDO::getUserId, userId)
                .likeIfPresent(OaSealApplyDO::getNo, reqVO.getNo())
                .eqIfPresent(OaSealApplyDO::getSealId, reqVO.getSealId())
                .likeIfPresent(OaSealApplyDO::getSealName, reqVO.getSealName())
                .eqIfPresent(OaSealApplyDO::getDeptId, reqVO.getDeptId())
                .betweenIfPresent(OaSealApplyDO::getCreateTime, reqVO.getCreateTime())
                .betweenIfPresent(OaSealApplyDO::getExpectedUseTime, reqVO.getExpectedUseTime())
                .eqIfPresent(OaSealApplyDO::getStatus, reqVO.getStatus())
                .eqIfPresent(OaSealApplyDO::getType, reqVO.getType())
                .eqIfPresent(OaSealApplyDO::getMode, reqVO.getMode())
                .eqIfPresent(OaSealApplyDO::getUrgent, reqVO.getUrgent())
                .orderByDesc(OaSealApplyDO::getId);
        query.eqIfPresent(OaSealApplyDO::getUseStatus, reqVO.getUseStatus());
        return selectPage(reqVO, query);
    }

    default List<OaSealApplyDO> selectListBySealIdAndBorrowTimeConflict(
            Long sealId, Long excludeId, LocalDateTime beginTime, LocalDateTime endTime) {
        return selectList(new LambdaQueryWrapperX<OaSealApplyDO>().eq(OaSealApplyDO::getSealId, sealId)
                .ne(OaSealApplyDO::getId, excludeId)
                .eq(OaSealApplyDO::getMode, OaSealUseModeEnum.BORROW.getMode())
                .le(OaSealApplyDO::getExpectedUseTime, endTime)
                .ge(OaSealApplyDO::getExpectedReturnTime, beginTime)
                .in(OaSealApplyDO::getStatus, BpmProcessInstanceStatusEnum.RUNNING.getStatus(),
                        BpmProcessInstanceStatusEnum.APPROVE.getStatus())
                .orderByAsc(OaSealApplyDO::getId));
    }
}
