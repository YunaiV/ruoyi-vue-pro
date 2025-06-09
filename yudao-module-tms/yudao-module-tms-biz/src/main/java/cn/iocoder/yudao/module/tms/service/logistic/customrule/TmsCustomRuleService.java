package cn.iocoder.yudao.module.tms.service.logistic.customrule;

import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.module.tms.controller.admin.logistic.customrule.vo.TmsCustomRulePageReqVO;
import cn.iocoder.yudao.module.tms.controller.admin.logistic.customrule.vo.TmsCustomRuleSaveReqVO;
import cn.iocoder.yudao.module.tms.dal.dataobject.logistic.customrule.TmsCustomRuleDO;
import cn.iocoder.yudao.module.tms.service.logistic.customrule.bo.TmsCustomRuleBO;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;

import java.util.Collection;
import java.util.List;


/**
 * ERP 海关规则 Service 接口
 *
 * @author 索迈管理员
 */
public interface TmsCustomRuleService {

    /**
     * 创建ERP 海关规则
     *
     * @param createReqVO 创建信息
     * @return 编号
     */
    Long createCustomRule(@Valid TmsCustomRuleSaveReqVO createReqVO);

    /**
     * 更新ERP 海关规则
     *
     * @param updateReqVO 更新信息
     */
    void updateCustomRule(@Valid TmsCustomRuleSaveReqVO updateReqVO);

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
    TmsCustomRuleDO getCustomRule(@NotNull Long id);

    /**
     * 获得ERP 海关规则分页
     *
     * @param pageReqVO 分页查询
     * @return ERP 海关规则分页
     */
    PageResult<TmsCustomRuleDO> getCustomRulePage(TmsCustomRulePageReqVO pageReqVO);


    /**
     * 获得海关规则BO
     *
     * @param id 海关规则id
     * @return 海关规则BO
     */
    TmsCustomRuleBO getCustomRuleBOById(@NotNull Long id);

    /**
     * 获得海关规则BO List
     *
     * @param ids 海关规则id list,为null则拿所有。
     * @return 海关规则BO list
     */
    List<TmsCustomRuleBO> getCustomRuleBOList(Collection<Long> ids);

    /**
     * 获得海关规则BO分页
     *
     * @param pageReqVO 海关规则分页查询
     * @return 海关规则BO分页
     */
    PageResult<TmsCustomRuleBO> getCustomRuleBOPage(TmsCustomRulePageReqVO pageReqVO);

    /**
     * 根据国别和产品ID集合获得海关规则列表
     *
     * @param country    国别
     * @param productIds 产品ID集合
     * @return 海关规则列表
     */
    List<TmsCustomRuleDO> getCustomRuleListByCountryAndProducts(Long countryCode, List<Long> productIds);
}