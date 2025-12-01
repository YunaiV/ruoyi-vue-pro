<script setup lang="ts">
import type { TableColumnsType } from 'ant-design-vue';

import type { OtaTask } from '#/api/iot/ota/task';
import type { OtaTaskRecord } from '#/api/iot/ota/task/record';

import { computed, reactive, ref } from 'vue';

import { useVbenModal } from '@vben/common-ui';
import { formatDate } from '@vben/utils';

import {
  Card,
  Col,
  Descriptions,
  message,
  Modal,
  Row,
  Table,
  Tabs,
  Tag,
} from 'ant-design-vue';

import { getOtaTask } from '#/api/iot/ota/task';
import {
  cancelOtaTaskRecord,
  getOtaTaskRecordPage,
  getOtaTaskRecordStatusStatistics,
} from '#/api/iot/ota/task/record';
import { IoTOtaTaskRecordStatusEnum } from '#/views/iot/utils/constants';

/** OTA 任务详情组件 */
defineOptions({ name: 'OtaTaskDetail' });

const emit = defineEmits(['success']);

const taskId = ref<number>();
const taskLoading = ref(false);
const task = ref<OtaTask>({} as OtaTask);

const taskStatisticsLoading = ref(false);
const taskStatistics = ref<Record<string, number>>({});

const recordLoading = ref(false);
const recordList = ref<OtaTaskRecord[]>([]);
const recordTotal = ref(0);
const queryParams = reactive({
  pageNo: 1,
  pageSize: 10,
  taskId: undefined as number | undefined,
  status: undefined as number | undefined,
});
const activeTab = ref('');

/** 状态标签配置 */
const statusTabs = computed(() => {
  const tabs = [{ key: '', label: '全部设备' }];
  Object.values(IoTOtaTaskRecordStatusEnum).forEach((status) => {
    tabs.push({
      key: status.value.toString(),
      label: status.label,
    });
  });
  return tabs;
});

/** 表格列配置 */
const columns: TableColumnsType = [
  {
    title: '设备名称',
    dataIndex: 'deviceName',
    key: 'deviceName',
    align: 'center' as const,
  },
  {
    title: '当前版本',
    dataIndex: 'fromFirmwareVersion',
    key: 'fromFirmwareVersion',
    align: 'center' as const,
  },
  {
    title: '升级状态',
    dataIndex: 'status',
    key: 'status',
    align: 'center' as const,
    width: 120,
  },
  {
    title: '升级进度',
    dataIndex: 'progress',
    key: 'progress',
    align: 'center' as const,
    width: 120,
  },
  {
    title: '状态描述',
    dataIndex: 'description',
    key: 'description',
    align: 'center' as const,
  },
  {
    title: '更新时间',
    dataIndex: 'updateTime',
    key: 'updateTime',
    align: 'center' as const,
    width: 180,
    customRender: ({ text }: any) => formatDate(text, 'YYYY-MM-DD HH:mm:ss'),
  },
  {
    title: '操作',
    key: 'action',
    align: 'center' as const,
    width: 80,
  },
];

const [ModalComponent, modalApi] = useVbenModal();

/** 获取任务详情 */
async function getTaskInfo() {
  if (!taskId.value) {
    return;
  }
  taskLoading.value = true;
  try {
    task.value = await getOtaTask(taskId.value);
  } finally {
    taskLoading.value = false;
  }
}

/** 获取统计数据 */
async function getStatistics() {
  if (!taskId.value) {
    return;
  }
  taskStatisticsLoading.value = true;
  try {
    taskStatistics.value = await getOtaTaskRecordStatusStatistics(
      undefined,
      taskId.value,
    );
  } finally {
    taskStatisticsLoading.value = false;
  }
}

/** 获取升级记录列表 */
async function getRecordList() {
  if (!taskId.value) {
    return;
  }
  recordLoading.value = true;
  try {
    queryParams.taskId = taskId.value;
    const data = await getOtaTaskRecordPage(queryParams);
    recordList.value = data.list || [];
    recordTotal.value = data.total || 0;
  } finally {
    recordLoading.value = false;
  }
}

