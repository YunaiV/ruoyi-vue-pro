package cn.iocoder.yudao.module.srm.api.product.dto;

import lombok.*;

import java.time.LocalDateTime;


/**
 * ERP 产品单位 DTO
 *
 * @author gumaomao
 */

@Data
@ToString(callSuper = true)
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ErpProductUnitDTO {

    /**
     * 单位编号
     */

    private Long id;
    /**
     * 单位名字
     */
    private String name;
    /**
     * 单位状态
     */
    private Integer status;

    /**
     * 创建时间
     */

    private LocalDateTime createTime;
    /**
     * 最后更新时间
     */

    private LocalDateTime updateTime;
    /**
     * 创建者，目前使用 SysUser 的 id 编号
     *
     * 使用 String 类型的原因是，未来可能会存在非数值的情况，留好拓展性。
     */
    private String creator;
    /**
     * 更新者，目前使用 SysUser 的 id 编号
     *
     * 使用 String 类型的原因是，未来可能会存在非数值的情况，留好拓展性。
     */
    private String updater;
    /**
     * 是否删除
     */
    private Boolean deleted;
}