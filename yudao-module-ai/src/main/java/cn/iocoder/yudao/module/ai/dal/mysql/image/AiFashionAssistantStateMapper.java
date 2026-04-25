package cn.iocoder.yudao.module.ai.dal.mysql.image;

import cn.iocoder.yudao.framework.mybatis.core.mapper.BaseMapperX;
import cn.iocoder.yudao.framework.mybatis.core.query.LambdaQueryWrapperX;
import cn.iocoder.yudao.module.ai.dal.dataobject.image.AiFashionAssistantStateDO;
import org.apache.ibatis.annotations.Mapper;

/**
 * AI 悬浮对话框页面状态 Mapper
 *
 * @author deepay
 */
@Mapper
public interface AiFashionAssistantStateMapper extends BaseMapperX<AiFashionAssistantStateDO> {

    default AiFashionAssistantStateDO selectByUserIdAndPage(Long userId, String pageName) {
        return selectOne(new LambdaQueryWrapperX<AiFashionAssistantStateDO>()
                .eq(AiFashionAssistantStateDO::getUserId, userId)
                .eq(AiFashionAssistantStateDO::getPageName, pageName));
    }

}
