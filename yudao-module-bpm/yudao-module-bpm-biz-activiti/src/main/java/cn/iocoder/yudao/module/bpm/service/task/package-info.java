/**
 * task 包下，存放的都是 xxx 实例。例如说：
 * 1. ProcessInstance 是 ProcessDefinition 创建而来的实例；
 * 2. TaskInstance 是 TaskDefinition 创建而来的实例；
 * 3. ActivityInstance 是 BPMN 流程图的每个元素创建的实例；
 *
 * 考虑到 Task 和 Activity 可以比较明确表示名字，所以对应的 Service 就没有使用 Instance 后缀~
 * 嘿嘿，其实也是实现到比较后面的阶段，所以就暂时没去统一和修改了~
 *
 * @author 芋道源码
 */
package cn.iocoder.yudao.module.bpm.service.task;
