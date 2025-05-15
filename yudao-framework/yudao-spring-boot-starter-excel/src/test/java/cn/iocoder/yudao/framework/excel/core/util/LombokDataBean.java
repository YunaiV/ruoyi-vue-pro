package cn.iocoder.yudao.framework.excel.core.util;

import lombok.Data;
import lombok.experimental.Accessors;

import java.util.Date;

@Data
@Accessors(chain = true)
public class LombokDataBean {
    private Double number;
    private String string;
    private Date date;
    private Boolean areYouOk;

}
