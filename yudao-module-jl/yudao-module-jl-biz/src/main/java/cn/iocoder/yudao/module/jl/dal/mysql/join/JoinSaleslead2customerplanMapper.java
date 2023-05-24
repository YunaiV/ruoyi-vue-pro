package cn.iocoder.yudao.module.jl.dal.mysql.join;

import java.util.*;

import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.mybatis.core.query.LambdaQueryWrapperX;
import cn.iocoder.yudao.framework.mybatis.core.mapper.BaseMapperX;
import cn.iocoder.yudao.module.jl.dal.dataobject.join.JoinSaleslead2customerplanDO;
import org.apache.ibatis.annotations.Mapper;
import cn.iocoder.yudao.module.jl.controller.admin.join.vo.*;

/**
 * 销售线索中的客户方案 Mapper
 *
 * @author 惟象科技
 */
@Mapper
public interface JoinSaleslead2customerplanMapper extends BaseMapperX<JoinSaleslead2customerplanDO> {

    default PageResult<JoinSaleslead2customerplanDO> selectPage(JoinSaleslead2customerplanPageReqVO reqVO) {
        return selectPage(reqVO, new LambdaQueryWrapperX<JoinSaleslead2customerplanDO>()
                .betweenIfPresent(JoinSaleslead2customerplanDO::getCreateTime, reqVO.getCreateTime())
                .eqIfPresent(JoinSaleslead2customerplanDO::getSalesleadId, reqVO.getSalesleadId())
                .eqIfPresent(JoinSaleslead2customerplanDO::getFileUrl, reqVO.getFileUrl())
                .likeIfPresent(JoinSaleslead2customerplanDO::getFileName, reqVO.getFileName())
                .orderByDesc(JoinSaleslead2customerplanDO::getId));
    }

    // selectBySalesleadId，按照时间倒序，只选择一个
    default List<JoinSaleslead2customerplanDO> selectBySalesleadId(Long salesleadId) {
        return selectList(new LambdaQueryWrapperX<JoinSaleslead2customerplanDO>()
                .eq(JoinSaleslead2customerplanDO::getSalesleadId, salesleadId)
                .orderByDesc(JoinSaleslead2customerplanDO::getCreateTime));
    }



    default List<JoinSaleslead2customerplanDO> selectList(JoinSaleslead2customerplanExportReqVO reqVO) {
        return selectList(new LambdaQueryWrapperX<JoinSaleslead2customerplanDO>()
                .betweenIfPresent(JoinSaleslead2customerplanDO::getCreateTime, reqVO.getCreateTime())
                .eqIfPresent(JoinSaleslead2customerplanDO::getSalesleadId, reqVO.getSalesleadId())
                .eqIfPresent(JoinSaleslead2customerplanDO::getFileUrl, reqVO.getFileUrl())
                .likeIfPresent(JoinSaleslead2customerplanDO::getFileName, reqVO.getFileName())
                .orderByDesc(JoinSaleslead2customerplanDO::getId));
    }

}
