package cn.iocoder.yudao.framework.mybatis.core.vo;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fhs.core.trans.vo.TransPojo;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 基础 VO 对象
 * 用于统一定义 VO 层的通用字段，便于继承和拓展。
 * <p>
 * 为什么实现 TransPojo 接口？ 因为使用 Easy-Trans TransType. SIMPLE 模式，集成 MyBatis Plus 查询
 * @author wdy
 */
@Data
@JsonIgnoreProperties(value = "transMap") // 由于 Easy-Trans 会添加 transMap 属性，避免 Jackson 在 Spring Cache 反序列化报错
public abstract class BaseVO implements Serializable, TransPojo {

    /**
     * 创建时间
     */
    @Schema(description = "创建时间")
    private LocalDateTime createTime;

    /**
     * 最后更新时间
     */
    @Schema(description = "最后更新时间")
    private LocalDateTime updateTime;

    /**
     * 创建者 ID
     * 使用 String 类型，便于未来扩展。
     */
    @Schema(description = "创建者", example = "admin")
    private String creator;

    /**
     * 更新者 ID
     * 使用 String 类型，便于未来扩展。
     */
    @Schema(description = "更新者", example = "admin")
    private String updater;
}
