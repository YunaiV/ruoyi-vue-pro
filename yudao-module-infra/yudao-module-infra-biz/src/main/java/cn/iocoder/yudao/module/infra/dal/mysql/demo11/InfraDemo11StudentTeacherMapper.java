package cn.iocoder.yudao.module.infra.dal.mysql.demo11;

import java.util.*;

import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.common.pojo.PageParam;
import cn.iocoder.yudao.framework.mybatis.core.query.LambdaQueryWrapperX;
import cn.iocoder.yudao.framework.mybatis.core.mapper.BaseMapperX;
import cn.iocoder.yudao.module.infra.dal.dataobject.demo11.InfraDemo11StudentTeacherDO;
import org.apache.ibatis.annotations.Mapper;

/**
 * 学生班主任 Mapper
 *
 * @author 芋道源码
 */
@Mapper
public interface InfraDemo11StudentTeacherMapper extends BaseMapperX<InfraDemo11StudentTeacherDO> {

    default InfraDemo11StudentTeacherDO selectByStudentId(Long studentId) {
        return selectOne(InfraDemo11StudentTeacherDO::getStudentId, studentId);
    }

    default int deleteByStudentId(Long studentId) {
        return delete(InfraDemo11StudentTeacherDO::getStudentId, studentId);
    }

}