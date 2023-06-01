package cn.iocoder.yudao.module.jl.entity.laboratory;

import cn.iocoder.yudao.module.jl.entity.BaseEntity;
import lombok.*;
import java.util.*;
import javax.persistence.*;
import javax.validation.constraints.*;
import java.time.LocalDateTime;
import java.time.LocalDateTime;

/**
 * 实验名目的擅长人员 Entity
 *
 */
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@Entity(name = "CategorySkillUser")
@Table(name = "jl_laboratory_category_skilluser")
public class CategorySkillUser extends BaseEntity {

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
     * 实验人员 id
     */
    @Column(name = "user_id", nullable = false )
    private Long userId;

}
