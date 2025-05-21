package cn.iocoder.yudao.module.cms.dal.dataobject.article; // Updated package

import cn.iocoder.yudao.framework.mybatis.core.dataobject.BaseDO;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.*;
import java.time.LocalDateTime;
import java.util.List; // Added for tagIds

@TableName("cms_article")
@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CmsArticleDO extends BaseDO {
    private Long id;
    private String title;
    private String slug;
    private String content; // Consider using LONGTEXT for this
    private Long authorId; // Assuming this links to a user ID in the system
    private Long categoryId;
    private Integer status; // 0: draft, 1: published, 2: archived
    private LocalDateTime publishedAt;
    private String coverImageUrl;
    private Integer views;
    private String metaDescription;
    private String metaKeywords;
    // creator, create_time, updater, update_time, deleted, tenant_id are in BaseDO

    @TableField(exist = false)
    private List<Long> tagIds; // For holding associated tag IDs
}
