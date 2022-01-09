package cn.iocoder.yudao.adminserver.modules.bpm.framework.activiti;

import cn.iocoder.yudao.adminserver.modules.bpm.service.task.listener.BpmTackActivitiEventListener;
import org.activiti.spring.SpringProcessEngineConfiguration;
import org.activiti.spring.boot.ProcessEngineConfigurationConfigurer;
import org.springframework.context.annotation.Configuration;

import javax.annotation.Resource;
import java.util.Collections;

/**
 * BPM 模块的 Activiti 配置类
 */
@Configuration
public class BpmActivitiConfiguration implements ProcessEngineConfigurationConfigurer {

    @Resource
    private BpmTackActivitiEventListener taskActivitiEventListener;

    @Override
    public void configure(SpringProcessEngineConfiguration configuration) {
        // 注册监听器，例如说 BpmActivitiEventListener
        configuration.setEventListeners(Collections.singletonList(taskActivitiEventListener));
    }

}
