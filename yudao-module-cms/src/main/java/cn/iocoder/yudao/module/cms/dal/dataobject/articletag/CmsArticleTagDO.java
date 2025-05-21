package cn.iocoder.yudao.module.cms.dal.dataobject.articletag;

import cn.iocoder.yudao.framework.mybatis.core.dataobject.BaseDO;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.*;

@TableName("cms_article_tag")
@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CmsArticleTagDO extends BaseDO {

    private Long id; // Assuming 'id' is the primary key of the join table itself
    private Long articleId;
    private Long tagId;
}
