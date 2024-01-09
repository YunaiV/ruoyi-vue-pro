package cn.iocoder.yudao.module.bpm.service.candidate;

import cn.iocoder.yudao.module.bpm.controller.admin.candidate.vo.BpmTaskCandidateVO;
import cn.iocoder.yudao.module.bpm.controller.admin.definition.vo.rule.BpmTaskAssignRuleBaseVO;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.flowable.engine.delegate.DelegateExecution;

import java.util.HashSet;
import java.util.Set;

/**
 * 获取候选人信息
 */
@AllArgsConstructor
@NoArgsConstructor
@Data
public class BpmCandidateSourceInfo {

    @Schema(description = "流程id")
    private String processInstanceId;

    @Schema(description = "当前任务ID")
    private String taskId;

    /**
     * 通过这些规则，生成最终需要生成的用户
     */
    @Schema(description = "当前任务预选规则")
    private Set<BpmTaskCandidateVO> rules;

    @Schema(description = "源执行流程")
    private DelegateExecution execution;

    public void addRule(BpmTaskCandidateVO vo) {
        assert vo != null;
        if (rules == null) {
            rules = new HashSet<>();
        }
        rules.add(vo);
    }
}