package cn.iocoder.yudao.module.infra.dal.mysql.demo11;

import java.util.*;

import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.mybatis.core.query.LambdaQueryWrapperX;
import cn.iocoder.yudao.framework.mybatis.core.mapper.BaseMapperX;
import cn.iocoder.yudao.module.infra.dal.dataobject.demo11.InfraDemo11StudentDO;
import org.apache.ibatis.annotations.Mapper;
import cn.iocoder.yudao.module.infra.controller.admin.demo11.vo.*;

/**
 * 学生 Mapper
 *
 * @author 芋道源码
 */
@Mapper
public interface InfraDemo11StudentMapper extends BaseMapperX<InfraDemo11StudentDO> {

    default PageResult<InfraDemo11StudentDO> selectPage(InfraDemo11StudentPageReqVO reqVO) {
        return selectPage(reqVO, new LambdaQueryWrapperX<InfraDemo11StudentDO>()
                .likeIfPresent(InfraDemo11StudentDO::getName, reqVO.getName())
                .eqIfPresent(InfraDemo11StudentDO::getBirthday, reqVO.getBirthday())
                .eqIfPresent(InfraDemo11StudentDO::getSex, reqVO.getSex())
                .eqIfPresent(InfraDemo11StudentDO::getEnabled, reqVO.getEnabled())
                .betweenIfPresent(InfraDemo11StudentDO::getCreateTime, reqVO.getCreateTime())
                .orderByDesc(InfraDemo11StudentDO::getId));
    }

    default List<InfraDemo11StudentDO> selectList(InfraDemo11StudentExportReqVO reqVO) {
        return selectList(new LambdaQueryWrapperX<InfraDemo11StudentDO>()
                .likeIfPresent(InfraDemo11StudentDO::getName, reqVO.getName())
                .eqIfPresent(InfraDemo11StudentDO::getBirthday, reqVO.getBirthday())
                .eqIfPresent(InfraDemo11StudentDO::getSex, reqVO.getSex())
                .eqIfPresent(InfraDemo11StudentDO::getEnabled, reqVO.getEnabled())
                .betweenIfPresent(InfraDemo11StudentDO::getCreateTime, reqVO.getCreateTime())
                .orderByDesc(InfraDemo11StudentDO::getId));
    }

}