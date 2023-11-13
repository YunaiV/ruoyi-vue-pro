package cn.iocoder.yudao.module.infra.dal.mysql.demo12;

import java.util.*;

import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.common.pojo.PageParam;
import cn.iocoder.yudao.framework.mybatis.core.query.LambdaQueryWrapperX;
import cn.iocoder.yudao.framework.mybatis.core.mapper.BaseMapperX;
import cn.iocoder.yudao.module.infra.dal.dataobject.demo12.InfraDemo12StudentTeacherDO;
import org.apache.ibatis.annotations.Mapper;

/**
 * 学生班主任 Mapper
 *
 * @author 芋道源码
 */
@Mapper
public interface InfraDemo12StudentTeacherMapper extends BaseMapperX<InfraDemo12StudentTeacherDO> {

    default InfraDemo12StudentTeacherDO selectByStudentId(Long studentId) {
        return selectOne(InfraDemo12StudentTeacherDO::getStudentId, studentId);
    }

    default int deleteByStudentId(Long studentId) {
        return delete(InfraDemo12StudentTeacherDO::getStudentId, studentId);
    }

}