package cn.iocoder.yudao.module.jl.dal.dataobject.crm;

import cn.iocoder.yudao.module.jl.dal.dataobject.BaseEntity;
import cn.iocoder.yudao.module.jl.dal.dataobject.system.SystemUser;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.Accessors;

import javax.persistence.*;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import java.time.Instant;
import java.time.LocalDateTime;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@Accessors(chain = true)
@Entity(name = "Customer")
@Table(name = "jl_crm_customer")
public class Customer extends BaseEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", nullable = false)
    private Long id;

    @Size(max = 255)
    @NotNull
    @Column(name = "name", nullable = false)
    private String name;

    @Size(max = 128)
    @NotNull
    @Column(name = "source", nullable = false, length = 128)
    private String source;

    @Size(max = 20)
    @NotNull
    @Column(name = "phone", nullable = false, length = 20)
    private String phone;

    @Size(max = 50)
    @NotNull
    @Column(name = "wechat", nullable = false, length = 50)
    private String wechat;

    @Size(max = 50)
    @Column(name = "email", length = 50)
    private String email;

    @Size(max = 64)
    @Column(name = "doctor_professional_rank", length = 64)
    private String doctorProfessionalRank;

//    @Column(name = "hospital_id")
//    private Long hospitalId;

    @Size(max = 128)
    @Column(name = "hospital_department", length = 128)
    private String hospitalDepartment;

    @Size(max = 128)
    @Column(name = "academic_title", length = 128)
    private String academicTitle;

//    @Column(name = "university_id")
//    private Long universityId;
//
    @Size(max = 128)
    @Column(name = "academic_credential", length = 128)
    private String academicCredential;
//
//    @Column(name = "company_id")
//    private Long companyId;

    @Lob
    @Column(name = "mark")
    private String mark;

    @Size(max = 128)
    @Column(name = "province", length = 128)
    private String province;

    @Size(max = 128)
    @Column(name = "city", length = 128)
    private String city;

    @Size(max = 128)
    @Column(name = "area", length = 128)
    private String area;

    @Size(max = 128)
    @Column(name = "type", length = 128)
    private String type;

    @Column(name = "deal_count")
    private Integer dealCount;

    @Column(name = "deal_total_amount")
    private Long dealTotalAmount;

    @Column(name = "arrears")
    private Long arrears;

    @Column(name = "last_followup_time")
    private Instant lastFollowupTime;

//    @Column(name = "sales_id")
//    private Long salesId;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "sales_id")
    private SystemUser sales;

    @Column(name = "last_followup_id")
    private Long lastFollowupId;

    @Column(name = "last_saleslead_id")
    private Long lastSalesleadId;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "company_id")
    private Institution company;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "hospital_id")
    private Institution hospital;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "university_id")
    private Institution university;
}