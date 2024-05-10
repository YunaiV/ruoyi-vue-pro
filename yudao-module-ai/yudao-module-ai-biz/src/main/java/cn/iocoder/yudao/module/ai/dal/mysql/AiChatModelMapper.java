package cn.iocoder.yudao.module.ai.dal.mysql;

import cn.hutool.core.collection.CollUtil;
import cn.iocoder.yudao.framework.common.pojo.PageParam;
import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.mybatis.core.mapper.BaseMapperX;
import cn.iocoder.yudao.framework.mybatis.core.query.LambdaQueryWrapperX;
import cn.iocoder.yudao.module.ai.controller.admin.model.vo.chatModel.AiChatModelPageReqVO;
import cn.iocoder.yudao.module.ai.dal.dataobject.model.AiChatModelDO;
import org.apache.ibatis.annotations.Mapper;

/**
 * API 聊天模型 Mapper
 *
 * @author fansili
 */
@Mapper
public interface AiChatModelMapper extends BaseMapperX<AiChatModelDO> {

    // TODO 芋艿：要搞一下
    /**
     * 查询 - 第一个modal
     *
     * @return
     */
    default AiChatModelDO selectFirstModal() {
        PageResult<AiChatModelDO> pageResult = selectPage(new PageParam().setPageNo(1).setPageSize(1),
                new LambdaQueryWrapperX<AiChatModelDO>()
                        .orderByAsc(AiChatModelDO::getSort)
        );
        if (CollUtil.isEmpty(pageResult.getList())) {
            return null;
        }
        return pageResult.getList().get(0);
    }

    default PageResult<AiChatModelDO> selectPage(AiChatModelPageReqVO reqVO) {
        return selectPage(reqVO, new LambdaQueryWrapperX<AiChatModelDO>()
                .likeIfPresent(AiChatModelDO::getName, reqVO.getName())
                .eqIfPresent(AiChatModelDO::getModel, reqVO.getModel())
                .eqIfPresent(AiChatModelDO::getPlatform, reqVO.getPlatform())
                .orderByDesc(AiChatModelDO::getId));
    }

}
