package cn.iocoder.yudao.module.yaya.dal.dataobject.content;

import cn.iocoder.yudao.framework.mybatis.core.dataobject.BaseDO;
import com.baomidou.mybatisplus.annotation.KeySequence;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

@TableName("yaya_content_season")
@KeySequence("yaya_content_season_seq")
@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
public class YayaContentSeasonDO extends BaseDO {

    @TableId
    private Long id;
    private String seasonKey;
    private String name;
    private Integer active;
    private Integer defaulted;

}
