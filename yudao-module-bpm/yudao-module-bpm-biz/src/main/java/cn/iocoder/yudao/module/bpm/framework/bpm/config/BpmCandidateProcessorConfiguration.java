package cn.iocoder.yudao.module.bpm.framework.bpm.config;

import cn.iocoder.yudao.module.bpm.framework.flowable.core.behavior.script.BpmTaskAssignScript;
import cn.iocoder.yudao.module.bpm.service.candidate.sourceInfoProcessor.*;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

// TODO @芋艿：Candidate 相关还在完善中，用户可以暂时忽略，仅 yudao 开发的同学需要关注~计划是把 Candidate 和 Assign 融合成一套
/**
 * BPM 通用的 Configuration 配置类，提供给 Activiti 和 Flowable
 * @author kyle
 */
@Configuration(proxyBeanMethods = false)
public class BpmCandidateProcessorConfiguration {
    @Bean
    public BpmCandidateAdminUserApiSourceInfoProcessor bpmCandidateAdminUserApiSourceInfoProcessor() {
        return new BpmCandidateAdminUserApiSourceInfoProcessor();
    }

    @Bean
    public BpmCandidateDeptApiSourceInfoProcessor bpmCandidateDeptApiSourceInfoProcessor() {
        return new BpmCandidateDeptApiSourceInfoProcessor();
    }

    @Bean
    public BpmCandidatePostApiSourceInfoProcessor bpmCandidatePostApiSourceInfoProcessor() {
        return new BpmCandidatePostApiSourceInfoProcessor();
    }

    @Bean
    public BpmCandidateRoleApiSourceInfoProcessor bpmCandidateRoleApiSourceInfoProcessor() {
        return new BpmCandidateRoleApiSourceInfoProcessor();
    }

    @Bean
    public BpmCandidateUserGroupApiSourceInfoProcessor bpmCandidateUserGroupApiSourceInfoProcessor() {
        return new BpmCandidateUserGroupApiSourceInfoProcessor();
    }

    /**
     * 可以自己定制脚本，然后通过这里设置到处理器里面去
     * @param scriptsOp 脚本包装对象
     * @return
     */
    @Bean
    public BpmCandidateScriptApiSourceInfoProcessor bpmCandidateScriptApiSourceInfoProcessor(ObjectProvider<BpmTaskAssignScript> scriptsOp) {
        BpmCandidateScriptApiSourceInfoProcessor bpmCandidateScriptApiSourceInfoProcessor = new BpmCandidateScriptApiSourceInfoProcessor();
        bpmCandidateScriptApiSourceInfoProcessor.setScripts(scriptsOp);
        return bpmCandidateScriptApiSourceInfoProcessor;
    }
}
