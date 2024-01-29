package cn.iocoder.yudao.module.crm.dal.mysql.receivable;

import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.mybatis.core.mapper.BaseMapperX;
import cn.iocoder.yudao.framework.mybatis.core.query.LambdaQueryWrapperX;
import cn.iocoder.yudao.framework.mybatis.core.query.MPJLambdaWrapperX;
import cn.iocoder.yudao.module.crm.controller.admin.receivable.vo.plan.CrmReceivablePlanPageReqVO;
import cn.iocoder.yudao.module.crm.dal.dataobject.receivable.CrmReceivablePlanDO;
import cn.iocoder.yudao.module.crm.enums.common.CrmBizTypeEnum;
import cn.iocoder.yudao.module.crm.util.CrmQueryWrapperUtils;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import org.apache.ibatis.annotations.Mapper;

import java.util.Collection;
import java.util.List;

/**
 * 回款计划 Mapper
 *
 * @author 芋道源码
 */
@Mapper
public interface CrmReceivablePlanMapper extends BaseMapperX<CrmReceivablePlanDO> {

    default int updateOwnerUserIdById(Long id, Long ownerUserId) {
        return update(new LambdaUpdateWrapper<CrmReceivablePlanDO>()
                .eq(CrmReceivablePlanDO::getId, id)
                .set(CrmReceivablePlanDO::getOwnerUserId, ownerUserId));
    }

    default PageResult<CrmReceivablePlanDO> selectPageByCustomerId(CrmReceivablePlanPageReqVO reqVO) {
        return selectPage(reqVO, new LambdaQueryWrapperX<CrmReceivablePlanDO>()
                .eq(CrmReceivablePlanDO::getCustomerId, reqVO.getCustomerId()) // 必须传递
                .eqIfPresent(CrmReceivablePlanDO::getContractId, reqVO.getContractId())
                .orderByDesc(CrmReceivablePlanDO::getId));
    }

    default PageResult<CrmReceivablePlanDO> selectPage(CrmReceivablePlanPageReqVO pageReqVO, Long userId) {
        MPJLambdaWrapperX<CrmReceivablePlanDO> query = new MPJLambdaWrapperX<>();
        // 拼接数据权限的查询条件
        CrmQueryWrapperUtils.appendPermissionCondition(query, CrmBizTypeEnum.CRM_RECEIVABLE_PLAN.getType(),
                CrmReceivablePlanDO::getId, userId, pageReqVO.getSceneType(), Boolean.FALSE);
        // 拼接自身的查询条件
        query.selectAll(CrmReceivablePlanDO.class)
                .eqIfPresent(CrmReceivablePlanDO::getCustomerId, pageReqVO.getCustomerId())
                .eqIfPresent(CrmReceivablePlanDO::getContractId, pageReqVO.getContractId())
                .orderByDesc(CrmReceivablePlanDO::getId);
        return selectJoinPage(pageReqVO, CrmReceivablePlanDO.class, query);
    }

    default List<CrmReceivablePlanDO> selectBatchIds(Collection<Long> ids, Long userId) {
        MPJLambdaWrapperX<CrmReceivablePlanDO> query = new MPJLambdaWrapperX<>();
        // 拼接数据权限的查询条件
        CrmQueryWrapperUtils.appendPermissionCondition(query, CrmBizTypeEnum.CRM_RECEIVABLE_PLAN.getType(), ids, userId);
        return selectJoinList(CrmReceivablePlanDO.class, query);
    }

}
