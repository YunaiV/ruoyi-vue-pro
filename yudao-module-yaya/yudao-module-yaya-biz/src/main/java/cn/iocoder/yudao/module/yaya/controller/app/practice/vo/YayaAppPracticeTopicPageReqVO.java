package cn.iocoder.yudao.module.yaya.controller.app.practice.vo;

import cn.iocoder.yudao.framework.common.pojo.PageParam;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import lombok.experimental.Accessors;

@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
@Accessors(chain = true)
public class YayaAppPracticeTopicPageReqVO extends PageParam {

    private String season;
    private Integer part;
    private String topicType;
    private String progressFilter;
    private String q;

}
