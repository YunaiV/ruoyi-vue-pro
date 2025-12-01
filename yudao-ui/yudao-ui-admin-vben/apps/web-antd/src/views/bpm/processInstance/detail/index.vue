<script lang="ts" setup>
import type { BpmProcessInstanceApi } from '#/api/bpm/processInstance';
import type { SystemUserApi } from '#/api/system/user';

import { nextTick, onMounted, ref, shallowRef, watch } from 'vue';

import { Page, useVbenModal } from '@vben/common-ui';
import {
  BpmFieldPermissionType,
  BpmModelFormType,
  BpmModelType,
  BpmTaskStatusEnum,
  DICT_TYPE,
} from '@vben/constants';
import {
  IconifyIcon,
  SvgBpmApproveIcon,
  SvgBpmCancelIcon,
  SvgBpmRejectIcon,
  SvgBpmRunningIcon,
} from '@vben/icons';
import { formatDateTime } from '@vben/utils';

import { Avatar, Card, Col, message, Row, TabPane, Tabs } from 'ant-design-vue';

import {
  getApprovalDetail as getApprovalDetailApi,
  getProcessInstanceBpmnModelView,
} from '#/api/bpm/processInstance';
import { getSimpleUserList } from '#/api/system/user';
import DictTag from '#/components/dict-tag/dict-tag.vue';
import { setConfAndFields2 } from '#/components/form-create';
import { registerComponent } from '#/utils';

import ProcessInstanceBpmnViewer from './modules/bpm-viewer.vue';
import ProcessInstanceOperationButton from './modules/operation-button.vue';
import ProcessssPrint from './modules/process-print.vue';
import ProcessInstanceSimpleViewer from './modules/simple-bpm-viewer.vue';
import BpmProcessInstanceTaskList from './modules/task-list.vue';
import ProcessInstanceTimeline from './modules/time-line.vue';

defineOptions({ name: 'BpmProcessInstanceDetail' });

const props = defineProps<{
  activityId?: string; // 流程活动编号，用于抄送查看
  id: string; // 流程实例的编号
  taskId?: string; // 任务编号
}>();

const processInstanceLoading = ref(false); // 流程实例的加载中
const processInstance = ref<BpmProcessInstanceApi.ProcessInstance>(); // 流程实例
const processDefinition = ref<any>({}); // 流程定义
const processModelView = ref<any>({}); // 流程模型视图
const operationButtonRef = ref(); // 操作按钮组件 ref
const activeTab = ref('form');
const taskListRef = ref();
const auditIconsMap: {
  [key: string]:
    | typeof SvgBpmApproveIcon
    | typeof SvgBpmCancelIcon
    | typeof SvgBpmRejectIcon
    | typeof SvgBpmRunningIcon;
} = {
  [BpmTaskStatusEnum.RUNNING]: SvgBpmRunningIcon,
  [BpmTaskStatusEnum.APPROVE]: SvgBpmApproveIcon,
  [BpmTaskStatusEnum.REJECT]: SvgBpmRejectIcon,
  [BpmTaskStatusEnum.CANCEL]: SvgBpmCancelIcon,
  [BpmTaskStatusEnum.APPROVING]: SvgBpmApproveIcon,
  [BpmTaskStatusEnum.RETURN]: SvgBpmRejectIcon,
  [BpmTaskStatusEnum.WAIT]: SvgBpmRunningIcon,
};
const activityNodes = ref<BpmProcessInstanceApi.ApprovalNodeInfo[]>([]); // 审批节点信息
const userOptions = ref<SystemUserApi.User[]>([]); // 用户列表

const fApi = ref<any>();
const detailForm = ref({
  rule: [],
  option: {},
  value: {},
}); // 流程实例的表单详情
const writableFields: Array<string> = []; // 表单可以编辑的字段

const BusinessFormComponent = shallowRef<any>(null); // 异步组件(业务表单）

/** 获取详情 */
async function getDetail() {
  // 获得审批详情
  await getApprovalDetail();
  // 获得流程模型视图
  await getProcessModelView();
}

