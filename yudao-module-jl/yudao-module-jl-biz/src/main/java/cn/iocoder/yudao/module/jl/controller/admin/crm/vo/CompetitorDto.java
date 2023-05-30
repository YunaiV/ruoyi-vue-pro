package cn.iocoder.yudao.module.jl.controller.admin.crm.vo;

import cn.iocoder.yudao.module.jl.dal.dataobject.crm.Competitor;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;

import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import java.io.Serializable;

/**
 * DTO for {@link Competitor}
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@Accessors(chain = true)
public class CompetitorDto implements Serializable {
    private Long id;

    @NotNull
    @Size(max = 255)
    private String name;
    @Size(max = 255)
    private String contactName;
    @Size(max = 20)
    private String phone;
    @Size(max = 64)
    private String type;
    private String advantage;
    private String disadvantage;
    private String mark;
}