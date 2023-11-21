package cn.iocoder.yudao.module.system.api.dept.dto;

import cn.iocoder.yudao.framework.common.enums.CommonStatusEnum;
import lombok.Data;

/**
 * 岗位 Response DTO
 *
 * @author 芋道源码
 */
@Data
public class PostRespDTO {

    /**
     * 岗位序号
     */
    private Long id;
    /**
     * 岗位名称
     */
    private String name;
    /**
     * 岗位编码
     */
    private String code;
    /**
     * 岗位排序
     */
    private Integer sort;
    /**
     * 状态
     *
     * 枚举 {@link CommonStatusEnum}
     */
    private Integer status;

}
