package cn.iocoder.yudao.framework.ai.core.model.suno;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;

/**
 * @Author xiaoxin
 * @Date 2024/5/29
 */
@Data
@Accessors(chain = true)
@NoArgsConstructor
@AllArgsConstructor
public class SunoConfig {
    /**
     * token信息
     */
    private String token;
}
