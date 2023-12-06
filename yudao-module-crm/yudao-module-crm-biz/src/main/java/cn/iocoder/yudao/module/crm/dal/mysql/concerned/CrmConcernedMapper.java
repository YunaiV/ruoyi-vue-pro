package cn.iocoder.yudao.module.crm.dal.mysql.concerned;

import cn.iocoder.yudao.framework.mybatis.core.mapper.BaseMapperX;
import cn.iocoder.yudao.framework.mybatis.core.query.LambdaQueryWrapperX;
import cn.iocoder.yudao.module.crm.dal.dataobject.concerned.CrmConcernedDO;
import cn.iocoder.yudao.module.crm.enums.common.CrmBizTypeEnum;
import org.apache.ibatis.annotations.Mapper;

import java.util.Collection;
import java.util.List;

/**
 * CRM 关注的数据 Mapper
 *
 * @author HUIHUI
 */
@Mapper
public interface CrmConcernedMapper extends BaseMapperX<CrmConcernedDO> {

    /**
     * 查询用户关注的数据
     *
     * @param bizType 数据类型，关联 {@link CrmBizTypeEnum}
     * @param bizIds  数据编号，关联 {@link CrmBizTypeEnum} 对应模块 DO#getId()
     * @param userId  用户编号
     * @return 关注的数据
     */
    default List<CrmConcernedDO> selectList(Integer bizType, Collection<Long> bizIds, Long userId) {
        return selectList(new LambdaQueryWrapperX<CrmConcernedDO>()
                .eq(CrmConcernedDO::getBizType, bizType)
                .in(CrmConcernedDO::getBizId, bizIds)
                .eq(CrmConcernedDO::getUserId, userId));
    }

}