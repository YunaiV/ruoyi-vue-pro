package cn.iocoder.yudao.framework.template.config;

import com.deepoove.poi.policy.RenderPolicy;
import lombok.Builder;
import lombok.Data;
import org.springframework.core.io.Resource;

import java.util.List;
import java.util.Map;

/**
 * 模板标签绑定的策略配置属性
 * 用于描述单个模板标签（tagName）对应的渲染策略（RenderPolicy）配置。
 * 示例：绑定标签 products 到表格渲染策略 LoopRowTableRenderPolicy。
 *
 * @author wdy
 */
@Data
@Builder
public class TemplatePolicy {


    /**
     * 渲染策略，例如表格循环、段落插入、图片渲染等
     * 一个文件: N个策略<tagName:policy>
     */
    List<Map<String, RenderPolicy>> policies;
    /**
     * 所在的 Word 模板资源文件（classpath 资源）
     * 可用于绑定标签到指定模板
     */
    private Resource resource;
    /**
     * 是否启用预热：
     * - true：程序启动时会加载该模板并初始化缓存
     * - false：不参与预热，仅在首次使用时加载
     */
    private Boolean enablePreload;
//
//    /**
//     * 模板中的占位符标签名称，例如 {{products}}
//     */
//    private String tagName;
//
//    /**
//     * 渲染策略，例如表格循环、段落插入、图片渲染等
//     */
//    private RenderPolicy policy;
}
