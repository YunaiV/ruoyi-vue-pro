package cn.iocoder.yudao.module.mes.service.md.autocode;

import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.module.mes.controller.admin.md.autocode.vo.rule.MesMdAutoCodeRulePageReqVO;
import cn.iocoder.yudao.module.mes.controller.admin.md.autocode.vo.rule.MesMdAutoCodeRuleSaveReqVO;
import cn.iocoder.yudao.module.mes.dal.dataobject.md.autocode.MesMdAutoCodeRuleDO;
import jakarta.validation.Valid;

import java.util.List;

/**
 * MES 编码规则 Service 接口
 *
 * @author 芋道源码
 */
public interface MesMdAutoCodeRuleService {

    /**
     * 创建编码规则
     *
     * @param createReqVO 创建信息
     * @return 编号
     */
    Long createAutoCodeRule(@Valid MesMdAutoCodeRuleSaveReqVO createReqVO);

    /**
     * 更新编码规则
     *
     * @param updateReqVO 更新信息
     */
    void updateAutoCodeRule(@Valid MesMdAutoCodeRuleSaveReqVO updateReqVO);

    /**
     * 删除编码规则
     *
     * @param id 编号
     */
    void deleteAutoCodeRule(Long id);

    /**
     * 获得编码规则
     *
     * @param id 编号
     * @return 编码规则
     */
    MesMdAutoCodeRuleDO getAutoCodeRule(Long id);

    /**
     * 获得编码规则分页
     *
     * @param pageReqVO 分页查询
     * @return 编码规则分页
     */
    PageResult<MesMdAutoCodeRuleDO> getAutoCodeRulePage(MesMdAutoCodeRulePageReqVO pageReqVO);

    /**
     * 获得编码规则列表（根据状态）
     *
     * @param status 状态
     * @return 编码规则列表
     */
    List<MesMdAutoCodeRuleDO> getAutoCodeRuleListByStatus(Integer status);

    /**
     * 根据规则编码获取规则
     *
     * @param code 规则编码
     * @return 编码规则
     */
    MesMdAutoCodeRuleDO getAutoCodeRuleByCode(String code);

    /**
     * 校验编码规则是否存在
     *
     * @param id 编号
     */
    void validateAutoCodeRuleExists(Long id);

}
