package cn.iocoder.yudao.module.jl.dal.mysql.crm;

import java.util.*;

import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.mybatis.core.query.LambdaQueryWrapperX;
import cn.iocoder.yudao.framework.mybatis.core.mapper.BaseMapperX;
import cn.iocoder.yudao.module.jl.dal.dataobject.crm.FollowupDO;
import org.apache.ibatis.annotations.Mapper;
import cn.iocoder.yudao.module.jl.controller.admin.crm.vo.*;

/**
 * 销售线索跟进，可以是跟进客户，也可以是跟进线索 Mapper
 *
 * @author 惟象科技
 */
@Mapper
public interface FollowupMapper extends BaseMapperX<FollowupDO> {

    default PageResult<FollowupDO> selectPage(FollowupPageReqVO reqVO) {
        return selectPage(reqVO, new LambdaQueryWrapperX<FollowupDO>()
                .betweenIfPresent(FollowupDO::getCreateTime, reqVO.getCreateTime())
                .eqIfPresent(FollowupDO::getContent, reqVO.getContent())
                .eqIfPresent(FollowupDO::getCustomerId, reqVO.getCustomerId())
                .eqIfPresent(FollowupDO::getRefId, reqVO.getRefId())
                .eqIfPresent(FollowupDO::getType, reqVO.getType())
                .orderByDesc(FollowupDO::getId));
    }

    default FollowupDO selectLatestOneBySealsLeadId(Long id) {
        return selectOne(new LambdaQueryWrapperX<FollowupDO>()
                .eqIfPresent(FollowupDO::getRefId, id)
                .eq(FollowupDO::getType, "leadsFollowup")
                .orderByDesc(FollowupDO::getCreateTime)
                .last("LIMIT 1"));
    }

    default List<FollowupDO> selectList(FollowupExportReqVO reqVO) {
        return selectList(new LambdaQueryWrapperX<FollowupDO>()
                .betweenIfPresent(FollowupDO::getCreateTime, reqVO.getCreateTime())
                .eqIfPresent(FollowupDO::getContent, reqVO.getContent())
                .eqIfPresent(FollowupDO::getCustomerId, reqVO.getCustomerId())
                .eqIfPresent(FollowupDO::getRefId, reqVO.getRefId())
                .eqIfPresent(FollowupDO::getType, reqVO.getType())
                .orderByDesc(FollowupDO::getId));
    }

}
