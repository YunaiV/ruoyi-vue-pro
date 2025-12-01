import CallActivity from './task-components/CallActivity.vue';
import ReceiveTask from './task-components/ReceiveTask.vue';
import ScriptTask from './task-components/ScriptTask.vue';
import ServiceTask from './task-components/ServiceTask.vue';
import UserTask from './task-components/UserTask.vue';

export const installedComponent = {
  UserTask: {
    name: '用户任务',
    component: UserTask,
  },
  ServiceTask: {
    name: '服务任务',
    component: ServiceTask,
  },
  ScriptTask: {
    name: '脚本任务',
    component: ScriptTask,
  },
  ReceiveTask: {
    name: '接收任务',
    component: ReceiveTask,
  },
  CallActivity: {
    name: '调用活动',
    component: CallActivity,
  },
};

export const getTaskCollapseItemName = (
  elementType: keyof typeof installedComponent,
) => {
  return installedComponent[elementType].name;
};

export const isTaskCollapseItemShow = (
  elementType: keyof typeof installedComponent,
) => {
  return installedComponent[elementType];
};