/** 切换标签 */
function handleTabChange(tabKey: number | string) {
  activeTab.value = String(tabKey);
  queryParams.pageNo = 1;
  queryParams.status =
    activeTab.value === '' ? undefined : Number.parseInt(String(tabKey));
  getRecordList();
}

/** 分页变化 */
function handleTableChange(pagination: any) {
  queryParams.pageNo = pagination.current;
  queryParams.pageSize = pagination.pageSize;
  getRecordList();
}

/** 取消升级 */
async function handleCancelUpgrade(record: OtaTaskRecord) {
  Modal.confirm({
    title: '确认取消',
    content: '确认要取消该设备的升级任务吗？',
    async onOk() {
      try {
        await cancelOtaTaskRecord(record.id!);
        message.success('取消成功');
        await getRecordList();
        await getStatistics();
        await getTaskInfo();
        emit('success');
      } catch (error) {
        console.error('取消升级失败', error);
      }
    },
  });
}

/** 打开弹窗 */
function open(id: number) {
  modalApi.open();
  taskId.value = id;
  activeTab.value = '';
  queryParams.pageNo = 1;
  queryParams.status = undefined;

  // 加载数据
  getTaskInfo();
  getStatistics();
  getRecordList();
}

/** 暴露方法 */
defineExpose({ open });
</script>

