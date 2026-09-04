package cn.iocoder.yudao.module.ai.dal.mysql.billing;

import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.mybatis.core.mapper.BaseMapperX;
import cn.iocoder.yudao.framework.mybatis.core.query.LambdaQueryWrapperX;
import cn.iocoder.yudao.module.ai.controller.admin.model.vo.budget.AiBudgetConfigPageReqVO;
import cn.iocoder.yudao.module.ai.dal.dataobject.billing.AiBudgetConfigDO;
import org.apache.ibatis.annotations.Mapper;

/**
 * AI 预算配置 Mapper
 *
 * @author 芋道源码
 */
@Mapper
public interface AiBudgetConfigMapper extends BaseMapperX<AiBudgetConfigDO> {

    default AiBudgetConfigDO selectByUserAndPeriod(Long userId, String periodType) {
        return selectOne(new LambdaQueryWrapperX<AiBudgetConfigDO>()
                .eq(AiBudgetConfigDO::getUserId, userId)
                .eq(AiBudgetConfigDO::getPeriodType, periodType));
    }

    default PageResult<AiBudgetConfigDO> selectPage(AiBudgetConfigPageReqVO reqVO) {
        return selectPage(reqVO, new LambdaQueryWrapperX<AiBudgetConfigDO>()
                .eqIfPresent(AiBudgetConfigDO::getUserId, reqVO.getUserId())
                .eqIfPresent(AiBudgetConfigDO::getPeriodType, reqVO.getPeriodType())
                .eqIfPresent(AiBudgetConfigDO::getStatus, reqVO.getStatus())
                .orderByDesc(AiBudgetConfigDO::getId));
    }

}
