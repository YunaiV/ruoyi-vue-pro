package cn.iocoder.yudao.module.jl.dal.dataobject.crm;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;

import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import java.io.Serializable;
import java.time.Instant;
import java.time.LocalDateTime;

/**
 * DTO for {@link Customer}
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@Accessors(chain = true)
public class CustomerDto implements Serializable {
    private LocalDateTime createTime;
    private LocalDateTime updateTime;
    private Long id;
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
    private Integer dealCount;
    private Long dealTotalAmount;
    private Long arrears;
    private Instant lastFollowupTime;
    private SystemUserDto sales;
    private Long lastFollowupId;
    private Long lastSalesleadId;
    private InstitutionDto company;
    private InstitutionDto hospital;
    private InstitutionDto university;

    /**
     * DTO for {@link cn.iocoder.yudao.module.jl.dal.dataobject.system.SystemUser}
     */
    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    @Accessors(chain = true)
    public static class SystemUserDto implements Serializable {
        private Long id;
        @NotNull
        @Size(max = 30)
        private String username;
        @NotNull
        @Size(max = 30)
        private String nickname;
        @Size(max = 500)
        private String remark;
        private Long deptId;
        @Size(max = 255)
        private String postIds;
        @Size(max = 50)
        private String email;
        @Size(max = 11)
        private String mobile;
        private Byte sex;
        @Size(max = 512)
        private String avatar;
        @NotNull
        private Byte status;
    }

    /**
     * DTO for {@link Institution}
     */
    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    @Accessors(chain = true)
    public static class InstitutionDto implements Serializable {
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

//    /**
//     * DTO for {@link Institution}
//     */
//    @Data
//    @AllArgsConstructor
//    @NoArgsConstructor
//    @Accessors(chain = true)
//    public static class InstitutionDto implements Serializable {
//        private Long id;
//        @NotNull
//        @Size(max = 128)
//        private String province;
//        @NotNull
//        @Size(max = 128)
//        private String city;
//        @NotNull
//        @Size(max = 255)
//        private String name;
//        @NotNull
//        @Size(max = 512)
//        private String address;
//        private String mark;
//        @NotNull
//        @Size(max = 128)
//        private String type;
//    }
//
//    /**
//     * DTO for {@link Institution}
//     */
//    @Data
//    @AllArgsConstructor
//    @NoArgsConstructor
//    @Accessors(chain = true)
//    public static class InstitutionDto implements Serializable {
//        private Long id;
//        @NotNull
//        @Size(max = 128)
//        private String province;
//        @NotNull
//        @Size(max = 128)
//        private String city;
//        @NotNull
//        @Size(max = 255)
//        private String name;
//        @NotNull
//        @Size(max = 512)
//        private String address;
//        private String mark;
//        @NotNull
//        @Size(max = 128)
//        private String type;
//    }
}