package cn.iocoder.yudao.module.jl.entity.laboratory;

import cn.iocoder.yudao.module.jl.entity.BaseEntity;
import lombok.*;
import java.util.*;
import javax.persistence.*;
import javax.validation.constraints.*;
import java.time.LocalDateTime;
import java.time.LocalDateTime;

/**
 * 实验名目的参考资料 Entity
 *
 */
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@Entity(name = "CategoryReference")
@Table(name = "jl_laboratory_category_reference")
public class CategoryReference extends BaseEntity {

    /**
     * 岗位ID
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", nullable = false )
    private Long id;

    /**
     * 实验名目 id
     */
    @Column(name = "category_id", nullable = false )
    private Long categoryId;

    /**
     * 文件名
     */
    @Column(name = "name", nullable = false )
    private String name;

    /**
     * 操作步骤的内容
     */
    @Column(name = "url", nullable = false )
    private String url;

    /**
     * 类型(文献、结果参考、交付标准)
     */
    @Column(name = "type", nullable = false )
    private String type;

}
