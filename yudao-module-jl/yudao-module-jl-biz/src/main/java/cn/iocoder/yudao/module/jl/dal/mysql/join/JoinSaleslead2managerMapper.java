package cn.iocoder.yudao.module.jl.dal.mysql.join;

import java.util.*;

import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.mybatis.core.query.LambdaQueryWrapperX;
import cn.iocoder.yudao.framework.mybatis.core.mapper.BaseMapperX;
import cn.iocoder.yudao.module.jl.dal.dataobject.join.JoinSaleslead2managerDO;
import org.apache.ibatis.annotations.Mapper;
import cn.iocoder.yudao.module.jl.controller.admin.join.vo.*;

/**
 * 销售线索中的项目售前支持人员 Mapper
 *
 * @author 惟象科技
 */
@Mapper
public interface JoinSaleslead2managerMapper extends BaseMapperX<JoinSaleslead2managerDO> {

    default PageResult<JoinSaleslead2managerDO> selectPage(JoinSaleslead2managerPageReqVO reqVO) {
        return selectPage(reqVO, new LambdaQueryWrapperX<JoinSaleslead2managerDO>()
                .betweenIfPresent(JoinSaleslead2managerDO::getCreateTime, reqVO.getCreateTime())
                .eqIfPresent(JoinSaleslead2managerDO::getSalesleadId, reqVO.getSalesleadId())
                .eqIfPresent(JoinSaleslead2managerDO::getManagerId, reqVO.getManagerId())
                .orderByDesc(JoinSaleslead2managerDO::getId));
    }

    default List<JoinSaleslead2managerDO> selectList(JoinSaleslead2managerExportReqVO reqVO) {
        return selectList(new LambdaQueryWrapperX<JoinSaleslead2managerDO>()
                .betweenIfPresent(JoinSaleslead2managerDO::getCreateTime, reqVO.getCreateTime())
                .eqIfPresent(JoinSaleslead2managerDO::getSalesleadId, reqVO.getSalesleadId())
                .eqIfPresent(JoinSaleslead2managerDO::getManagerId, reqVO.getManagerId())
                .orderByDesc(JoinSaleslead2managerDO::getId));
    }

}
