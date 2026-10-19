package cn.iocoder.yudao.module.oa.dal.dataobject.file;

import cn.iocoder.yudao.framework.mybatis.core.dataobject.BaseDO;
import cn.iocoder.yudao.module.system.api.user.dto.AdminUserRespDTO;
import com.baomidou.mybatisplus.annotation.KeySequence;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

/**
 * OA 云盘收藏 DO
 *
 * @author 芋道源码
 */
@TableName(value = "oa_file_favorite", autoResultMap = true)
@KeySequence("oa_file_favorite_seq")
@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
public class OaFileFavoriteDO extends BaseDO {

    /**
     * 编号
     */
    @TableId
    private Long id;
    /**
     * 收藏节点编号
     *
     * 关联 {@link OaFileNodeDO#getId()}
     */
    private Long nodeId;
    /**
     * 收藏用户编号
     *
     * 关联 {@link AdminUserRespDTO#getId()}
     */
    private Long userId;

}
