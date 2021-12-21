package cn.iocoder.yudao.framework.activiti.config;

import org.activiti.api.runtime.shared.identity.UserGroupManager;
import org.activiti.spring.SpringProcessEngineConfiguration;
import org.activiti.spring.boot.ProcessEngineConfigurationConfigurer;
import org.apache.ibatis.session.SqlSessionFactory;
import org.springframework.context.annotation.Configuration;
import org.springframework.stereotype.Component;

@Configuration
public class YudaoActivitiConfiguration {




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
