package cn.iocoder.yudao.module.jl.dal.dataobject.crm;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.Accessors;

import javax.persistence.*;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import java.time.Instant;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@Accessors(chain = true)
@Entity(name = "Saleslead")
@Table(name = "jl_crm_saleslead")
public class Saleslead {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", nullable = false)
    private Long id;

    @Size(max = 64)
    @Column(name = "creator", length = 64)
    private String creator;

    @NotNull
    @Column(name = "create_time", nullable = false)
    private Instant createTime;

    @Size(max = 64)
    @Column(name = "updater", length = 64)
    private String updater;

    @NotNull
    @Column(name = "update_time", nullable = false)
    private Instant updateTime;

    @NotNull
    @Column(name = "deleted", nullable = false)
    private Boolean deleted = false;

    @NotNull
    @Column(name = "tenant_id", nullable = false)
    private Long tenantId;

    @Size(max = 100)
    @NotNull
    @Column(name = "source", nullable = false, length = 100)
    private String source;

    @Lob
    @Column(name = "requirement")
    private String requirement;

    @Column(name = "budget")
    private Long budget;

    @Column(name = "quotation")
    private Long quotation;

    @NotNull
    @Column(name = "status", nullable = false)
    private Integer status;

    @NotNull
    @Column(name = "customer_id", nullable = false)
    private Long customerId;

    @Column(name = "project_id")
    private Long projectId;

    @Size(max = 128)
    @Column(name = "business_type", length = 128)
    private String businessType;

    @Lob
    @Column(name = "lost_note")
    private String lostNote;

    @Column(name = "manager_id")
    private Long managerId;

}