package cn.iocoder.yudao.module.jl.controller.admin.crm.vo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;

import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import java.io.Serializable;

/**
 * DTO for {@link cn.iocoder.yudao.module.jl.dal.dataobject.crm.Customer}
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@Accessors(chain = true)
public class CustomerCreateReq implements Serializable {
    @NotNull
    @Size(max = 255)
    private String name;
    @NotNull
    @Size(max = 128)
    private String source;
    @NotNull
    @Size(max = 20)
    private String phone;
    @NotNull
    @Size(max = 50)
    private String wechat;
    @Size(max = 50)
    private String email;
    @Size(max = 64)
    private String doctorProfessionalRank;
    @Size(max = 128)
    private String hospitalDepartment;
    @Size(max = 128)
    private String academicTitle;
    @Size(max = 128)
    private String academicCredential;
    private String mark;
    @Size(max = 128)
    private String province;
    @Size(max = 128)
    private String city;
    @Size(max = 128)
    private String area;
    @Size(max = 128)
    private String type;
    private Long salesId;
    private Long companyId;
    private Long hospitalId;
    private Long universityId;
}