package cn.iocoder.yudao.module.ai.dal.mysql.model;

import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.mybatis.core.mapper.BaseMapperX;
import cn.iocoder.yudao.framework.mybatis.core.query.LambdaQueryWrapperX;
import cn.iocoder.yudao.framework.mybatis.core.query.QueryWrapperX;
import cn.iocoder.yudao.module.ai.controller.admin.model.vo.chatModel.AiChatModelPageReqVO;
import cn.iocoder.yudao.module.ai.dal.dataobject.model.AiChatModelDO;
import org.apache.ibatis.annotations.Mapper;

import java.util.Collection;
import java.util.List;

/**
 * API 聊天模型 Mapper
 *
 * @author fansili
 */
@Mapper
public interface AiChatModelMapper extends BaseMapperX<AiChatModelDO> {

    default AiChatModelDO selectFirstByStatus(Integer status) {
        return selectOne(new QueryWrapperX<AiChatModelDO>()
                .eq("status", status)
                .limitN(1)
                .orderByAsc("sort"));
    }

    default PageResult<AiChatModelDO> selectPage(AiChatModelPageReqVO reqVO) {
        return selectPage(reqVO, new LambdaQueryWrapperX<AiChatModelDO>()
                .likeIfPresent(AiChatModelDO::getName, reqVO.getName())
                .eqIfPresent(AiChatModelDO::getModel, reqVO.getModel())
                .eqIfPresent(AiChatModelDO::getPlatform, reqVO.getPlatform())
                .orderByAsc(AiChatModelDO::getSort));
    }

    default List<AiChatModelDO> selectList(Integer status) {
        return selectList(new LambdaQueryWrapperX<AiChatModelDO>()
                .eq(AiChatModelDO::getStatus, status)
                .orderByAsc(AiChatModelDO::getSort));
    }

}
