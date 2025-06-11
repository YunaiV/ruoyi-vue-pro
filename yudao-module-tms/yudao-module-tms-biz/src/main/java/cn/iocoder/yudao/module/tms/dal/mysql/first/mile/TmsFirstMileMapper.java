package cn.iocoder.yudao.module.tms.dal.mysql.first.mile;

import cn.iocoder.yudao.framework.mybatis.core.mapper.BaseMapperX;
import cn.iocoder.yudao.module.tms.dal.dataobject.first.mile.TmsFirstMileDO;
import org.apache.ibatis.annotations.Mapper;

/**
 * 头程申请单 Mapper 接口
 *
 * @author 芋道源码
 */
@Mapper
public interface TmsFirstMileMapper extends BaseMapperX<TmsFirstMileDO> {

    default boolean selectByCode(String code) {
        return selectCount(TmsFirstMileDO::getCode, code) > 0;
    }

    default TmsFirstMileDO selectByCodeRaw(String code) {
        return selectOne(TmsFirstMileDO::getCode, code);
    }

}