package cn.iocoder.yudao.module.jl.dal.mysql.crm;

import java.util.*;

import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.mybatis.core.query.LambdaQueryWrapperX;
import cn.iocoder.yudao.framework.mybatis.core.mapper.BaseMapperX;
import cn.iocoder.yudao.module.jl.dal.dataobject.crm.SalesleadDO;
import org.apache.ibatis.annotations.Mapper;
import cn.iocoder.yudao.module.jl.controller.admin.crm.vo.*;

/**
 * 销售线索 Mapper
 *
 * @author 惟象科技
 */
@Mapper
public interface SalesleadMapper extends BaseMapperX<SalesleadDO> {

    default PageResult<SalesleadDO> selectPage(SalesleadPageReqVO reqVO) {
        return selectPage(reqVO, new LambdaQueryWrapperX<SalesleadDO>()
                .betweenIfPresent(SalesleadDO::getCreateTime, reqVO.getCreateTime())
                .eqIfPresent(SalesleadDO::getSource, reqVO.getSource())
                .eqIfPresent(SalesleadDO::getRequirement, reqVO.getRequirement())
                .eqIfPresent(SalesleadDO::getBudget, reqVO.getBudget())
                .eqIfPresent(SalesleadDO::getQuotation, reqVO.getQuotation())
                .eqIfPresent(SalesleadDO::getStatus, reqVO.getStatus())
                .eqIfPresent(SalesleadDO::getCustomerId, reqVO.getCustomerId())
                .eqIfPresent(SalesleadDO::getProjectId, reqVO.getProjectId())
                .orderByDesc(SalesleadDO::getId));
    }

    default List<SalesleadDO> selectList(SalesleadExportReqVO reqVO) {
        return selectList(new LambdaQueryWrapperX<SalesleadDO>()
                .betweenIfPresent(SalesleadDO::getCreateTime, reqVO.getCreateTime())
                .eqIfPresent(SalesleadDO::getSource, reqVO.getSource())
                .eqIfPresent(SalesleadDO::getRequirement, reqVO.getRequirement())
                .eqIfPresent(SalesleadDO::getBudget, reqVO.getBudget())
                .eqIfPresent(SalesleadDO::getQuotation, reqVO.getQuotation())
                .eqIfPresent(SalesleadDO::getStatus, reqVO.getStatus())
                .eqIfPresent(SalesleadDO::getCustomerId, reqVO.getCustomerId())
                .eqIfPresent(SalesleadDO::getProjectId, reqVO.getProjectId())
                .orderByDesc(SalesleadDO::getId));
    }

}
