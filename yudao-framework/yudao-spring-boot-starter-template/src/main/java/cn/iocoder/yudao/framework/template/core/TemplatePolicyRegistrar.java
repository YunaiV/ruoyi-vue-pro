package cn.iocoder.yudao.framework.template.core;

import cn.iocoder.yudao.framework.template.config.TemplateTagPolicyProperty;

import java.util.List;

public interface TemplatePolicyRegistrar {

    List<TemplateTagPolicyProperty> getPolicyProperties();
}