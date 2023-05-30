package cn.iocoder.yudao.module.jl.dal.dataobject.crm;

import cn.iocoder.yudao.module.jl.dal.dataobject.BaseEntity;
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
@Entity(name = "Institution")
@Table(name = "jl_crm_institution")
public class Institution extends BaseEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", nullable = false)
    private Long id;

    @Size(max = 128)
    @NotNull
    @Column(name = "province", nullable = false, length = 128)
    private String province;

    @Size(max = 128)
    @NotNull
    @Column(name = "city", nullable = false, length = 128)
    private String city;

    @Size(max = 255)
    @NotNull
    @Column(name = "name", nullable = false)
    private String name;

    @Size(max = 512)
    @NotNull
    @Column(name = "address", nullable = false, length = 512)
    private String address;

    @Lob
    @Column(name = "mark")
    private String mark;

    @Size(max = 128)
    @NotNull
    @Column(name = "type", nullable = false, length = 128)
    private String type;

}