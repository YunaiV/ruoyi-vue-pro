package cn.iocoder.yudao.module.iot.service.rule.data;

import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.common.util.object.BeanUtils;
import cn.iocoder.yudao.module.iot.controller.admin.rule.vo.data.rule.IotDataRulePageReqVO;
import cn.iocoder.yudao.module.iot.controller.admin.rule.vo.data.rule.IotDataRuleSaveReqVO;
import cn.iocoder.yudao.module.iot.dal.dataobject.rule.IotDataRuleDO;
import cn.iocoder.yudao.module.iot.dal.mysql.rule.IotDataRuleMapper;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Service;
import org.springframework.validation.annotation.Validated;

import static cn.iocoder.yudao.framework.common.exception.util.ServiceExceptionUtil.exception;
import static cn.iocoder.yudao.module.iot.enums.ErrorCodeConstants.DATA_RULE_NOT_EXISTS;

/**
 * IoT 数据流转规则 Service 实现类
 *
 * @author 芋道源码
 */
@Service
@Validated
public class IotDataRuleServiceImpl implements IotDataRuleService {

    @Resource
    private IotDataRuleMapper dataRuleMapper;

    @Override
    public Long createDataRule(IotDataRuleSaveReqVO createReqVO) {
        IotDataRuleDO dataRule = BeanUtils.toBean(createReqVO, IotDataRuleDO.class);
        dataRuleMapper.insert(dataRule);
        return dataRule.getId();
    }

    @Override
    public void updateDataRule(IotDataRuleSaveReqVO updateReqVO) {
        // 校验存在
        validateDataRuleExists(updateReqVO.getId());
        // 更新
        IotDataRuleDO updateObj = BeanUtils.toBean(updateReqVO, IotDataRuleDO.class);
        dataRuleMapper.updateById(updateObj);
    }

    @Override
    public void deleteDataRule(Long id) {
        // 校验存在
        validateDataRuleExists(id);
        // 删除
        dataRuleMapper.deleteById(id);
    }

    private void validateDataRuleExists(Long id) {
        if (dataRuleMapper.selectById(id) == null) {
            throw exception(DATA_RULE_NOT_EXISTS);
        }
    }

    @Override
    public IotDataRuleDO getDataRule(Long id) {
        return dataRuleMapper.selectById(id);
    }

    @Override
    public PageResult<IotDataRuleDO> getDataRulePage(IotDataRulePageReqVO pageReqVO) {
        return dataRuleMapper.selectPage(pageReqVO);
    }

}