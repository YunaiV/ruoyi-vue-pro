package cn.iocoder.yudao.module.crm.dal.mysql.receivable;

import java.util.*;

import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.mybatis.core.query.LambdaQueryWrapperX;
import cn.iocoder.yudao.framework.mybatis.core.mapper.BaseMapperX;
import cn.iocoder.yudao.module.crm.dal.dataobject.receivable.CrmReceivableDO;
import org.apache.ibatis.annotations.Mapper;
import cn.iocoder.yudao.module.crm.controller.admin.receivable.vo.*;

/**
 * 回款管理 Mapper
 *
 * @author 赤焰
 */
@Mapper
public interface CrmReceivableMapper extends BaseMapperX<CrmReceivableDO> {

    default PageResult<CrmReceivableDO> selectPage(CrmReceivablePageReqVO reqVO) {
        return selectPage(reqVO, new LambdaQueryWrapperX<CrmReceivableDO>()
                .eqIfPresent(CrmReceivableDO::getNo, reqVO.getNo())
                .eqIfPresent(CrmReceivableDO::getPlanId, reqVO.getPlanId())
                .eqIfPresent(CrmReceivableDO::getCustomerId, reqVO.getCustomerId())
                .eqIfPresent(CrmReceivableDO::getContractId, reqVO.getContractId())
                .eqIfPresent(CrmReceivableDO::getCheckStatus, reqVO.getCheckStatus())
                .betweenIfPresent(CrmReceivableDO::getReturnTime, reqVO.getReturnTime())
                .eqIfPresent(CrmReceivableDO::getReturnType, reqVO.getReturnType())
                .eqIfPresent(CrmReceivableDO::getPrice, reqVO.getPrice())
                .eqIfPresent(CrmReceivableDO::getOwnerUserId, reqVO.getOwnerUserId())
                .eqIfPresent(CrmReceivableDO::getStatus, reqVO.getStatus())
                .betweenIfPresent(CrmReceivableDO::getCreateTime, reqVO.getCreateTime())
                .orderByDesc(CrmReceivableDO::getId));
    }

    default List<CrmReceivableDO> selectList(CrmReceivableExportReqVO reqVO) {
        return selectList(new LambdaQueryWrapperX<CrmReceivableDO>()
                .eqIfPresent(CrmReceivableDO::getNo, reqVO.getNo())
                .eqIfPresent(CrmReceivableDO::getPlanId, reqVO.getPlanId())
                .eqIfPresent(CrmReceivableDO::getCustomerId, reqVO.getCustomerId())
                .eqIfPresent(CrmReceivableDO::getContractId, reqVO.getContractId())
                .eqIfPresent(CrmReceivableDO::getCheckStatus, reqVO.getCheckStatus())
                .betweenIfPresent(CrmReceivableDO::getReturnTime, reqVO.getReturnTime())
                .eqIfPresent(CrmReceivableDO::getReturnType, reqVO.getReturnType())
                .eqIfPresent(CrmReceivableDO::getPrice, reqVO.getPrice())
                .eqIfPresent(CrmReceivableDO::getOwnerUserId, reqVO.getOwnerUserId())
                .eqIfPresent(CrmReceivableDO::getBatchId, reqVO.getBatchId())
                .eqIfPresent(CrmReceivableDO::getStatus, reqVO.getStatus())
                .eqIfPresent(CrmReceivableDO::getRemark, reqVO.getRemark())
                .betweenIfPresent(CrmReceivableDO::getCreateTime, reqVO.getCreateTime())
                .orderByDesc(CrmReceivableDO::getId));
    }

}