/** 获得审批详情 */
async function getApprovalDetail() {
  processInstanceLoading.value = true;
  try {
    const param = {
      processInstanceId: props.id,
      activityId: props.activityId,
      taskId: props.taskId,
    };
    const data = await getApprovalDetailApi(param);
    if (!data) {
      message.error('查询不到审批详情信息！');
    }
    if (!data.processDefinition || !data.processInstance) {
      message.error('查询不到流程信息！');
    }

    processInstance.value = data.processInstance;
    processDefinition.value = data.processDefinition;

    // 设置表单信息
    if (processDefinition.value.formType === BpmModelFormType.NORMAL) {
      // 获取表单字段权限
      const formFieldsPermission = data.formFieldsPermission;
      // 清空可编辑字段为空
      writableFields.splice(0);
      if (detailForm.value.rule?.length > 0) {
        // 避免刷新 form-create 显示不了
        detailForm.value.value = processInstance.value.formVariables;
      } else {
        setConfAndFields2(
          detailForm,
          processDefinition.value.formConf,
          processDefinition.value.formFields,
          processInstance.value.formVariables,
        );
      }
      await nextTick();
      fApi.value?.btn.show(false);
      fApi.value?.resetBtn.show(false);
      fApi.value?.disabled(true);
      // 设置表单字段权限
      if (formFieldsPermission) {
        Object.keys(data.formFieldsPermission).forEach((item) => {
          setFieldPermission(item, formFieldsPermission[item]);
        });
      }
    } else {
      // 注意：data.processDefinition.formCustomViewPath 是组件的全路径，例如说：/crm/contract/detail/index.vue
      BusinessFormComponent.value = registerComponent(
        data?.processDefinition?.formCustomViewPath || '',
      );
    }

    // 获取审批节点，显示 Timeline 的数据
    activityNodes.value = data.activityNodes;

    // 获取待办任务显示操作按钮
    operationButtonRef.value?.loadTodoTask(data.todoTask);
  } catch {
    message.error('获取审批详情失败！');
  } finally {
    processInstanceLoading.value = false;
  }
}

/** 获取流程模型视图*/
async function getProcessModelView() {
  if (BpmModelType.BPMN === processDefinition.value?.modelType) {
    // 重置，解决 BPMN 流程图刷新不会重新渲染问题
    processModelView.value = {
      bpmnXml: '',
    };
  }
  const data = await getProcessInstanceBpmnModelView(props.id);
  if (data) {
    processModelView.value = data;
  }
}

/** 设置表单权限 */
function setFieldPermission(field: string, permission: string) {
  if (permission === BpmFieldPermissionType.READ) {
    fApi.value?.disabled(true, field);
  }
  if (permission === BpmFieldPermissionType.WRITE) {
    fApi.value?.disabled(false, field);
    // 加入可以编辑的字段
    writableFields.push(field);
  }
  if (permission === BpmFieldPermissionType.NONE) {
    fApi.value?.hidden(true, field);
  }
}

/** 操作成功后刷新 */
const refresh = () => {
  // 重新获取详情
  getDetail();
};

const [PrintModal, printModalApi] = useVbenModal({
  connectedComponent: ProcessssPrint,
  destroyOnClose: true,
});

/** 打开打印对话框 */
function handlePrint() {
  printModalApi.setData({ processInstanceId: props.id }).open();
}

/** 监听 Tab 切换，当切换到 "record" 标签时刷新任务列表 */
watch(
  () => activeTab.value,
  async (newVal) => {
    if (newVal === 'record') {
      // 如果切换到流转记录标签，刷新任务列表
      await nextTick();
      taskListRef.value?.refresh();
    }
  },
);

/** 初始化 */
onMounted(async () => {
  await getDetail();
  // 获得用户列表
  userOptions.value = await getSimpleUserList();
});
</script>

