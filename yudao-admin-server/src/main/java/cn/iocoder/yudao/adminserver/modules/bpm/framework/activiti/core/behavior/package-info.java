/**
 * 拓展 {@link org.activiti.engine.impl.bpmn.behavior.UserTaskActivityBehavior} 实现，基于 {@link cn.iocoder.yudao.adminserver.modules.bpm.dal.dataobject.definition.BpmTaskAssignRuleDO} 实现自定义的任务分配规则。
 * 原因：BPMN 默认的 assign、candidateUsers、candidateGroups 拓展起来有一定的难度，所以选择放弃它们，使用自己定义的规则。
 *
 * @author 芋道源码
 */
package cn.iocoder.yudao.adminserver.modules.bpm.framework.activiti.core.behavior;
