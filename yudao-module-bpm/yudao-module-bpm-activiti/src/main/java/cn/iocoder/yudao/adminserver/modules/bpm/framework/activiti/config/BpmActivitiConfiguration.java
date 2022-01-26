package cn.iocoder.yudao.adminserver.modules.bpm.framework.activiti.config;

import cn.iocoder.yudao.adminserver.modules.bpm.framework.activiti.core.behavior.BpmActivityBehaviorFactory;
import cn.iocoder.yudao.adminserver.modules.bpm.framework.activiti.core.behavior.script.BpmTaskAssignScript;
import cn.iocoder.yudao.adminserver.modules.bpm.framework.activiti.core.event.BpmProcessInstanceResultEventPublisher;
import cn.iocoder.yudao.adminserver.modules.bpm.framework.activiti.core.identity.EmptyUserGroupManager;
import cn.iocoder.yudao.adminserver.modules.bpm.framework.activiti.core.listener.BpmTackActivitiEventListener;
import cn.iocoder.yudao.adminserver.modules.bpm.service.definition.BpmTaskAssignRuleService;
import cn.iocoder.yudao.adminserver.modules.bpm.service.definition.BpmUserGroupService;
import cn.iocoder.yudao.coreservice.modules.system.service.dept.SysDeptCoreService;
import cn.iocoder.yudao.coreservice.modules.system.service.permission.SysPermissionCoreService;
import cn.iocoder.yudao.coreservice.modules.system.service.user.SysUserCoreService;
import org.activiti.api.runtime.shared.identity.UserGroupManager;
import org.activiti.spring.boot.ProcessEngineConfigurationConfigurer;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.Collections;
import java.util.List;

import static org.activiti.spring.boot.ProcessEngineAutoConfiguration.BEHAVIOR_FACTORY_MAPPING_CONFIGURER;

/**
 * BPM 模块的 Activiti 配置类
 */
@Configuration
public class BpmActivitiConfiguration {

    /**
     * BPM 模块的 ProcessEngineConfigurationConfigurer 实现类，主要设置各种监听器、用户组管理
     */
    @Bean
    public ProcessEngineConfigurationConfigurer bpmProcessEngineConfigurationConfigurer(
            BpmTackActivitiEventListener taskActivitiEventListener) {
        return configuration -> {
            // 注册监听器，例如说 BpmActivitiEventListener
            configuration.setEventListeners(Collections.singletonList(taskActivitiEventListener));
            // 用户组
            configuration.setUserGroupManager(new EmptyUserGroupManager());
        };
    }

    /**
     * 用于设置自定义的 ActivityBehaviorFactory 实现的 ProcessEngineConfigurationConfigurer 实现类
     *
     * 目的：覆盖 {@link org.activiti.spring.boot.ProcessEngineAutoConfiguration} 的
     *      defaultActivityBehaviorFactoryMappingConfigurer 方法创建的 Bean
     */
    @Bean(name = BEHAVIOR_FACTORY_MAPPING_CONFIGURER)
    public ProcessEngineConfigurationConfigurer defaultActivityBehaviorFactoryMappingConfigurer(
            BpmActivityBehaviorFactory bpmActivityBehaviorFactory) {
        return configuration -> {
            // 设置 ActivityBehaviorFactory 实现类，用于流程任务的审核人的自定义
            configuration.setActivityBehaviorFactory(bpmActivityBehaviorFactory);
        };
    }

    @Bean
    public BpmActivityBehaviorFactory bpmActivityBehaviorFactory(BpmTaskAssignRuleService taskRuleService,
                                                                 SysPermissionCoreService permissionCoreService,
                                                                 SysDeptCoreService deptCoreService,
                                                                 BpmUserGroupService userGroupService,
                                                                 SysUserCoreService userCoreService,
                                                                 List<BpmTaskAssignScript> scripts) {
        BpmActivityBehaviorFactory bpmActivityBehaviorFactory = new BpmActivityBehaviorFactory();
        bpmActivityBehaviorFactory.setBpmTaskRuleService(taskRuleService);
        bpmActivityBehaviorFactory.setPermissionCoreService(permissionCoreService);
        bpmActivityBehaviorFactory.setDeptCoreService(deptCoreService);
        bpmActivityBehaviorFactory.setUserGroupService(userGroupService);
        bpmActivityBehaviorFactory.setUserCoreService(userCoreService);
        bpmActivityBehaviorFactory.setScripts(scripts);
        return bpmActivityBehaviorFactory;
    }

    @Bean
    public BpmProcessInstanceResultEventPublisher processInstanceResultEventPublisher(ApplicationEventPublisher publisher) {
        return new BpmProcessInstanceResultEventPublisher(publisher);
    }

}
