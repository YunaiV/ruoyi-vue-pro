package cn.iocoder.yudao.module.yaya.service.content.vo;

import cn.iocoder.yudao.framework.common.pojo.PageParam;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import lombok.experimental.Accessors;

@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
@Accessors(chain = true)
public class YayaTopicPageReqVO extends PageParam {

    private String seasonKey;
    private Integer part;
    private String publishStatus;
    private String keyword;

}
