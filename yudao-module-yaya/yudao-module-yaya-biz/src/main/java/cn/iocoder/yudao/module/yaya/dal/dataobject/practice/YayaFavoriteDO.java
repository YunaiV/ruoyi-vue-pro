package cn.iocoder.yudao.module.yaya.dal.dataobject.practice;

import cn.iocoder.yudao.framework.mybatis.core.dataobject.BaseDO;
import com.baomidou.mybatisplus.annotation.KeySequence;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

@TableName("yaya_favorite")
@KeySequence("yaya_favorite_seq")
@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
public class YayaFavoriteDO extends BaseDO {

    @TableId
    private Long id;
    private Long memberUserId;
    private Long topicId;

}
