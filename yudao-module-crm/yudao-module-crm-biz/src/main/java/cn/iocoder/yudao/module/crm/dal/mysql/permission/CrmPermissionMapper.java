package cn.iocoder.yudao.module.crm.dal.mysql.permission;

import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.mybatis.core.mapper.BaseMapperX;
import cn.iocoder.yudao.framework.mybatis.core.query.LambdaQueryWrapperX;
import cn.iocoder.yudao.module.crm.dal.dataobject.permission.CrmPermissionDO;
import cn.iocoder.yudao.module.crm.framework.enums.CrmBizTypeEnum;
import cn.iocoder.yudao.module.crm.service.permission.bo.CrmPermissionPageReqBO;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

/**
 * crm 数据权限 mapper
 *
 * @author HUIHUI
 */
@Mapper
public interface CrmPermissionMapper extends BaseMapperX<CrmPermissionDO> {

    // TODO @puhui999：是不是不用谢这个注释；因为方法名，可以自解释；
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

    // TODO @puhui999：是不是不用谢这个注释；因为方法名，可以自解释；
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

    default PageResult<CrmPermissionDO> selectPage(CrmPermissionPageReqBO pageReqBO) {
        return selectPage(pageReqBO, new LambdaQueryWrapperX<CrmPermissionDO>()
                .eq(CrmPermissionDO::getBizType, pageReqBO.getBizType())
                .eq(CrmPermissionDO::getUserId, pageReqBO.getUserId())); // 只要是团队成员都有读取的权限
    }

}
