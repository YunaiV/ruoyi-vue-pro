package cn.iocoder.yudao.module.pms.enums.pm.workitem;

import cn.hutool.core.util.StrUtil;
import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * PMS 工作项动态内容枚举
 *
 * @author 芋道源码
 */
@Getter
@AllArgsConstructor
public enum PmsWorkItemActivityContentEnum {

    WORK_ITEM_CREATED("创建了工作项"),
    WORK_ITEM_FIELD_UPDATED("将{}从「{}」修改为「{}」"),
    WORK_ITEM_DESCRIPTION_UPDATED("更新了工作项描述"),
    WORK_ITEM_LABELS_UPDATED("将标签从「{}」修改为「{}」"),
    WORK_ITEM_MEMBERS_UPDATED("将参与人从「{}」修改为「{}」"),
    WORK_ITEM_ATTACHMENTS_UPDATED("将附件从「{}」修改为「{}」"),
    WORK_ITEM_PARENT_UPDATED("将父工作项从「{}」修改为「{}」"),
    WORK_ITEM_NAME_UPDATED("将工作项名称从「{}」修改为「{}」"),
    WORK_ITEM_STATUS_UPDATED("将状态从「{}」修改为「{}」"),
    WORK_ITEM_MOVED_TO_BACKLOG("将工作项移回待规划"),
    WORK_ITEM_PLANNED_TO_ITERATION("将工作项规划到迭代"),
    WORK_ITEM_ARCHIVED("归档了工作项"),
    WORK_ITEM_RECYCLED("将工作项移入回收站"),
    WORK_ITEM_RESTORED("恢复了工作项"),

    COMMENT_CREATED("添加了评论"),
    COMMENT_REPLIED("回复了评论"),
    COMMENT_UPDATED("更新了评论"),
    COMMENT_DELETED("删除了评论"),

    WORK_LOG_CREATED("登记了 {} 小时工时"),
    WORK_LOG_UPDATED("更新了工时记录");

    /**
     * 动态内容模板
     */
    private final String content;

    /**
     * 格式化动态内容
     *
     * @param arguments 模板参数
     * @return 动态内容
     */
    public String format(Object... arguments) {
        return StrUtil.format(content, arguments);
    }

}
