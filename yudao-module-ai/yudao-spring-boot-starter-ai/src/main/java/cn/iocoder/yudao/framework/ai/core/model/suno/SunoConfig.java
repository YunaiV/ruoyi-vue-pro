package cn.iocoder.yudao.framework.ai.core.model.suno;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

// TODO @xin：不需要这个类哈，直接 SunoApi 传入 baseUrl 参数即可
/**
 * Suno 配置类
 *
 * @author  xiaoxin
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
