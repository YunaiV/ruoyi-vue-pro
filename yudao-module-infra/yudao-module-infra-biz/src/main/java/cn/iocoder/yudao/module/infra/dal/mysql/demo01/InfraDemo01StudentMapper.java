package cn.iocoder.yudao.module.infra.dal.mysql.demo01;

import java.util.*;

import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.mybatis.core.query.LambdaQueryWrapperX;
import cn.iocoder.yudao.framework.mybatis.core.mapper.BaseMapperX;
import cn.iocoder.yudao.module.infra.dal.dataobject.demo01.InfraDemo01StudentDO;
import org.apache.ibatis.annotations.Mapper;
import cn.iocoder.yudao.module.infra.controller.admin.demo01.vo.*;

/**
 * 学生 Mapper
 *
 * @author 芋道源码
 */
@Mapper
public interface InfraDemo01StudentMapper extends BaseMapperX<InfraDemo01StudentDO> {

    default PageResult<InfraDemo01StudentDO> selectPage(InfraDemo01StudentPageReqVO reqVO) {
        return selectPage(reqVO, new LambdaQueryWrapperX<InfraDemo01StudentDO>()
                .likeIfPresent(InfraDemo01StudentDO::getName, reqVO.getName())
                .eqIfPresent(InfraDemo01StudentDO::getBirthday, reqVO.getBirthday())
                .eqIfPresent(InfraDemo01StudentDO::getSex, reqVO.getSex())
                .eqIfPresent(InfraDemo01StudentDO::getEnabled, reqVO.getEnabled())
                .betweenIfPresent(InfraDemo01StudentDO::getCreateTime, reqVO.getCreateTime())
                .orderByDesc(InfraDemo01StudentDO::getId));
    }

    default List<InfraDemo01StudentDO> selectList(InfraDemo01StudentExportReqVO reqVO) {
        return selectList(new LambdaQueryWrapperX<InfraDemo01StudentDO>()
                .likeIfPresent(InfraDemo01StudentDO::getName, reqVO.getName())
                .eqIfPresent(InfraDemo01StudentDO::getBirthday, reqVO.getBirthday())
                .eqIfPresent(InfraDemo01StudentDO::getSex, reqVO.getSex())
                .eqIfPresent(InfraDemo01StudentDO::getEnabled, reqVO.getEnabled())
                .betweenIfPresent(InfraDemo01StudentDO::getCreateTime, reqVO.getCreateTime())
                .orderByDesc(InfraDemo01StudentDO::getId));
    }

}