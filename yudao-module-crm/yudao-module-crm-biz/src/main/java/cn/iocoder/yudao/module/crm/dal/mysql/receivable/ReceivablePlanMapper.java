package cn.iocoder.yudao.module.crm.dal.mysql.receivable;

import java.util.*;

import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.mybatis.core.query.LambdaQueryWrapperX;
import cn.iocoder.yudao.framework.mybatis.core.mapper.BaseMapperX;
import cn.iocoder.yudao.module.crm.dal.dataobject.receivable.ReceivablePlanDO;
import org.apache.ibatis.annotations.Mapper;
import cn.iocoder.yudao.module.crm.controller.admin.receivable.vo.*;

/**
 * 回款计划 Mapper
 *
 * @author 芋道源码
 */
@Mapper
public interface ReceivablePlanMapper extends BaseMapperX<ReceivablePlanDO> {

    default PageResult<ReceivablePlanDO> selectPage(ReceivablePlanPageReqVO reqVO) {
        return selectPage(reqVO, new LambdaQueryWrapperX<ReceivablePlanDO>()
                .eqIfPresent(ReceivablePlanDO::getPeriod, reqVO.getPeriod())
                .eqIfPresent(ReceivablePlanDO::getStatus, reqVO.getStatus())
                .eqIfPresent(ReceivablePlanDO::getCheckStatus, reqVO.getCheckStatus())
                .betweenIfPresent(ReceivablePlanDO::getReturnTime, reqVO.getReturnTime())
                .eqIfPresent(ReceivablePlanDO::getRemindDays, reqVO.getRemindDays())
                .betweenIfPresent(ReceivablePlanDO::getRemindTime, reqVO.getRemindTime())
                .eqIfPresent(ReceivablePlanDO::getCustomerId, reqVO.getCustomerId())
                .eqIfPresent(ReceivablePlanDO::getContractId, reqVO.getContractId())
                .eqIfPresent(ReceivablePlanDO::getOwnerUserId, reqVO.getOwnerUserId())
                .eqIfPresent(ReceivablePlanDO::getRemark, reqVO.getRemark())
                .betweenIfPresent(ReceivablePlanDO::getCreateTime, reqVO.getCreateTime())
                .orderByDesc(ReceivablePlanDO::getId));
    }

    default List<ReceivablePlanDO> selectList(ReceivablePlanExportReqVO reqVO) {
        return selectList(new LambdaQueryWrapperX<ReceivablePlanDO>()
                .eqIfPresent(ReceivablePlanDO::getPeriod, reqVO.getPeriod())
                .eqIfPresent(ReceivablePlanDO::getStatus, reqVO.getStatus())
                .eqIfPresent(ReceivablePlanDO::getCheckStatus, reqVO.getCheckStatus())
                .betweenIfPresent(ReceivablePlanDO::getReturnTime, reqVO.getReturnTime())
                .eqIfPresent(ReceivablePlanDO::getRemindDays, reqVO.getRemindDays())
                .betweenIfPresent(ReceivablePlanDO::getRemindTime, reqVO.getRemindTime())
                .eqIfPresent(ReceivablePlanDO::getCustomerId, reqVO.getCustomerId())
                .eqIfPresent(ReceivablePlanDO::getContractId, reqVO.getContractId())
                .eqIfPresent(ReceivablePlanDO::getOwnerUserId, reqVO.getOwnerUserId())
                .eqIfPresent(ReceivablePlanDO::getRemark, reqVO.getRemark())
                .betweenIfPresent(ReceivablePlanDO::getCreateTime, reqVO.getCreateTime())
                .orderByDesc(ReceivablePlanDO::getId));
    }

}
