package cn.iocoder.yudao.module.infra.dal.mysql.demo12;

import java.util.*;

import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.common.pojo.PageParam;
import cn.iocoder.yudao.framework.mybatis.core.query.LambdaQueryWrapperX;
import cn.iocoder.yudao.framework.mybatis.core.mapper.BaseMapperX;
import cn.iocoder.yudao.module.infra.dal.dataobject.demo12.InfraDemo12StudentContactDO;
import org.apache.ibatis.annotations.Mapper;

/**
 * 学生联系人 Mapper
 *
 * @author 芋道源码
 */
@Mapper
public interface InfraDemo12StudentContactMapper extends BaseMapperX<InfraDemo12StudentContactDO> {

    default PageResult<InfraDemo12StudentContactDO> selectPage(PageParam reqVO, Long studentId) {
        return selectPage(reqVO, new LambdaQueryWrapperX<InfraDemo12StudentContactDO>()
            .eq(InfraDemo12StudentContactDO::getStudentId, studentId)
            .orderByDesc(InfraDemo12StudentContactDO::getId));
    }

    default int deleteByStudentId(Long studentId) {
        return delete(InfraDemo12StudentContactDO::getStudentId, studentId);
    }

}