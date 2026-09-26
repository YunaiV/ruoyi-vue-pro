package cn.iocoder.yudao.module.ai.dal.mysql.billing;

import cn.iocoder.yudao.framework.common.enums.CommonStatusEnum;
import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.mybatis.core.mapper.BaseMapperX;
import cn.iocoder.yudao.framework.mybatis.core.query.LambdaQueryWrapperX;
import cn.iocoder.yudao.module.ai.controller.admin.model.vo.pricing.AiModelPricingPageReqVO;
import cn.iocoder.yudao.module.ai.dal.dataobject.billing.AiModelPricingDO;
import org.apache.ibatis.annotations.Mapper;

/**
 * AI 模型计费配置 Mapper
 *
 * @author 芋道源码
 */
@Mapper
public interface AiModelPricingMapper extends BaseMapperX<AiModelPricingDO> {

    /**
     * 查询指定模型最新的启用计费配置
     *
     * @param modelId 模型编号
     * @return 计费配置，不存在返回 null
     */
    default AiModelPricingDO selectLatestByModelId(Long modelId) {
        return selectOne(new LambdaQueryWrapperX<AiModelPricingDO>()
                .eq(AiModelPricingDO::getModelId, modelId)
                .eq(AiModelPricingDO::getStatus, CommonStatusEnum.ENABLE.getStatus())
                .orderByDesc(AiModelPricingDO::getCreateTime)
                .last("LIMIT 1"));
    }

    default PageResult<AiModelPricingDO> selectPage(AiModelPricingPageReqVO reqVO) {
        return selectPage(reqVO, new LambdaQueryWrapperX<AiModelPricingDO>()
                .eqIfPresent(AiModelPricingDO::getModelId, reqVO.getModelId())
                .eqIfPresent(AiModelPricingDO::getStatus, reqVO.getStatus())
                .orderByDesc(AiModelPricingDO::getId));
    }

}
