package cn.iocoder.yudao.module.jl.dal.mysql.join;

import java.util.*;

import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.mybatis.core.query.LambdaQueryWrapperX;
import cn.iocoder.yudao.framework.mybatis.core.mapper.BaseMapperX;
import cn.iocoder.yudao.module.jl.dal.dataobject.join.JoinSaleslead2competitorDO;
import org.apache.ibatis.annotations.Mapper;
import cn.iocoder.yudao.module.jl.controller.admin.join.vo.*;

/**
 * 销售线索中竞争对手的报价 Mapper
 *
 * @author 惟象科技
 */
@Mapper
public interface JoinSaleslead2competitorMapper extends BaseMapperX<JoinSaleslead2competitorDO> {

    default PageResult<JoinSaleslead2competitorDO> selectPage(JoinSaleslead2competitorPageReqVO reqVO) {
        return selectPage(reqVO, new LambdaQueryWrapperX<JoinSaleslead2competitorDO>()
                .betweenIfPresent(JoinSaleslead2competitorDO::getCreateTime, reqVO.getCreateTime())
                .eqIfPresent(JoinSaleslead2competitorDO::getSalesleadId, reqVO.getSalesleadId())
                .eqIfPresent(JoinSaleslead2competitorDO::getCompetitorId, reqVO.getCompetitorId())
                .eqIfPresent(JoinSaleslead2competitorDO::getCompetitorQuotation, reqVO.getCompetitorQuotation())
                .orderByDesc(JoinSaleslead2competitorDO::getId));
    }

    default List<JoinSaleslead2competitorDO> selectList(JoinSaleslead2competitorExportReqVO reqVO) {
        return selectList(new LambdaQueryWrapperX<JoinSaleslead2competitorDO>()
                .betweenIfPresent(JoinSaleslead2competitorDO::getCreateTime, reqVO.getCreateTime())
                .eqIfPresent(JoinSaleslead2competitorDO::getSalesleadId, reqVO.getSalesleadId())
                .eqIfPresent(JoinSaleslead2competitorDO::getCompetitorId, reqVO.getCompetitorId())
                .eqIfPresent(JoinSaleslead2competitorDO::getCompetitorQuotation, reqVO.getCompetitorQuotation())
                .orderByDesc(JoinSaleslead2competitorDO::getId));
    }

}
