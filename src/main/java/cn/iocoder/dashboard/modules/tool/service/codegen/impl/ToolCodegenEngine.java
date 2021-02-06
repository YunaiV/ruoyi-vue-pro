package cn.iocoder.dashboard.modules.tool.service.codegen.impl;

import cn.hutool.extra.template.TemplateConfig;
import cn.hutool.extra.template.TemplateEngine;
import cn.hutool.extra.template.TemplateUtil;
import cn.iocoder.dashboard.common.pojo.PageParam;
import cn.iocoder.dashboard.common.pojo.PageResult;
import cn.iocoder.dashboard.framework.mybatis.core.dataobject.BaseDO;
import cn.iocoder.dashboard.framework.mybatis.core.mapper.BaseMapperX;
import cn.iocoder.dashboard.framework.mybatis.core.query.QueryWrapperX;
import cn.iocoder.dashboard.modules.tool.dal.mysql.dataobject.codegen.ToolCodegenColumnDO;
import cn.iocoder.dashboard.modules.tool.dal.mysql.dataobject.codegen.ToolCodegenTableDO;
import cn.iocoder.dashboard.util.collection.CollectionUtils;
import cn.iocoder.dashboard.util.date.DateUtils;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static cn.hutool.core.text.CharSequenceUtil.*;

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
    /**
     * 全局通用变量映射
     */
    private final Map<String, Object> globalBindingMap = new HashMap<>();

    public ToolCodegenEngine() {
        // 初始化 TemplateEngine 属性
        TemplateConfig config = new TemplateConfig();
        config.setResourceMode(TemplateConfig.ResourceMode.CLASSPATH);
        this.templateEngine = TemplateUtil.createEngine(config);
        // 初始化基础 bindingMap
        initGlobalBindingMap();
    }

    private void initGlobalBindingMap() {
        // 全局配置
        globalBindingMap.put("basePackage", "cn.iocoder.dashboard.modules"); // TODO 基础包, 抽成参数
        // 全局 Java Bean
        globalBindingMap.put("PageResultClassName", PageResult.class.getName());
        globalBindingMap.put("DateUtilsClassName", DateUtils.class.getName());
        // VO 类，独有字段
        globalBindingMap.put("PageParamClassName", PageParam.class.getName());
        // DO 类，独有字段
        globalBindingMap.put("baseDOFields", ToolCodegenBuilder.BASE_DO_FIELDS);
        globalBindingMap.put("baseDOClassName", BaseDO.class.getName());
        globalBindingMap.put("QueryWrapperClassName", QueryWrapperX.class.getName());
        globalBindingMap.put("BaseMapperClassName", BaseMapperX.class.getName());
    }

    public void execute(ToolCodegenTableDO table, List<ToolCodegenColumnDO> columns) {
        // 创建 bindingMap
        Map<String, Object> bindingMap = new HashMap<>(globalBindingMap);
        bindingMap.put("table", table);
        bindingMap.put("columns", columns);
        bindingMap.put("primaryColumn", CollectionUtils.findFirst(columns, ToolCodegenColumnDO::getPrimaryKey));
        bindingMap.put("simpleClassName", upperFirst(toCamelCase(subAfter( // 去掉第一个驼峰，例如说 SysUser 去掉后是 User
                toUnderlineCase(table.getClassName()), '_', false))));
        // 执行生成
//        String result = templateEngine.getTemplate("codegen/dal/do.vm").render(bindingMap);
//        String result = templateEngine.getTemplate("codegen/dal/mapper.vm").render(bindingMap);
//        String result = templateEngine.getTemplate("codegen/controller/vo/pageReqVO.vm").render(bindingMap);
//        String result = templateEngine.getTemplate("codegen/controller/vo/baseVO.vm").render(bindingMap);
//        String result = templateEngine.getTemplate("codegen/controller/vo/createReqVO.vm").render(bindingMap);
//        String result = templateEngine.getTemplate("codegen/controller/vo/updateReqVO.vm").render(bindingMap);
//        String result = templateEngine.getTemplate("codegen/controller/vo/respVO.vm").render(bindingMap);
        String result = templateEngine.getTemplate("codegen/service/service.vm").render(bindingMap);
        System.out.println(result);
    }

}
