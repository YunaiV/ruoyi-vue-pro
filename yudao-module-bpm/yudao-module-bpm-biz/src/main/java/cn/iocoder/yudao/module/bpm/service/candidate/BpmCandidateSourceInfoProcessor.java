package cn.iocoder.yudao.module.bpm.service.candidate;

import cn.hutool.core.collection.CollUtil;
import cn.iocoder.yudao.module.bpm.controller.admin.candidate.vo.BpmTaskCandidateVO;
import cn.iocoder.yudao.module.bpm.enums.definition.BpmTaskAssignRuleTypeEnum;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

public interface BpmCandidateSourceInfoProcessor {
    /**
     * 获取该处理器支持的类型
     * 来自 {@link BpmTaskAssignRuleTypeEnum}
     *
     * @return
     */
    Set<Integer> getSupportedTypes();

    /**
     * 对规则和人员做校验
     *
     * @param type    规则
     * @param options 人员id
     */
    void validRuleOptions(Integer type, Set<Long> options);

    /**
     * 默认的处理
     * 如果想去操作所有的规则，则可以覆盖此方法
     *
     * @param request
     * @param chain
     * @return 必须包含的是用户ID，而不是其他的ID
     * @throws Exception
     */
    default Set<Long> process(BpmCandidateSourceInfo request, BpmCandidateSourceInfoProcessorChain chain) throws Exception {
        Set<BpmTaskCandidateVO> rules = request.getRules();
        Set<Long> results = new HashSet<>();
        for (BpmTaskCandidateVO rule : rules) {
            // 每个处理器都有机会处理自己支持的事件
            if (CollUtil.contains(getSupportedTypes(), rule.getType())) {
                results.addAll(doProcess(request, rule));
            }
        }
        return results;
    }

    default Set<Long> doProcess(BpmCandidateSourceInfo request, BpmTaskCandidateVO rule) {
        return Collections.emptySet();
    }
}