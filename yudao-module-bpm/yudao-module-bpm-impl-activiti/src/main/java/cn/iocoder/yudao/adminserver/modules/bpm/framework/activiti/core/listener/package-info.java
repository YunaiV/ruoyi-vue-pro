/**
 * 自定义各种 Activiti 的监听器，实现流程示例、流程任务的拓展表信息的同步
 * 例如说，{@link org.activiti.api.task.model.Task} 新建时，我们也要新建对应的 {@link cn.iocoder.yudao.adminserver.modules.bpm.dal.dataobject.task.BpmTaskExtDO} 记录
 *
 * @author 芋道源码
 */
package cn.iocoder.yudao.adminserver.modules.bpm.framework.activiti.core.listener;
