package cn.iocoder.yudao.framework.ai.core.model.suno;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @Author xiaoxin
 * @Date 2024/5/29
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class SunoConfig {
    /**
     * suno-api服务的基本路径
     */
    private String baseUrl;
}
