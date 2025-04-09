package cn.iocoder.yudao.framework.template.config;

import com.deepoove.poi.policy.RenderPolicy;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.core.io.Resource;

import java.util.List;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class TemplateRegistar {

    private String tagName;

    private RenderPolicy policy;

    /**
     * 是否启用模板预热
     */
    private boolean enablePreload;

    /**
     * 模板扫描路径（支持多个 classpath 目录）
     */
    private List<Resource> resources;

    /**
     * 缓存有效期（可选：预留未来用）
     */
    private Long cacheTtlSeconds = 86400L; //一天
}