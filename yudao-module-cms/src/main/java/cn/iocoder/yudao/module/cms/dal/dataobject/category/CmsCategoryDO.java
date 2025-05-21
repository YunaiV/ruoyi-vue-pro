package cn.iocoder.yudao.module.cms.dal.dataobject.category;

import cn.iocoder.yudao.framework.mybatis.core.dataobject.BaseDO;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.*;

@TableName("cms_category")
@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CmsCategoryDO extends BaseDO {
    private Long id;
    private String name;
    private String slug;
    private Long parentId;
    private String description;
}
