package cn.iocoder.yudao.module.crm.dal.mysql.permission;

import cn.iocoder.yudao.framework.mybatis.core.mapper.BaseMapperX;
import cn.iocoder.yudao.framework.mybatis.core.query.LambdaQueryWrapperX;
import cn.iocoder.yudao.module.crm.dal.dataobject.permission.CrmPermissionDO;
import cn.iocoder.yudao.module.crm.framework.enums.CrmBizTypeEnum;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

/**
 * crm 数据权限 mapper
 *
 * @author HUIHUI
 */
@Mapper
public interface CrmPermissionMapper extends BaseMapperX<CrmPermissionDO> {

    /**
     * 获取用户数据权限通过 数据类型 x 某个数据 x 用户编号
     *
     * @param bizType 数据类型，关联 {@link CrmBizTypeEnum}
     * @param bizId   数据编号，关联 {@link CrmBizTypeEnum} 对应模块 DO#getId()
     * @param userId  用户编号，AdminUser#id
     * @return Crm 数据权限
     */
    default CrmPermissionDO selectByBizTypeAndBizIdByUserId(Integer bizType, Long bizId, Long userId) {
        return selectOne(new LambdaQueryWrapperX<CrmPermissionDO>()
                .eq(CrmPermissionDO::getBizType, bizType)
                .eq(CrmPermissionDO::getBizId, bizId)
                .eq(CrmPermissionDO::getUserId, userId));
    }

    /**
     * 获取数据权限列表通过 数据类型 x 某个数据
     *
     * @param bizType 数据类型，关联 {@link CrmBizTypeEnum}
     * @param bizId   数据编号，关联 {@link CrmBizTypeEnum} 对应模块 DO#getId()
     * @return Crm 数据权限列表
     */
    default List<CrmPermissionDO> selectByBizTypeAndBizId(Integer bizType, Long bizId) {
        return selectList(new LambdaQueryWrapperX<CrmPermissionDO>()
                .eq(CrmPermissionDO::getBizType, bizType)
                .eq(CrmPermissionDO::getBizId, bizId));
    }

}
