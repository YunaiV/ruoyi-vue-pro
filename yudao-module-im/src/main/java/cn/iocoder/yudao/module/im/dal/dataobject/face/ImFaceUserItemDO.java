package cn.iocoder.yudao.module.im.dal.dataobject.face;

import cn.iocoder.yudao.framework.mybatis.core.dataobject.BaseDO;
import com.baomidou.mybatisplus.annotation.KeySequence;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.*;

/**
 * IM 用户私有表情 DO（个人表情包，对照微信「我的表情」）
 *
 * @author 芋道源码
 */
@TableName("im_face_user_item")
@KeySequence("im_face_user_item_seq")
@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ImFaceUserItemDO extends BaseDO {

    /**
     * 编号
     */
    @TableId
    private Long id;
    /**
     * 所属用户编号
     * <p>
     * 关联 AdminUserDO 的 id 编号
     */
    private Long userId;
    /**
     * 表情图 URL
     */
    private String url;
    /**
     * 表情名（可选）
     * <p>
     * 用户私有表情通常不带名字，留字段以备将来「重命名」交互
     */
    private String name;
    /**
     * 渲染宽度（像素）
     */
    private Integer width;
    /**
     * 渲染高度（像素）
     */
    private Integer height;
    /**
     * 排序
     */
    private Integer sort;

}