<template>
  <ModalComponent title="升级任务详情" class="w-5/6">
    <div class="p-4">
      <!-- 任务信息 -->
      <Card title="任务信息" class="mb-5" :loading="taskLoading">
        <Descriptions :column="3" bordered>
          <Descriptions.Item label="任务编号">{{ task.id }}</Descriptions.Item>
          <Descriptions.Item label="任务名称">
            {{ task.name }}
          </Descriptions.Item>
          <Descriptions.Item label="升级范围">
            <Tag v-if="task.deviceScope === 1" color="blue">全部设备</Tag>
            <Tag v-else-if="task.deviceScope === 2" color="green">指定设备</Tag>
            <Tag v-else>{{ task.deviceScope }}</Tag>
          </Descriptions.Item>
          <Descriptions.Item label="任务状态">
            <Tag v-if="task.status === 0" color="orange">待执行</Tag>
            <Tag v-else-if="task.status === 1" color="blue">执行中</Tag>
            <Tag v-else-if="task.status === 2" color="green">已完成</Tag>
            <Tag v-else-if="task.status === 3" color="red">已取消</Tag>
            <Tag v-else>{{ task.status }}</Tag>
          </Descriptions.Item>
          <Descriptions.Item label="创建时间">
            {{
              task.createTime
                ? formatDate(task.createTime, 'YYYY-MM-DD HH:mm:ss')
                : '-'
            }}
          </Descriptions.Item>
          <Descriptions.Item label="任务描述" :span="3">
            {{ task.description }}
          </Descriptions.Item>
        </Descriptions>
      </Card>

      <!-- 任务升级设备统计 -->
      <Card title="升级设备统计" class="mb-5" :loading="taskStatisticsLoading">
        <Row :gutter="20" class="py-5">
          <Col :span="6">
            <div
              class="rounded border border-solid border-gray-200 bg-gray-50 p-5 text-center"
            >
              <div class="mb-2 text-3xl font-bold text-blue-500">
                {{
                  Object.values(taskStatistics).reduce(
                    (sum, count) => sum + (count || 0),
                    0,
                  ) || 0
                }}
              </div>
              <div class="text-sm text-gray-600">升级设备总数</div>
            </div>
          </Col>
          <Col :span="3">
            <div
              class="rounded border border-solid border-gray-200 bg-gray-50 p-5 text-center"
            >
              <div class="mb-2 text-3xl font-bold text-gray-400">
                {{
                  taskStatistics[IoTOtaTaskRecordStatusEnum.PENDING.value] || 0
                }}
              </div>
              <div class="text-sm text-gray-600">待推送</div>
            </div>
          </Col>
          <Col :span="3">
            <div
              class="rounded border border-solid border-gray-200 bg-gray-50 p-5 text-center"
            >
              <div class="mb-2 text-3xl font-bold text-blue-400">
                {{
                  taskStatistics[IoTOtaTaskRecordStatusEnum.PUSHED.value] || 0
                }}
              </div>
              <div class="text-sm text-gray-600">已推送</div>
            </div>
          </Col>
          <Col :span="3">
            <div
              class="rounded border border-solid border-gray-200 bg-gray-50 p-5 text-center"
            >
              <div class="mb-2 text-3xl font-bold text-yellow-500">
                {{
                  taskStatistics[IoTOtaTaskRecordStatusEnum.UPGRADING.value] ||
                  0
                }}
              </div>
              <div class="text-sm text-gray-600">正在升级</div>
            </div>
          </Col>
          <Col :span="3">
            <div
              class="rounded border border-solid border-gray-200 bg-gray-50 p-5 text-center"
            >
              <div class="mb-2 text-3xl font-bold text-green-500">
                {{
                  taskStatistics[IoTOtaTaskRecordStatusEnum.SUCCESS.value] || 0
                }}
              </div>
              <div class="text-sm text-gray-600">升级成功</div>
            </div>
          </Col>
          <Col :span="3">
            <div
              class="rounded border border-solid border-gray-200 bg-gray-50 p-5 text-center"
            >
              <div class="mb-2 text-3xl font-bold text-red-500">
                {{
                  taskStatistics[IoTOtaTaskRecordStatusEnum.FAILURE.value] || 0
                }}
              </div>
              <div class="text-sm text-gray-600">升级失败</div>
            </div>
          </Col>
          <Col :span="3">
            <div
              class="rounded border border-solid border-gray-200 bg-gray-50 p-5 text-center"
            >
              <div class="mb-2 text-3xl font-bold text-gray-400">
                {{
                  taskStatistics[IoTOtaTaskRecordStatusEnum.CANCELED.value] || 0
                }}
              </div>
              <div class="text-sm text-gray-600">升级取消</div>
            </div>
          </Col>
        </Row>
      </Card>

      <!-- 设备管理 -->
      <Card title="升级设备记录">
        <Tabs
          v-model:active-key="activeTab"
          @change="handleTabChange"
          class="mb-4"
        >
          <Tabs.TabPane
            v-for="tab in statusTabs"
            :key="tab.key"
            :tab="tab.label"
          />
        </Tabs>

        <Table
          :columns="columns"
          :data-source="recordList"
          :loading="recordLoading"
          :pagination="{
            current: queryParams.pageNo,
            pageSize: queryParams.pageSize,
            total: recordTotal,
            showSizeChanger: true,
            showQuickJumper: true,
            showTotal: (total: number) => `共 ${total} 条`,
          }"
          @change="handleTableChange"
        >
          <template #bodyCell="{ column, record }">
            <!-- 升级状态 -->
            <template v-if="column.key === 'status'">
              <Tag v-if="record.status === 0" color="default">待推送</Tag>
              <Tag v-else-if="record.status === 1" color="blue">已推送</Tag>
              <Tag v-else-if="record.status === 2" color="processing">
                升级中
              </Tag>
              <Tag v-else-if="record.status === 3" color="success">成功</Tag>
              <Tag v-else-if="record.status === 4" color="error">失败</Tag>
              <Tag v-else-if="record.status === 5" color="warning">已取消</Tag>
              <Tag v-else>{{ record.status }}</Tag>
            </template>

            <!-- 升级进度 -->
            <template v-else-if="column.key === 'progress'">
              {{ record.progress }}%
            </template>

            <!-- 操作 -->
            <template v-else-if="column.key === 'action'">
              <a
                v-if="
                  [
                    IoTOtaTaskRecordStatusEnum.PENDING.value,
                    IoTOtaTaskRecordStatusEnum.PUSHED.value,
                    IoTOtaTaskRecordStatusEnum.UPGRADING.value,
                  ].includes(record.status)
                "
                class="text-red-500"
                @click="handleCancelUpgrade(record)"
              >
                取消
              </a>
            </template>
          </template>
        </Table>
      </Card>
    </div>
  </ModalComponent>
</template>
