package cn.iocoder.yudao.module.ai.dal.mysql.model;

import cn.iocoder.yudao.framework.common.enums.CommonStatusEnum;
import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.mybatis.core.mapper.BaseMapperX;
import cn.iocoder.yudao.framework.mybatis.core.query.LambdaQueryWrapperX;
import cn.iocoder.yudao.module.ai.controller.admin.model.vo.chatRole.AiChatRolePageReqVO;
import cn.iocoder.yudao.module.ai.dal.dataobject.model.AiChatRoleDO;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

/**
 * AI 聊天角色 Mapper
 *
 * @author fansili
 */
@Mapper
public interface AiChatRoleMapper extends BaseMapperX<AiChatRoleDO> {

    default PageResult<AiChatRoleDO> selectPage(AiChatRolePageReqVO reqVO) {
        return selectPage(reqVO, new LambdaQueryWrapperX<AiChatRoleDO>()
                .likeIfPresent(AiChatRoleDO::getName, reqVO.getName())
                .eqIfPresent(AiChatRoleDO::getCategory, reqVO.getCategory())
                .eqIfPresent(AiChatRoleDO::getPublicStatus, reqVO.getPublicStatus())
                .orderByAsc(AiChatRoleDO::getSort));
    }

    default PageResult<AiChatRoleDO> selectPageByMy(AiChatRolePageReqVO reqVO, Long userId) {
        return selectPage(reqVO, new LambdaQueryWrapperX<AiChatRoleDO>()
                .likeIfPresent(AiChatRoleDO::getName, reqVO.getName())
                .eqIfPresent(AiChatRoleDO::getCategory, reqVO.getCategory())
                // 情况一：公开
                .eq(Boolean.TRUE.equals(reqVO.getPublicStatus()), AiChatRoleDO::getPublicStatus, reqVO.getPublicStatus())
                // 情况二：私有
                .eq(Boolean.FALSE.equals(reqVO.getPublicStatus()), AiChatRoleDO::getUserId, userId)
                .eq(Boolean.FALSE.equals(reqVO.getPublicStatus()), AiChatRoleDO::getStatus, CommonStatusEnum.ENABLE.getStatus())
                .orderByAsc(AiChatRoleDO::getSort));
    }

    default List<AiChatRoleDO> selectListGroupByCategory(Integer status) {
        return selectList(new LambdaQueryWrapperX<AiChatRoleDO>()
                .select(AiChatRoleDO::getCategory)
                .eq(AiChatRoleDO::getStatus, status)
                .groupBy(AiChatRoleDO::getCategory));
    }

    default List<AiChatRoleDO> selectListByName(String name) {
        return selectList(new LambdaQueryWrapperX<AiChatRoleDO>()
                .likeIfPresent(AiChatRoleDO::getName, name)
                .orderByAsc(AiChatRoleDO::getSort));
    }

}
