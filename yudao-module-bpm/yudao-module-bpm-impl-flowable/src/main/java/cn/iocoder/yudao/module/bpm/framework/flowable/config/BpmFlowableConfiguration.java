package cn.iocoder.yudao.module.bpm.framework.flowable.config;

import cn.iocoder.yudao.module.bpm.framework.flowable.core.behavior.BpmActivityBehaviorFactory;
import cn.iocoder.yudao.module.bpm.framework.flowable.core.listener.BpmProcessInstanceEventListener;
import cn.iocoder.yudao.module.bpm.service.definition.BpmTaskAssignRuleService;
import cn.iocoder.yudao.module.bpm.service.definition.BpmUserGroupService;
import cn.iocoder.yudao.module.system.api.dept.DeptApi;
import cn.iocoder.yudao.module.system.api.permission.PermissionApi;
import cn.iocoder.yudao.module.system.api.user.AdminUserApi;
import org.flowable.common.engine.api.delegate.event.FlowableEventListener;
import org.flowable.engine.runtime.ProcessInstance;
import org.flowable.spring.SpringProcessEngineConfiguration;
import org.flowable.spring.boot.EngineConfigurationConfigurer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.ArrayList;
import java.util.List;
/**
 * BPM 模块的 Flowable 配置类
 *
 * @author jason
 */
@Configuration
public class BpmFlowableConfiguration {

    /**
     * 将自定义 listener 加入全局listener
     * @param processInstanceListener 自定义监听 {@link ProcessInstance}
     */
    @Bean
    public EngineConfigurationConfigurer<SpringProcessEngineConfiguration> addCustomerListenerConfigurer (BpmProcessInstanceEventListener processInstanceListener,
                                                                                                          BpmActivityBehaviorFactory bpmActivityBehaviorFactory) {
        return engineConfiguration -> {
            List<FlowableEventListener> eventListeners = new ArrayList<>();
            eventListeners.add(processInstanceListener);
            engineConfiguration.setEventListeners(eventListeners);
            engineConfiguration.setActivityBehaviorFactory(bpmActivityBehaviorFactory);
        };
    }



    @Bean
    public BpmActivityBehaviorFactory bpmActivityBehaviorFactory(BpmTaskAssignRuleService taskRuleService,
                                                                 BpmUserGroupService userGroupService,
                                                                 PermissionApi permissionApi,
                                                                 DeptApi deptApi,
                                                                 AdminUserApi adminUserApi) {
        BpmActivityBehaviorFactory bpmActivityBehaviorFactory = new BpmActivityBehaviorFactory();
        bpmActivityBehaviorFactory.setBpmTaskRuleService(taskRuleService);
        bpmActivityBehaviorFactory.setUserGroupService(userGroupService);
        bpmActivityBehaviorFactory.setAdminUserApi(adminUserApi);
        bpmActivityBehaviorFactory.setPermissionApi(permissionApi);
        bpmActivityBehaviorFactory.setDeptApi(deptApi);
//        bpmActivityBehaviorFactory.setScripts(scripts);
        return bpmActivityBehaviorFactory;
    }
}
