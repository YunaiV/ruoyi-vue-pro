package cn.iocoder.yudao.module.cms.dal.dataobject.comment;

import cn.iocoder.yudao.framework.mybatis.core.dataobject.BaseDO;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.*;

@TableName("cms_comment")
@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CmsCommentDO extends BaseDO {

    private Long id;
    private Long articleId;
    private Long userId;        // ID of the logged-in user who commented, nullable
    private String authorName;  // Name of the commenter (if not logged in or preferred display name), nullable
    private String authorEmail; // Email of the commenter (if not logged in), nullable
    private String content;
    private Long parentId;      // For threaded comments, nullable
    private Integer status;     // 0: pending, 1: approved, 2: spam
}
