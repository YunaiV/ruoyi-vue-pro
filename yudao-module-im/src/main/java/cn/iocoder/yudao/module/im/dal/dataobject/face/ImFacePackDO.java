package cn.iocoder.yudao.module.im.dal.dataobject.face;

import cn.iocoder.yudao.framework.common.enums.CommonStatusEnum;
import cn.iocoder.yudao.framework.mybatis.core.dataobject.BaseDO;
import com.baomidou.mybatisplus.annotation.KeySequence;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.*;

/**
 * IM 表情包 DO（运营配置的系统表情包元数据）
 *
 * @author 芋道源码
 */
@TableName("im_face_pack")
@KeySequence("im_face_pack_seq")
@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ImFacePackDO extends BaseDO {

    /**
     * 编号
     */
    @TableId
    private Long id;
    /**
     * 表情包名称
     */
    private String name;
    /**
     * 表情包图标
     * <p>
     * 面板底部 tab 栏显示
     */
    private String icon;
    /**
     * 排序
     */
    private Integer sort;
    /**
     * 状态
     * <p>
     * 枚举 {@link CommonStatusEnum}
     */
    private Integer status;

}