<template>
  <Page auto-content-height>
    <Card
      :body-style="{
        overflowY: 'auto',
        paddingTop: '12px',
      }"
    >
      <template #title>
        <div class="flex items-center gap-4">
          <span class="text-gray-500">编号：{{ id || '-' }}</span>
          <IconifyIcon
            icon="lucide:printer"
            class="cursor-pointer hover:text-primary"
            @click="handlePrint"
          />
        </div>
      </template>

      <div class="flex h-full flex-col">
        <!-- 流程基本信息 -->
        <div class="flex flex-col gap-2">
          <div class="mb-2.5 flex h-10 items-center gap-5">
            <div class="mb-1 text-2xl font-bold">
              {{ processInstance?.name }}
            </div>
            <DictTag
              v-if="processInstance?.status"
              :type="DICT_TYPE.BPM_PROCESS_INSTANCE_STATUS"
              :value="processInstance.status"
            />
          </div>

          <div class="mb-2.5 flex h-12 items-center gap-5 text-sm">
            <div
              class="flex items-center gap-2 rounded-3xl bg-gray-100 px-2.5 py-1 dark:bg-gray-600"
            >
              <Avatar
                :size="28"
                v-if="processInstance?.startUser?.avatar"
                :src="processInstance?.startUser?.avatar"
              />
              <Avatar
                :size="28"
                v-else-if="processInstance?.startUser?.nickname"
              >
                {{ processInstance?.startUser?.nickname.substring(0, 1) }}
              </Avatar>
              <span class="text-sm">
                {{ processInstance?.startUser?.nickname }}
              </span>
            </div>
            <div class="text-gray-500">
              {{ formatDateTime(processInstance?.startTime) }} 提交
            </div>
          </div>

          <component
            v-if="processInstance?.status"
            :is="auditIconsMap[processInstance?.status]"
            class="absolute right-5 top-2.5 size-36"
          />
        </div>

        <!-- 流程操作 -->
        <div class="process-tabs-container flex flex-1 flex-col">
          <Tabs v-model:active-key="activeTab" class="mt-0 h-full">
            <TabPane tab="审批详情" key="form" class="tab-pane-content">
              <Row :gutter="[48, 24]" class="h-full">
                <Col
                  :xs="24"
                  :sm="24"
                  :md="18"
                  :lg="18"
                  :xl="16"
                  class="h-full"
                >
                  <!-- 流程表单 -->
                  <div
                    v-if="
                      processDefinition?.formType === BpmModelFormType.NORMAL
                    "
                    class="h-full"
                  >
                    <form-create
                      v-model="detailForm.value"
                      v-model:api="fApi"
                      :option="detailForm.option"
                      :rule="detailForm.rule"
                    />
                  </div>
                  <div
                    v-else-if="
                      processDefinition?.formType === BpmModelFormType.CUSTOM
                    "
                    class="h-full"
                  >
                    <BusinessFormComponent :id="processInstance?.businessKey" />
                  </div>
                </Col>
                <Col :xs="24" :sm="24" :md="6" :lg="6" :xl="8" class="h-full">
                  <div class="mt-4 h-full">
                    <ProcessInstanceTimeline :activity-nodes="activityNodes" />
                  </div>
                </Col>
              </Row>
            </TabPane>
            <TabPane
              tab="流程图"
              key="diagram"
              class="tab-pane-content"
              :force-render="true"
            >
              <div class="h-full">
                <ProcessInstanceSimpleViewer
                  v-show="
                    processDefinition.modelType &&
                    processDefinition.modelType === BpmModelType.SIMPLE
                  "
                  :loading="processInstanceLoading"
                  :model-view="processModelView"
                />
                <ProcessInstanceBpmnViewer
                  v-show="
                    processDefinition.modelType &&
                    processDefinition.modelType === BpmModelType.BPMN
                  "
                  :loading="processInstanceLoading"
                  :model-view="processModelView"
                />
              </div>
            </TabPane>
            <TabPane tab="流转记录" key="record" class="tab-pane-content">
              <div class="h-full">
                <BpmProcessInstanceTaskList
                  ref="taskListRef"
                  :loading="processInstanceLoading"
                  :id="id"
                />
              </div>
            </TabPane>
            <!-- TODO 待开发 -->
            <TabPane
              tab="流转评论"
              key="comment"
              v-if="false"
              class="tab-pane-content"
            >
              <div class="h-full">待开发</div>
            </TabPane>
          </Tabs>
        </div>
      </div>

      <template #actions>
        <div class="px-4">
          <ProcessInstanceOperationButton
            ref="operationButtonRef"
            :process-instance="processInstance"
            :process-definition="processDefinition"
            :user-options="userOptions"
            :normal-form="detailForm"
            :normal-form-api="fApi"
            :writable-fields="writableFields"
            @success="refresh"
          />
        </div>
      </template>
    </Card>
    <!-- 打印对话框 -->
    <PrintModal />
  </Page>
</template>

<style lang="scss" scoped>
// @jason：看看能不能通过 tailwindcss 简化下
.ant-tabs-content {
  height: 100%;
}

.process-tabs-container {
  display: flex;
  flex-direction: column;
  height: 100%;
}

:deep(.ant-tabs) {
  display: flex;
  flex-direction: column;
  height: 100%;
}

:deep(.ant-tabs-content) {
  flex: 1;
  overflow-y: auto;
}

:deep(.ant-tabs-tabpane) {
  height: 100%;
}

.tab-pane-content {
  height: calc(100vh - 420px);
  padding-right: 12px;
  overflow: hidden auto;
}
</style>
