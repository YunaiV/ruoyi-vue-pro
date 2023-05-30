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

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@Accessors(chain = true)
@Entity(name = "Competitor")
@Table(name = "jl_crm_competitor")
public class Competitor extends BaseEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", nullable = false)
    private Long id;

    @Size(max = 255)
    @NotNull
    @Column(name = "name", nullable = false)
    private String name;

    @Size(max = 255)
    @Column(name = "contact_name")
    private String contactName;

    @Size(max = 20)
    @Column(name = "phone", length = 20)
    private String phone;

    @Size(max = 64)
    @Column(name = "type", length = 64)
    private String type;

    @Lob
    @Column(name = "advantage")
    private String advantage;

    @Lob
    @Column(name = "disadvantage")
    private String disadvantage;

    @Lob
    @Column(name = "mark")
    private String mark;
}