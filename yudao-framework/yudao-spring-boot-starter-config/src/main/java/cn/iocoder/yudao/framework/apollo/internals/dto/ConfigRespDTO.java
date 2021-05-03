package cn.iocoder.yudao.framework.apollo.internals.dto;

import lombok.Data;

import java.util.Date;

/**
 * 配置 Response DTO
 *
 * @author 芋道源码
 */
@Data
public class ConfigRespDTO {

    /**
     * 参数键名
     */
    private String key;
    /**
     * 参数键值
     */
    private String value;

    /**
     * 是否删除
     */
    private Boolean deleted;
    /**
     * 更新时间
     */
    private Date updateTime;

}
