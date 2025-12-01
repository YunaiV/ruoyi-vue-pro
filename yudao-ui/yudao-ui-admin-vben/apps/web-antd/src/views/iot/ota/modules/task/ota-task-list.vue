<script setup lang="ts">
import type { TableColumnsType } from 'ant-design-vue';

import type { OtaTask } from '#/api/iot/ota/task';

import { onMounted, reactive, ref } from 'vue';

import { IconifyIcon } from '@vben/icons';
import { formatDate } from '@vben/utils';

import {
  Button,
  Card,
  Input,
  message,
  Modal,
  Space,
  Table,
  Tag,
} from 'ant-design-vue';

import { getOtaTaskPage } from '#/api/iot/ota/task';
import { IoTOtaTaskStatusEnum } from '#/views/iot/utils/constants';

import OtaTaskDetail from './ota-task-detail.vue';
import OtaTaskForm from './ota-task-form.vue';

/** IoT OTA 任务列表 */
defineOptions({ name: 'OtaTaskList' });

const props = defineProps<{
  firmwareId: number;
  productId: number;
}>();

const emit = defineEmits(['success']);

// 任务列表
const taskLoading = ref(false);
const taskList = ref<OtaTask[]>([]);
const taskTotal = ref(0);
const queryParams = reactive({
  pageNo: 1,
  pageSize: 10,
  name: '',
  firmwareId: props.firmwareId,
});
const taskFormRef = ref(); // 任务表单引用
const taskDetailRef = ref(); // 任务详情引用

/** 获取任务列表 */
async function getTaskList() {
  taskLoading.value = true;
  try {
    const data = await getOtaTaskPage(queryParams);
    taskList.value = data.list;
    taskTotal.value = data.total;
  } finally {
    taskLoading.value = false;
  }
}

/** 搜索 */
function handleQuery() {
  queryParams.pageNo = 1;
  getTaskList();
}

/** 打开任务表单 */
function openTaskForm() {
  taskFormRef.value?.open();
}

/** 处理任务创建成功 */
function handleTaskCreateSuccess() {
  getTaskList();
  emit('success');
}

/** 查看任务详情 */
function handleTaskDetail(id: number) {
  taskDetailRef.value?.open(id);
}

/** 取消任务 */
async function handleCancelTask(id: number) {
  Modal.confirm({
    title: '确认取消',
    content: '确认要取消该升级任务吗？',
    async onOk() {
      try {
        await IoTOtaTaskApi.cancelOtaTask(id);
        message.success('取消成功');
        await refresh();
      } catch (error) {
        console.error('取消任务失败', error);
      }
    },
  });
}

/** 刷新数据 */
async function refresh() {
  await getTaskList();
  emit('success');
}

/** 分页变化 */
function handleTableChange(pagination: any) {
  queryParams.pageNo = pagination.current;
  queryParams.pageSize = pagination.pageSize;
  getTaskList();
}

/** 表格列配置 */
const columns: TableColumnsType = [
  {
    title: '任务编号',
    dataIndex: 'id',
    key: 'id',
    width: 80,
    align: 'center' as const,
  },
  {
    title: '任务名称',
    dataIndex: 'name',
    key: 'name',
    align: 'center' as const,
  },
  {
    title: '升级范围',
    dataIndex: 'deviceScope',
    key: 'deviceScope',
    align: 'center' as const,
  },
  {
    title: '升级进度',
    key: 'progress',
    align: 'center' as const,
  },
  {
    title: '创建时间',
    dataIndex: 'createTime',
    key: 'createTime',
    align: 'center' as const,
    customRender: ({ text }: any) => formatDate(text, 'YYYY-MM-DD HH:mm:ss'),
  },
  {
    title: '任务描述',
    dataIndex: 'description',
    key: 'description',
    align: 'center' as const,
    ellipsis: true,
  },
  {
    title: '任务状态',
    dataIndex: 'status',
    key: 'status',
    align: 'center' as const,
  },
  {
    title: '操作',
    key: 'action',
    align: 'center' as const,
    width: 120,
  },
];

/** 初始化 */
onMounted(() => {
  getTaskList();
});
</script>

<template>
  <Card title="升级任务管理" class="mb-5">
    <!-- 搜索栏 -->
    <div class="mb-4 flex items-center justify-between">
      <Button type="primary" @click="openTaskForm">
        <IconifyIcon icon="ant-design:plus-outlined" class="mr-1" />
        新增
      </Button>
      <Input
        v-model:value="queryParams.name"
        placeholder="请输入任务名称"
        allow-clear
        @press-enter="handleQuery"
        style="width: 240px"
      />
    </div>

    <!-- 任务列表 -->
    <Table
      :columns="columns"
      :data-source="taskList"
      :loading="taskLoading"
      :pagination="{
        current: queryParams.pageNo,
        pageSize: queryParams.pageSize,
        total: taskTotal,
        showSizeChanger: true,
        showQuickJumper: true,
        showTotal: (total: number) => `共 ${total} 条`,
      }"
      :scroll="{ x: 'max-content' }"
      @change="handleTableChange"
    >
      <!-- 升级范围 -->
      <template #bodyCell="{ column, record }">
        <template v-if="column.key === 'deviceScope'">
          <Tag v-if="record.deviceScope === 1" color="blue">全部设备</Tag>
          <Tag v-else-if="record.deviceScope === 2" color="green">指定设备</Tag>
          <Tag v-else>{{ record.deviceScope }}</Tag>
        </template>

        <!-- 升级进度 -->
        <template v-else-if="column.key === 'progress'">
          {{ record.deviceSuccessCount }}/{{ record.deviceTotalCount }}
        </template>

        <!-- 任务状态 -->
        <template v-else-if="column.key === 'status'">
          <Tag v-if="record.status === 0" color="orange">待执行</Tag>
          <Tag v-else-if="record.status === 1" color="blue">执行中</Tag>
          <Tag v-else-if="record.status === 2" color="green">已完成</Tag>
          <Tag v-else-if="record.status === 3" color="red">已取消</Tag>
          <Tag v-else>{{ record.status }}</Tag>
        </template>

        <!-- 操作 -->
        <template v-else-if="column.key === 'action'">
          <Space>
            <a @click="handleTaskDetail(record.id)">详情</a>
            <a
              v-if="record.status === IoTOtaTaskStatusEnum.IN_PROGRESS.value"
              class="text-red-500"
              @click="handleCancelTask(record.id)"
            >
              取消
            </a>
          </Space>
        </template>
      </template>
    </Table>

    <!-- 新增任务弹窗 -->
    <OtaTaskForm
      ref="taskFormRef"
      :firmware-id="firmwareId"
      :product-id="productId"
      @success="handleTaskCreateSuccess"
    />

    <!-- 任务详情弹窗 -->
    <OtaTaskDetail ref="taskDetailRef" @success="refresh" />
  </Card>
</template>
