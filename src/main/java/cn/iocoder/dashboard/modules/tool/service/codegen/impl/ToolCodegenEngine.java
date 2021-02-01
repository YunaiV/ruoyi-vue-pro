package cn.iocoder.dashboard.modules.tool.service.codegen.impl;

import cn.hutool.extra.template.TemplateConfig;
import cn.hutool.extra.template.TemplateEngine;
import cn.hutool.extra.template.TemplateUtil;
import cn.iocoder.dashboard.framework.mybatis.core.dataobject.BaseDO;
import cn.iocoder.dashboard.modules.tool.dal.mysql.dataobject.codegen.ToolCodegenColumnDO;
import cn.iocoder.dashboard.modules.tool.dal.mysql.dataobject.codegen.ToolCodegenTableDO;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 代码生成的引擎，用于具体生成代码
 * 目前基于 {@link org.apache.velocity.app.Velocity} 模板引擎实现
 *
 * 考虑到 Java 模板引擎的框架非常多，Freemarker、Velocity、Thymeleaf 等等，所以我们采用 hutool 封装的 {@link cn.hutool.extra.template.Template} 抽象
 *
 * @author 芋道源码
 */
@Component
public class ToolCodegenEngine {

    /**
     * 模板引擎，由 hutool 实现
     */
    private final TemplateEngine templateEngine;

    public ToolCodegenEngine() {
        TemplateConfig config = new TemplateConfig();
        config.setResourceMode(TemplateConfig.ResourceMode.CLASSPATH);
        this.templateEngine = TemplateUtil.createEngine(config);
    }

    public void execute(ToolCodegenTableDO table, List<ToolCodegenColumnDO> columns) {
        Map<String, Object> bindingMap = new HashMap<>();
        bindingMap.put("table", table);
        bindingMap.put("columns", columns);
        bindingMap.put("basePackage", "cn.iocoder.dashboard.modules"); // TODO 基础包
        // DO 类，独有字段
        bindingMap.put("baseDOFields", ToolCodegenBuilder.BASE_DO_FIELDS);
        bindingMap.put("baseDOClassName", BaseDO.class.getName());
        String result = templateEngine.getTemplate("codegen/dal/do.vm").render(bindingMap);
        System.out.println(result);
    }

}
