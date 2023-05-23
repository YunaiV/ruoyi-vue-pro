package cn.iocoder.yudao.module.jl.dal.mysql.join;

import java.util.*;

import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.mybatis.core.query.LambdaQueryWrapperX;
import cn.iocoder.yudao.framework.mybatis.core.mapper.BaseMapperX;
import cn.iocoder.yudao.module.jl.dal.dataobject.join.JoinSaleslead2reportDO;
import org.apache.ibatis.annotations.Mapper;
import cn.iocoder.yudao.module.jl.controller.admin.join.vo.*;

/**
 * 销售线索中的方案 Mapper
 *
 * @author 惟象科技
 */
@Mapper
public interface JoinSaleslead2reportMapper extends BaseMapperX<JoinSaleslead2reportDO> {

    default PageResult<JoinSaleslead2reportDO> selectPage(JoinSaleslead2reportPageReqVO reqVO) {
        return selectPage(reqVO, new LambdaQueryWrapperX<JoinSaleslead2reportDO>()
                .betweenIfPresent(JoinSaleslead2reportDO::getCreateTime, reqVO.getCreateTime())
                .eqIfPresent(JoinSaleslead2reportDO::getSalesleadId, reqVO.getSalesleadId())
                .eqIfPresent(JoinSaleslead2reportDO::getFileUrl, reqVO.getFileUrl())
                .likeIfPresent(JoinSaleslead2reportDO::getFileName, reqVO.getFileName())
                .orderByDesc(JoinSaleslead2reportDO::getId));
    }

    default List<JoinSaleslead2reportDO> selectList(JoinSaleslead2reportExportReqVO reqVO) {
        return selectList(new LambdaQueryWrapperX<JoinSaleslead2reportDO>()
                .betweenIfPresent(JoinSaleslead2reportDO::getCreateTime, reqVO.getCreateTime())
                .eqIfPresent(JoinSaleslead2reportDO::getSalesleadId, reqVO.getSalesleadId())
                .eqIfPresent(JoinSaleslead2reportDO::getFileUrl, reqVO.getFileUrl())
                .likeIfPresent(JoinSaleslead2reportDO::getFileName, reqVO.getFileName())
                .orderByDesc(JoinSaleslead2reportDO::getId));
    }

}
