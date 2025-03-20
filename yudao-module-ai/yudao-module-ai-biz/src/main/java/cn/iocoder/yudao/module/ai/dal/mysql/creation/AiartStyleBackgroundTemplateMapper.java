package cn.iocoder.yudao.module.ai.dal.mysql.creation;

import cn.iocoder.yudao.framework.common.enums.CommonStatusEnum;
import cn.iocoder.yudao.framework.mybatis.core.mapper.BaseMapperX;
import cn.iocoder.yudao.framework.mybatis.core.query.LambdaQueryWrapperX;
import cn.iocoder.yudao.module.ai.controller.admin.creation.vo.style.BackgroundTemplateReqVO;
import cn.iocoder.yudao.module.ai.dal.dataobject.creation.AiartStyleBackgroundTemplateDO;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

/**
 * AI style分隔 Mapper
 *
 * @author 芋道源码
 */
@Mapper
public interface AiartStyleBackgroundTemplateMapper extends BaseMapperX<AiartStyleBackgroundTemplateDO> {

    /**
     * 获取有效的商品后端模版
     * @param reqVO 请求对象
     * @return 结果
     */
    default List<AiartStyleBackgroundTemplateDO> selectList(BackgroundTemplateReqVO reqVO) {
        return selectList(new LambdaQueryWrapperX<AiartStyleBackgroundTemplateDO>()
                .eqIfPresent(AiartStyleBackgroundTemplateDO::getLevelFirst,reqVO.getFistLevelName())
                .eqIfPresent(AiartStyleBackgroundTemplateDO::getLevelSecond,reqVO.getSecondLevelName())
                .eqIfPresent(AiartStyleBackgroundTemplateDO::getStatus, CommonStatusEnum.ENABLE.getStatus())
                .orderByDesc(AiartStyleBackgroundTemplateDO::getId));
    }

}