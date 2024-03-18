package cn.iocoder.yudao.module.bpm.enums.task;

import cn.hutool.core.util.StrUtil;
import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 流程任务的 Comment 评论类型枚举
 *
 * @author kehaiyou
 */
@Getter
@AllArgsConstructor
public enum BpmCommentTypeEnum {

    APPROVE("1", "审批通过", "审批通过，原因是：{}"),
    REJECT("2", "不通过", "审批不通过：原因是：{}"),
    CANCEL("3", "已取消", "系统自动取消，原因是：{}"),
    RETURN("4", "退回", "任务被退回，原因是：{}"),
    DELEGATE_START("5", "委派发起", "[{}]将任务委派给[{}]，委派理由为:{}"),
    DELEGATE_END("6", "委派完成", "[{}]完成委派任务，任务重新回到[{}]手中，审批建议为:{}"),
    TRANSFER("7", "转派", "[{}]将任务转派给[{}]，转派理由为:{}"),
    ADD_SIGN("8", "加签", "[{}]{}给了[{}]，理由为：{}"),
    SUB_SIGN("9", "减签", "[{}]操作了【减签】,审批人[{}]的任务被取消"),
    ;

    /**
     * 操作类型
     *
     * 由于 BPM Comment 类型为 String，所以这里就不使用 Integer
     */
    private final String type;
    /**
     * 操作名字
     */
    private final String name;
    /**
     * 操作描述
     */
    private final String comment;

    public String formatComment(Object... params) {
         return StrUtil.format(comment, params);
    }

}
