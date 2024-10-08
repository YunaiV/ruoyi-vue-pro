package cn.iocoder.yudao.module.erp.dal.mysql.logistic.customrule;

import java.util.*;

import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.mybatis.core.query.LambdaQueryWrapperX;
import cn.iocoder.yudao.framework.mybatis.core.mapper.BaseMapperX;
import cn.iocoder.yudao.module.erp.dal.dataobject.logistic.customrule.ErpCustomRuleDO;
import org.apache.ibatis.annotations.Mapper;
import cn.iocoder.yudao.module.erp.controller.admin.logistic.customrule.vo.*;

/**
 * ERP 海关规则 Mapper
 *
 * @author 索迈管理员
 */
@Mapper
public interface ErpCustomRuleMapper extends BaseMapperX<ErpCustomRuleDO> {

    default PageResult<ErpCustomRuleDO> selectPage(ErpCustomRulePageReqVO reqVO) {
        return selectPage(reqVO, new LambdaQueryWrapperX<ErpCustomRuleDO>()
                .eqIfPresent(ErpCustomRuleDO::getCountryCode, reqVO.getCountryCode())
                .eqIfPresent(ErpCustomRuleDO::getType, reqVO.getType())
                .eqIfPresent(ErpCustomRuleDO::getSupplierProductId, reqVO.getSupplierProductId())
                .eqIfPresent(ErpCustomRuleDO::getDeclaredTypeEn, reqVO.getDeclaredTypeEn())
                .eqIfPresent(ErpCustomRuleDO::getDeclaredType, reqVO.getDeclaredType())
                .eqIfPresent(ErpCustomRuleDO::getDeclaredValue, reqVO.getDeclaredValue())
                .eqIfPresent(ErpCustomRuleDO::getDeclaredValueCurrencyCode, reqVO.getDeclaredValueCurrencyCode())
                .eqIfPresent(ErpCustomRuleDO::getTaxRate, reqVO.getTaxRate())
                .eqIfPresent(ErpCustomRuleDO::getHscode, reqVO.getHscode())
                .eqIfPresent(ErpCustomRuleDO::getLogisticAttribute, reqVO.getLogisticAttribute())
                .betweenIfPresent(ErpCustomRuleDO::getCreateTime, reqVO.getCreateTime())
                .orderByDesc(ErpCustomRuleDO::getId));
    }

}