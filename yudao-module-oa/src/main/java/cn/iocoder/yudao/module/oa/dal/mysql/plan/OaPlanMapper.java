package cn.iocoder.yudao.module.oa.dal.mysql.plan;

import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.mybatis.core.mapper.BaseMapperX;
import cn.iocoder.yudao.framework.mybatis.core.query.LambdaQueryWrapperX;
import cn.iocoder.yudao.module.oa.controller.admin.plan.vo.OaPlanPageReqVO;
import cn.iocoder.yudao.module.oa.dal.dataobject.plan.OaPlanDO;
import org.apache.ibatis.annotations.Mapper;

import java.time.LocalDateTime;
import java.util.Collection;
import java.util.List;

/**
 * OA 工作计划 Mapper
 *
 * @author 芋道源码
 */
@Mapper
public interface OaPlanMapper extends BaseMapperX<OaPlanDO> {

    default PageResult<OaPlanDO> selectPage(OaPlanPageReqVO pageReqVO, Long userId) {
        LambdaQueryWrapperX<OaPlanDO> query = new LambdaQueryWrapperX<OaPlanDO>()
                .eq(OaPlanDO::getCreator, userId.toString())
                .likeIfPresent(OaPlanDO::getTitle, pageReqVO.getTitle())
                .likeIfPresent(OaPlanDO::getLabel, pageReqVO.getLabel())
                .eqIfPresent(OaPlanDO::getType, pageReqVO.getType())
                .eqIfPresent(OaPlanDO::getStatus, pageReqVO.getStatus())
                .betweenIfPresent(OaPlanDO::getCreateTime, pageReqVO.getCreateTime());
        query.orderByDesc(OaPlanDO::getCreateTime).orderByDesc(OaPlanDO::getId);
        return selectPage(pageReqVO, query);
    }

    default List<OaPlanDO> selectListByCreatorsAndTypeAndCreateTime(Collection<String> creators,
                                                                   Integer type,
                                                                   LocalDateTime[] createTime) {
        return selectList(new LambdaQueryWrapperX<OaPlanDO>()
                .in(OaPlanDO::getCreator, creators)
                .eq(OaPlanDO::getType, type)
                .betweenIfPresent(OaPlanDO::getCreateTime, createTime)
                .orderByAsc(OaPlanDO::getCreator)
                .orderByDesc(OaPlanDO::getCreateTime)
                .orderByDesc(OaPlanDO::getId));
    }

}
