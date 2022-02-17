package cn.iocoder.yudao.module.bpm.framework.flowable.config;

import cn.iocoder.yudao.module.bpm.framework.flowable.core.behavior.BpmActivityBehaviorFactory;
import cn.iocoder.yudao.module.bpm.framework.flowable.core.behavior.script.BpmTaskAssignScript;
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
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
/**
 * BPM 模块的 Flowable 配置类
 *
 * @author jason
 */
@Configuration
public class BpmFlowableConfiguration {

    /**
     * Flowable engines 自定义的配置
     * @param  listeners 自定义 listener
     * @param  bpmActivityBehaviorFactory 自定义的 ActivityBehaviorFactory 实现
     */
    @Bean
    public EngineConfigurationConfigurer<SpringProcessEngineConfiguration> addCustomerListenerConfigurer (ObjectProvider<FlowableEventListener> listeners,
                                                                                                          BpmActivityBehaviorFactory bpmActivityBehaviorFactory) {
        return engineConfiguration -> {
             List<FlowableEventListener> eventListeners = new ArrayList<>();
            Iterator<FlowableEventListener> iterator = listeners.iterator();
            while (iterator.hasNext()) {
                eventListeners.add(iterator.next());
            }
            engineConfiguration.setEventListeners(eventListeners);
            engineConfiguration.setActivityBehaviorFactory(bpmActivityBehaviorFactory);
        };
    }



    @Bean
    public BpmActivityBehaviorFactory bpmActivityBehaviorFactory(BpmTaskAssignRuleService taskRuleService,
                                                                 BpmUserGroupService userGroupService,
                                                                 PermissionApi permissionApi,
                                                                 DeptApi deptApi,
                                                                 AdminUserApi adminUserApi,
                                                                 List<BpmTaskAssignScript> scripts) {
        BpmActivityBehaviorFactory bpmActivityBehaviorFactory = new BpmActivityBehaviorFactory();
        bpmActivityBehaviorFactory.setBpmTaskRuleService(taskRuleService);
        bpmActivityBehaviorFactory.setUserGroupService(userGroupService);
        bpmActivityBehaviorFactory.setAdminUserApi(adminUserApi);
        bpmActivityBehaviorFactory.setPermissionApi(permissionApi);
        bpmActivityBehaviorFactory.setDeptApi(deptApi);
        bpmActivityBehaviorFactory.setScripts(scripts);
        return bpmActivityBehaviorFactory;
    }
}
