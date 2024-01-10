package cn.iocoder.yudao.module.bpm.service.candidate;

import cn.iocoder.yudao.module.bpm.controller.admin.candidate.vo.BpmTaskCandidateRuleVO;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;
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
    @NotNull
    private String processInstanceId;

    @Schema(description = "当前任务ID")
    @NotNull
    private String taskId;
    /**
     * 通过这些规则，生成最终需要生成的用户
     */
    @Schema(description = "当前任务预选规则")
    @NotEmpty(message = "不允许空规则")
    private Set<BpmTaskCandidateRuleVO> rules;

    @Schema(description = "发起抄送的用户")
    private String creator;

    public void addRule(BpmTaskCandidateRuleVO vo) {
        assert vo != null;
        if (rules == null) {
            rules = new HashSet<>();
        }
        rules.add(vo);
    }
}