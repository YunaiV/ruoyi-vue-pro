package cn.iocoder.yudao.module.yaya.service.content.vo;

import lombok.Data;
import lombok.experimental.Accessors;

@Data
@Accessors(chain = true)
public class YayaContentSeasonCreateReq {

    private String seasonKey;
    private String name;
    private Boolean active;
    private Boolean defaulted;

}
