package cn.iocoder.yudao.module.jl.entity.crm;

import cn.iocoder.yudao.module.jl.entity.BaseEntity;
import cn.iocoder.yudao.module.jl.entity.user.User;
import com.fasterxml.jackson.annotation.JsonBackReference;
import com.fasterxml.jackson.annotation.JsonIdentityInfo;
import com.fasterxml.jackson.annotation.JsonManagedReference;
import com.fasterxml.jackson.annotation.ObjectIdGenerators;
import lombok.*;
import org.hibernate.annotations.NotFound;
import org.hibernate.annotations.NotFoundAction;

import java.util.*;
import javax.persistence.*;
import javax.validation.constraints.*;
import java.time.LocalDateTime;
import java.time.LocalDateTime;
import java.time.LocalDateTime;

/**
 * 客户 Entity
 *
 */
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@Entity(name = "Customer")
@Table(name = "jl_crm_customer")
@JsonIdentityInfo(generator = ObjectIdGenerators.PropertyGenerator.class, property = "id")
public class Customer extends BaseEntity {

    /**
     * ID
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", nullable = false )
    private Long id;

    /**
     * 姓名
     */
    @Column(name = "name", nullable = false )
    private String name;

    /**
     * 客户来源
     *
     * 枚举 {@link TODO customer_source 对应的类}
     */
    @Column(name = "source", nullable = false )
    private String source;

    /**
     * 手机号
     */
    @Column(name = "phone", nullable = false )
    private String phone;

    /**
     * 邮箱
     */
    @Column(name = "email")
    private String email;

    /**
     * 备注
     */
    @Column(name = "mark")
    private String mark;

    /**
     * 微信号
     */
    @Column(name = "wechat", nullable = false )
    private String wechat;

    /**
     * 医生职业级别
     */
    @Column(name = "doctor_professional_rank")
    private String doctorProfessionalRank;

    /**
     * 医院科室
     */
    @Column(name = "hospital_department")
    private String hospitalDepartment;

    /**
     * 学校职称
     */
    @Column(name = "academic_title")
    private String academicTitle;

    /**
     * 学历
     */
    @Column(name = "academic_credential")
    private String academicCredential;

    /**
     * 医院
     */
    @Column(name = "hospital_id")
    private Long hospitalId;

    @ManyToOne(fetch = FetchType.EAGER)
    @NotFound(action = NotFoundAction.IGNORE)
    @JoinColumn(name = "hospital_id", insertable = false, updatable = false)
    private Institution hospital;

    /**
     * 学校机构
     */
    @Column(name = "university_id")
    private Long universityId;

    @ManyToOne(fetch = FetchType.EAGER)
    @NotFound(action = NotFoundAction.IGNORE)
    @JoinColumn(name = "university_id", insertable = false, updatable = false)
    private Institution university;

    /**
     * 公司
     */
    @Column(name = "company_id")
    private Long companyId;

    @ManyToOne(fetch = FetchType.EAGER)
    @NotFound(action = NotFoundAction.IGNORE)
    @JoinColumn(name = "company_id", insertable = false, updatable = false)
    private Institution company;

    /**
     * 省
     */
    @Column(name = "province")
    private String province;

    /**
     * 市
     */
    @Column(name = "city")
    private String city;

    /**
     * 区
     */
    @Column(name = "area")
    private String area;

    /**
     * 客户类型
     */
    @Column(name = "type")
    private String type;

    /**
     * 成交次数
     */
    @Column(name = "deal_count")
    private Integer dealCount;

    /**
     * 成交总额
     */
    @Column(name = "deal_total_amount")
    private Long dealTotalAmount;

    /**
     * 欠款总额
     */
    @Column(name = "arrears")
    private Long arrears;

    /**
     * 最后一次跟进时间
     */
    @Column(name = "last_followup_time")
    private LocalDateTime lastFollowupTime;

    /**
     * 当前负责的销售人员
     */
    @Column(name = "sales_id")
    private Long salesId;

    @OneToOne(fetch = FetchType.EAGER)
    @NotFound(action = NotFoundAction.IGNORE)
    @JoinColumn(name = "sales_id", referencedColumnName = "id", insertable = false, updatable = false)
    private User sales;

    /**
     * 最后一次的跟进 id
     */
    @Column(name = "last_followup_id")
    private Long lastFollowupId;

    @ManyToOne(fetch = FetchType.EAGER)
    @NotFound(action = NotFoundAction.IGNORE)
    @JoinColumn(name = "last_followup_id", insertable = false, updatable = false)
    private Followup lastFollowup;

    /**
     * 最后一次销售线索
     */
    @Column(name = "last_saleslead_id")
    private Long lastSalesleadId;

    @OneToOne(fetch = FetchType.EAGER)
    @NotFound(action = NotFoundAction.IGNORE)
    @JoinColumn(name = "last_saleslead_id", insertable = false, updatable = false)
    @JsonManagedReference
    private Saleslead lastSaleslead;
}
