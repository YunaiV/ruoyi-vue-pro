package cn.iocoder.yudao.module.yaya.dal.dataobject.content;

import cn.iocoder.yudao.framework.mybatis.core.dataobject.BaseDO;
import com.baomidou.mybatisplus.annotation.KeySequence;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

@TableName("yaya_topic_tag")
@KeySequence("yaya_topic_tag_seq")
@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
public class YayaTopicTagDO extends BaseDO {

    @TableId
    private Long id;
    private Long topicId;
    private Long tagId;

}
