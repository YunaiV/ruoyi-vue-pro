package cn.iocoder.yudao.module.system.framework.datapermission.config;

import cn.iocoder.yudao.module.system.dal.dataobject.dept.SysDeptDO;
import cn.iocoder.yudao.module.system.dal.dataobject.user.UserDO;
import cn.iocoder.yudao.framework.datapermission.core.dept.rule.DeptDataPermissionRuleCustomizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * system 模块的数据权限 Configuration
 *
 * @author 芋道源码
 */
@Configuration(proxyBeanMethods = false)
public class DataPermissionConfiguration {

    @Bean
    public DeptDataPermissionRuleCustomizer sysDeptDataPermissionRuleCustomizer() {
        return rule -> {
            rule.addDeptColumn(UserDO.class);
            rule.addDeptColumn(SysDeptDO.class, "id");
        };
    }

}
