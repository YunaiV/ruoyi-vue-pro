package cn.iocoder.yudao.module.infra.dal.mysql.demo02;

import java.util.*;

import cn.iocoder.yudao.framework.mybatis.core.mapper.BaseMapperX;
import cn.iocoder.yudao.module.infra.dal.dataobject.demo02.InfraDemoStudentAddressDO;
import org.apache.ibatis.annotations.Mapper;

/**
 * 学生地址 Mapper
 *
 * @author 芋道源码
 */
@Mapper
public interface InfraDemoStudentAddressMapper extends BaseMapperX<InfraDemoStudentAddressDO> {

    default InfraDemoStudentAddressDO selectByStudentId(Long studentId) {
        return selectOne(InfraDemoStudentAddressDO::getStudentId, studentId);
    }

    default List<InfraDemoStudentAddressDO> selectListByStudentId(List<Long> studentIds) {
        return selectList(InfraDemoStudentAddressDO::getStudentId, studentIds);
    }

    default int deleteByStudentId(Long studentId) {
        return delete(InfraDemoStudentAddressDO::getStudentId, studentId);
    }

}