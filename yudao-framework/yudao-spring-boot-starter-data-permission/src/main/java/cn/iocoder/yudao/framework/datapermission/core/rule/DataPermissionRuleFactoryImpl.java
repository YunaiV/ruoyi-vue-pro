package cn.iocoder.yudao.framework.datapermission.core.rule;

import lombok.RequiredArgsConstructor;

import java.util.List;

@RequiredArgsConstructor
public class DataPermissionRuleFactoryImpl implements DataPermissionRuleFactory {

    /**
     * 数据权限规则数组
     */
    private final List<DataPermissionRule> rules;

    @Override
    public List<DataPermissionRule> getDataPermissionRules() {
        return rules;
    }

    @Override
    public List<DataPermissionRule> getDataPermissionRule(String mappedStatementId) {
        return null;
    }

}
