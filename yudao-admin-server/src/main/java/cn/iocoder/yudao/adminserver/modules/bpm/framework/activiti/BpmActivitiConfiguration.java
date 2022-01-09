package cn.iocoder.yudao.adminserver.modules.bpm.framework.activiti;

import org.activiti.spring.SpringProcessEngineConfiguration;
import org.activiti.spring.boot.ProcessEngineConfigurationConfigurer;
import org.springframework.context.annotation.Configuration;

/**
 * BPM 模块的 Activiti 配置类
 */
@Configuration
public class BpmActivitiConfiguration implements ProcessEngineConfigurationConfigurer {

    @Override
    public void configure(SpringProcessEngineConfiguration configuration) {
        // 注册监听器，例如说 ActivitiEventListener 的实现类
//        configuration.setEventListeners(Arrays.asList(processInstanceEventListener));
    }

}
