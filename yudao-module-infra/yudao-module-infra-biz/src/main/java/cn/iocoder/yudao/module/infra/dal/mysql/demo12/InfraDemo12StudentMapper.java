package cn.iocoder.yudao.module.infra.dal.mysql.demo12;

import java.util.*;

import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.mybatis.core.query.LambdaQueryWrapperX;
import cn.iocoder.yudao.framework.mybatis.core.mapper.BaseMapperX;
import cn.iocoder.yudao.module.infra.dal.dataobject.demo12.InfraDemo12StudentDO;
import org.apache.ibatis.annotations.Mapper;
import cn.iocoder.yudao.module.infra.controller.admin.demo12.vo.*;

/**
 * 学生 Mapper
 *
 * @author 芋道源码
 */
@Mapper
public interface InfraDemo12StudentMapper extends BaseMapperX<InfraDemo12StudentDO> {

    default PageResult<InfraDemo12StudentDO> selectPage(InfraDemo12StudentPageReqVO reqVO) {
        return selectPage(reqVO, new LambdaQueryWrapperX<InfraDemo12StudentDO>()
                .likeIfPresent(InfraDemo12StudentDO::getName, reqVO.getName())
                .eqIfPresent(InfraDemo12StudentDO::getBirthday, reqVO.getBirthday())
                .eqIfPresent(InfraDemo12StudentDO::getSex, reqVO.getSex())
                .eqIfPresent(InfraDemo12StudentDO::getEnabled, reqVO.getEnabled())
                .betweenIfPresent(InfraDemo12StudentDO::getCreateTime, reqVO.getCreateTime())
                .orderByDesc(InfraDemo12StudentDO::getId));
    }

    default List<InfraDemo12StudentDO> selectList(InfraDemo12StudentExportReqVO reqVO) {
        return selectList(new LambdaQueryWrapperX<InfraDemo12StudentDO>()
                .likeIfPresent(InfraDemo12StudentDO::getName, reqVO.getName())
                .eqIfPresent(InfraDemo12StudentDO::getBirthday, reqVO.getBirthday())
                .eqIfPresent(InfraDemo12StudentDO::getSex, reqVO.getSex())
                .eqIfPresent(InfraDemo12StudentDO::getEnabled, reqVO.getEnabled())
                .betweenIfPresent(InfraDemo12StudentDO::getCreateTime, reqVO.getCreateTime())
                .orderByDesc(InfraDemo12StudentDO::getId));
    }

}