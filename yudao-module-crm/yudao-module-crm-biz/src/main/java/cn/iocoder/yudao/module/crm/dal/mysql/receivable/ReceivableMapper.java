package cn.iocoder.yudao.module.crm.dal.mysql.receivable;

import java.util.*;

import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.mybatis.core.query.LambdaQueryWrapperX;
import cn.iocoder.yudao.framework.mybatis.core.mapper.BaseMapperX;
import cn.iocoder.yudao.module.crm.dal.dataobject.receivable.ReceivableDO;
import org.apache.ibatis.annotations.Mapper;
import cn.iocoder.yudao.module.crm.controller.admin.receivable.vo.*;

/**
 * 回款管理 Mapper
 *
 * @author 赤焰
 */
@Mapper
public interface ReceivableMapper extends BaseMapperX<ReceivableDO> {

    default PageResult<ReceivableDO> selectPage(ReceivablePageReqVO reqVO) {
        return selectPage(reqVO, new LambdaQueryWrapperX<ReceivableDO>()
                .eqIfPresent(ReceivableDO::getNo, reqVO.getNo())
                .eqIfPresent(ReceivableDO::getPlanId, reqVO.getPlanId())
                .eqIfPresent(ReceivableDO::getCustomerId, reqVO.getCustomerId())
                .eqIfPresent(ReceivableDO::getContractId, reqVO.getContractId())
                .eqIfPresent(ReceivableDO::getCheckStatus, reqVO.getCheckStatus())
                .eqIfPresent(ReceivableDO::getProcessInstanceId, reqVO.getProcessInstanceId())
                .betweenIfPresent(ReceivableDO::getReturnTime, reqVO.getReturnTime())
                .eqIfPresent(ReceivableDO::getReturnType, reqVO.getReturnType())
                .eqIfPresent(ReceivableDO::getPrice, reqVO.getPrice())
                .eqIfPresent(ReceivableDO::getOwnerUserId, reqVO.getOwnerUserId())
                .eqIfPresent(ReceivableDO::getBatchId, reqVO.getBatchId())
                .eqIfPresent(ReceivableDO::getSort, reqVO.getSort())
                .eqIfPresent(ReceivableDO::getDataScope, reqVO.getDataScope())
                .eqIfPresent(ReceivableDO::getDataScopeDeptIds, reqVO.getDataScopeDeptIds())
                .eqIfPresent(ReceivableDO::getStatus, reqVO.getStatus())
                .eqIfPresent(ReceivableDO::getRemark, reqVO.getRemark())
                .betweenIfPresent(ReceivableDO::getCreateTime, reqVO.getCreateTime())
                .orderByDesc(ReceivableDO::getId));
    }

    default List<ReceivableDO> selectList(ReceivableExportReqVO reqVO) {
        return selectList(new LambdaQueryWrapperX<ReceivableDO>()
                .eqIfPresent(ReceivableDO::getNo, reqVO.getNo())
                .eqIfPresent(ReceivableDO::getPlanId, reqVO.getPlanId())
                .eqIfPresent(ReceivableDO::getCustomerId, reqVO.getCustomerId())
                .eqIfPresent(ReceivableDO::getContractId, reqVO.getContractId())
                .eqIfPresent(ReceivableDO::getCheckStatus, reqVO.getCheckStatus())
                .eqIfPresent(ReceivableDO::getProcessInstanceId, reqVO.getProcessInstanceId())
                .betweenIfPresent(ReceivableDO::getReturnTime, reqVO.getReturnTime())
                .eqIfPresent(ReceivableDO::getReturnType, reqVO.getReturnType())
                .eqIfPresent(ReceivableDO::getPrice, reqVO.getPrice())
                .eqIfPresent(ReceivableDO::getOwnerUserId, reqVO.getOwnerUserId())
                .eqIfPresent(ReceivableDO::getBatchId, reqVO.getBatchId())
                .eqIfPresent(ReceivableDO::getSort, reqVO.getSort())
                .eqIfPresent(ReceivableDO::getDataScope, reqVO.getDataScope())
                .eqIfPresent(ReceivableDO::getDataScopeDeptIds, reqVO.getDataScopeDeptIds())
                .eqIfPresent(ReceivableDO::getStatus, reqVO.getStatus())
                .eqIfPresent(ReceivableDO::getRemark, reqVO.getRemark())
                .betweenIfPresent(ReceivableDO::getCreateTime, reqVO.getCreateTime())
                .orderByDesc(ReceivableDO::getId));
    }

}
