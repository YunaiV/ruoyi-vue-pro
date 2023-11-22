package cn.iocoder.yudao.module.crm.dal.mysql.permission;

import cn.iocoder.yudao.framework.mybatis.core.mapper.BaseMapperX;
import cn.iocoder.yudao.framework.mybatis.core.query.LambdaQueryWrapperX;
import cn.iocoder.yudao.module.crm.dal.dataobject.permission.CrmPermissionDO;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

/**
 * crm 数据权限 mapper
 *
 * @author HUIHUI
 */
@Mapper
public interface CrmPermissionMapper extends BaseMapperX<CrmPermissionDO> {

    default CrmPermissionDO selectByBizTypeAndBizIdByUserId(Integer bizType, Long bizId, Long userId) {
        return selectOne(new LambdaQueryWrapperX<CrmPermissionDO>()
                .eq(CrmPermissionDO::getBizType, bizType)
                .eq(CrmPermissionDO::getBizId, bizId)
                .eq(CrmPermissionDO::getUserId, userId));
    }

    default List<CrmPermissionDO> selectByBizTypeAndBizId(Integer bizType, Long bizId) {
        return selectList(new LambdaQueryWrapperX<CrmPermissionDO>()
                .eq(CrmPermissionDO::getBizType, bizType)
                .eq(CrmPermissionDO::getBizId, bizId));
    }

    default List<CrmPermissionDO> selectListByBizTypeAndUserId(Integer bizType, Long userId) {
        return selectList(new LambdaQueryWrapperX<CrmPermissionDO>()
                .eq(CrmPermissionDO::getBizType, bizType)
                .eq(CrmPermissionDO::getUserId, userId));
    }

    default List<CrmPermissionDO> selectListByBizTypeAndBizIdAndLevel(Integer bizType, Long bizId, Integer level) {
        return selectList(new LambdaQueryWrapperX<CrmPermissionDO>()
                .eq(CrmPermissionDO::getBizType, bizType)
                .eq(CrmPermissionDO::getBizId, bizId)
                .eq(CrmPermissionDO::getLevel, level));
    }

    default CrmPermissionDO selectByIdAndUserId(Long id, Long userId) {
        return selectOne(new LambdaQueryWrapperX<CrmPermissionDO>()
                .eq(CrmPermissionDO::getId, id).eq(CrmPermissionDO::getUserId, userId));
    }

}
