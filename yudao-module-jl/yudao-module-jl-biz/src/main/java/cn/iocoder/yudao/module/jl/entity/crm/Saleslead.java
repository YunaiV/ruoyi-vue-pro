package cn.iocoder.yudao.module.jl.entity.crm;

import cn.iocoder.yudao.module.jl.entity.BaseEntity;
import cn.iocoder.yudao.module.jl.entity.project.Project;
import cn.iocoder.yudao.module.jl.entity.project.ProjectQuote;
import cn.iocoder.yudao.module.jl.entity.user.User;
import com.fasterxml.jackson.annotation.JsonBackReference;
import com.fasterxml.jackson.annotation.JsonIdentityInfo;
import com.fasterxml.jackson.annotation.ObjectIdGenerators;
import com.fasterxml.jackson.annotation.JsonManagedReference;

import lombok.*;
import org.hibernate.annotations.NotFound;
import org.hibernate.annotations.NotFoundAction;

import java.util.*;
import javax.persistence.*;
import javax.validation.constraints.*;
import java.time.LocalDateTime;
import java.time.LocalDateTime;

/**
 * 销售线索 Entity
 *
 */
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@Entity(name = "Saleslead")
@Table(name = "jl_crm_saleslead")
@JsonIdentityInfo(generator = ObjectIdGenerators.PropertyGenerator.class, property = "id")
public class Saleslead extends BaseEntity {

    /**
     * ID
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", nullable = false )
    private Long id;

    /**
     * 销售线索来源
     *
     * 枚举 {@link TODO sales_lead_source 对应的类}
     */
    @Column(name = "source", nullable = false )
    private String source;

    /**
     * 关键需求
     */
    @Column(name = "requirement")
    private String requirement;

    /**
     * 预算(元)
     */
    @Column(name = "budget")
    private Long budget;

    /**
     * 报价
     */
    @Column(name = "quotation")
    private Long quotation;

    /**
     * 状态
     *
     * 枚举 {@link TODO sales_lead_status 对应的类}
     */
    @Column(name = "status", nullable = false )
    private Integer status;

    /**
     * 客户id
     */
    @Column(name = "customer_id", nullable = false )
    private Long customerId;

    @JsonBackReference
    @ManyToOne(fetch = FetchType.EAGER)
    @NotFound(action = NotFoundAction.IGNORE)
    @JoinColumn(name = "customer_id", insertable = false, updatable = false)
    private Customer customer;

    /**
     * 项目id
     */
    @Column(name = "project_id")
    private Long projectId;

    /**
     * 业务类型
     */
    @Column(name = "business_type")
    private String businessType;

    /**
     * 丢单的说明
     */
    @Column(name = "lost_note")
    private String lostNote;

    /**
     * 绑定的销售报价人员
     */
    @Column(name = "manager_id")
    private Long managerId;

    @OneToOne(fetch = FetchType.EAGER, optional=true)
    @NotFound(action = NotFoundAction.IGNORE)
    @JoinColumn(name="quotation", referencedColumnName="id", insertable = false, updatable = false)
    private ProjectQuote quote;

    @Column(name = "last_followup_id")
    private Long lastFollowUpId;

    @OneToOne
    @NotFound(action = NotFoundAction.IGNORE)
    @JoinColumn(name = "last_followup_id", referencedColumnName="id", insertable = false, updatable = false)
    private Followup lastFollowup;

    @OneToOne
    @NotFound(action = NotFoundAction.IGNORE)
    @JoinColumn(name = "project_id", referencedColumnName="id", insertable = false, updatable = false)
    private Project project;

    @OneToMany
    @NotFound(action = NotFoundAction.IGNORE)
    @JoinColumn(name = "saleslead_id", referencedColumnName="id", insertable = false, updatable = false)
    private List<SalesleadCompetitor> competitorQuotations;

    @OneToMany
    @NotFound(action = NotFoundAction.IGNORE)
    @JoinColumn(name = "saleslead_id", referencedColumnName="id", insertable = false, updatable = false)
    private List<SalesleadCustomerPlan> customerPlans;
}
