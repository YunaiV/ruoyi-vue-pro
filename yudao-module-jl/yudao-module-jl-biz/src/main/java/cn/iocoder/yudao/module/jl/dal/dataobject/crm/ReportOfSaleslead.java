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
@Entity(name = "ReportOfSaleslead")
@Table(name = "jl_join_saleslead2report")
public class ReportOfSaleslead {
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

    @NotNull
    @Column(name = "saleslead_id", nullable = false)
    private Long salesleadId;

    @Size(max = 512)
    @NotNull
    @Column(name = "file_url", nullable = false, length = 512)
    private String fileUrl;

    @Size(max = 512)
    @NotNull
    @Column(name = "file_name", nullable = false, length = 512)
    private String fileName;

}