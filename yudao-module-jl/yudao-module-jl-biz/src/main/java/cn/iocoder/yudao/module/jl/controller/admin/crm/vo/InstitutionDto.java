package cn.iocoder.yudao.module.jl.controller.admin.crm.vo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;

import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import java.io.Serializable;

/**
 * DTO for {@link cn.iocoder.yudao.module.jl.dal.dataobject.crm.Institution}
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@Accessors(chain = true)
public class InstitutionDto implements Serializable {
    private Long id;
    @NotNull
    @Size(max = 128)
    private String province;
    @NotNull
    @Size(max = 128)
    private String city;
    @NotNull
    @Size(max = 255)
    private String name;
    @NotNull
    @Size(max = 512)
    private String address;
    private String mark;
    @NotNull
    @Size(max = 128)
    private String type;
}