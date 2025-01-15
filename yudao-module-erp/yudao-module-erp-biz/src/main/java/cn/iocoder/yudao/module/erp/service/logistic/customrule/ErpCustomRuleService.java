package cn.iocoder.yudao.module.erp.service.logistic.customrule;

import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.module.erp.api.product.dto.ErpCustomRuleDTO;
import cn.iocoder.yudao.module.erp.controller.admin.logistic.customrule.vo.ErpCustomRulePageReqVO;
import cn.iocoder.yudao.module.erp.controller.admin.logistic.customrule.vo.ErpCustomRuleSaveReqVO;
import cn.iocoder.yudao.module.erp.dal.dataobject.logistic.customrule.ErpCustomRuleDO;
import jakarta.validation.Valid;

import java.util.List;

/**
 * ERP 海关规则 Service 接口
 *
 * @author 索迈管理员
 */
public interface ErpCustomRuleService {

    /**
     * 创建ERP 海关规则
     *
     * @param createReqVO 创建信息
     * @return 编号
     */
    Long createCustomRule(@Valid ErpCustomRuleSaveReqVO createReqVO);

    /**
     * 更新ERP 海关规则
     *
     * @param updateReqVO 更新信息
     */
    void updateCustomRule(@Valid ErpCustomRuleSaveReqVO updateReqVO);

    /**
     * 删除ERP 海关规则
     *
     * @param id 编号
     */
    void deleteCustomRule(Long id);

    /**
     * 获得ERP 海关规则
     *
     * @param id 编号
     * @return ERP 海关规则
     */
    ErpCustomRuleDO getCustomRule(Long id);

    /**
     * 获得ERP 海关规则分页
     *
     * @param pageReqVO 分页查询
     * @return ERP 海关规则分页
     */
    PageResult<ErpCustomRuleDO> getCustomRulePage(ErpCustomRulePageReqVO pageReqVO);

    /**
     * 海关规则列表DO -> 海关规则列表DTO
     *
     * @param customRuleDO 海关规则列表DO
     * @return ErpCustomRuleDTO 海关规则列表DTO
     */
    ErpCustomRuleDTO convertToDTO(ErpCustomRuleDO customRuleDO);

    /**
     * 海关规则列表集合 DOs-> 海关规则列表集合 DTOs
     *
     * @param customRuleDOList 海关规则列表List<ErpCustomRuleDTO>
     * @return ErpCustomRuleDTO 海关规则列表List<ErpCustomRuleDO>
     */
    List<ErpCustomRuleDTO> convertToDTOList(List<ErpCustomRuleDO> customRuleDOList);
}