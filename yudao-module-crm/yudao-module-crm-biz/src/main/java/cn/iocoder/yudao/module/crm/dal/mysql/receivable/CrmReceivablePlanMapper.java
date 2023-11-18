package cn.iocoder.yudao.module.crm.dal.mysql.receivable;

import java.util.*;

import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.mybatis.core.query.LambdaQueryWrapperX;
import cn.iocoder.yudao.framework.mybatis.core.mapper.BaseMapperX;
import cn.iocoder.yudao.module.crm.dal.dataobject.receivable.CrmReceivablePlanDO;
import org.apache.ibatis.annotations.Mapper;
import cn.iocoder.yudao.module.crm.controller.admin.receivable.vo.*;

/**
 * 回款计划 Mapper
 *
 * @author 芋道源码
 */
@Mapper
public interface CrmReceivablePlanMapper extends BaseMapperX<CrmReceivablePlanDO> {

    default PageResult<CrmReceivablePlanDO> selectPage(CrmReceivablePlanPageReqVO reqVO) {
        return selectPage(reqVO, new LambdaQueryWrapperX<CrmReceivablePlanDO>()
                .eqIfPresent(CrmReceivablePlanDO::getStatus, reqVO.getStatus())
                .eqIfPresent(CrmReceivablePlanDO::getCheckStatus, reqVO.getCheckStatus())
                .betweenIfPresent(CrmReceivablePlanDO::getReturnTime, reqVO.getReturnTime())
                .betweenIfPresent(CrmReceivablePlanDO::getRemindTime, reqVO.getRemindTime())
                .eqIfPresent(CrmReceivablePlanDO::getCustomerId, reqVO.getCustomerId())
                .eqIfPresent(CrmReceivablePlanDO::getContractId, reqVO.getContractId())
                .eqIfPresent(CrmReceivablePlanDO::getOwnerUserId, reqVO.getOwnerUserId())
                .betweenIfPresent(CrmReceivablePlanDO::getCreateTime, reqVO.getCreateTime())
                .orderByDesc(CrmReceivablePlanDO::getId));
    }

    default List<CrmReceivablePlanDO> selectList(CrmReceivablePlanExportReqVO reqVO) {
        return selectList(new LambdaQueryWrapperX<CrmReceivablePlanDO>()
                .eqIfPresent(CrmReceivablePlanDO::getPeriod, reqVO.getPeriod())
                .eqIfPresent(CrmReceivablePlanDO::getStatus, reqVO.getStatus())
                .eqIfPresent(CrmReceivablePlanDO::getCheckStatus, reqVO.getCheckStatus())
                .betweenIfPresent(CrmReceivablePlanDO::getReturnTime, reqVO.getReturnTime())
                .eqIfPresent(CrmReceivablePlanDO::getRemindDays, reqVO.getRemindDays())
                .betweenIfPresent(CrmReceivablePlanDO::getRemindTime, reqVO.getRemindTime())
                .eqIfPresent(CrmReceivablePlanDO::getCustomerId, reqVO.getCustomerId())
                .eqIfPresent(CrmReceivablePlanDO::getContractId, reqVO.getContractId())
                .eqIfPresent(CrmReceivablePlanDO::getOwnerUserId, reqVO.getOwnerUserId())
                .eqIfPresent(CrmReceivablePlanDO::getRemark, reqVO.getRemark())
                .betweenIfPresent(CrmReceivablePlanDO::getCreateTime, reqVO.getCreateTime())
                .orderByDesc(CrmReceivablePlanDO::getId));
    }

}
