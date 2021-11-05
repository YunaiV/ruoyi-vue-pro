package cn.iocoder.yudao.framework.activiti.config;

import org.activiti.api.runtime.shared.identity.UserGroupManager;
import org.activiti.image.ProcessDiagramGenerator;
import org.activiti.image.impl.DefaultProcessDiagramGenerator;
import org.activiti.spring.SpringProcessEngineConfiguration;
import org.activiti.spring.boot.ProcessEngineConfigurationConfigurer;
import org.apache.ibatis.session.SqlSessionFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.stereotype.Component;

@Configuration
public class YudaoActivitiConfiguration {



    @Bean
    public ProcessDiagramGenerator processDiagramGenerator (){
        return new DefaultProcessDiagramGenerator();
    }
    @Component
    public static class SqlSessionFactoryProcessEngineConfigurationConfigurer
            implements ProcessEngineConfigurationConfigurer {

        private final SqlSessionFactory sqlSessionFactory;
        public SqlSessionFactoryProcessEngineConfigurationConfigurer(SqlSessionFactory sessionFactory) {
            this.sqlSessionFactory = sessionFactory;
        }

        @Override
        public void configure(SpringProcessEngineConfiguration springProcessEngineConfiguration) {
            springProcessEngineConfiguration.setSqlSessionFactory(sqlSessionFactory);
        }
    }


}
