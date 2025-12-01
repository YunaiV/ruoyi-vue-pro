<!-- 设备事件管理 -->
<script setup lang="ts">
import type { ThingModelData } from '#/api/iot/thingmodel';

import { computed, onMounted, reactive, ref } from 'vue';

import { ContentWrap } from '@vben/common-ui';
import { IconifyIcon } from '@vben/icons';
import { formatDate } from '@vben/utils';

import {
  Button,
  Divider,
  Form,
  Pagination,
  RangePicker,
  Select,
  Table,
  Tag,
} from 'ant-design-vue';

import { getDeviceMessagePairPage } from '#/api/iot/device/device';
import {
  getEventTypeLabel,
  IotDeviceMessageMethodEnum,
  IoTThingModelTypeEnum,
} from '#/views/iot/utils/constants';

const props = defineProps<{
  deviceId: number;
  thingModelList: ThingModelData[];
}>();

const loading = ref(false); // 列表的加载中
const total = ref(0); // 列表的总页数
const list = ref([] as any[]); // 列表的数据
const queryParams = reactive({
  deviceId: props.deviceId,
  method: IotDeviceMessageMethodEnum.EVENT_POST.method, // 固定筛选事件消息
  identifier: '',
  times: undefined,
  pageNo: 1,
  pageSize: 10,
});
const queryFormRef = ref(); // 搜索的表单

/** 事件类型的物模型数据 */
const eventThingModels = computed(() => {
  return props.thingModelList.filter(
    (item: ThingModelData) =>
      String(item.type) === String(IoTThingModelTypeEnum.EVENT),
  );
});

/** 查询列表 */
async function getList() {
  if (!props.deviceId) return;
  loading.value = true;
  try {
    const data = await getDeviceMessagePairPage(queryParams);
    list.value = data.list || [];
    total.value = data.total || 0;
  } finally {
    loading.value = false;
  }
}

/** 搜索按钮操作 */
function handleQuery() {
  queryParams.pageNo = 1;
  getList();
}

/** 重置按钮操作 */
function resetQuery() {
  queryFormRef.value?.resetFields();
  queryParams.identifier = '';
  queryParams.times = undefined;
  handleQuery();
}

/** 获取事件名称 */
function getEventName(identifier: string | undefined) {
  if (!identifier) return '-';
  const event = eventThingModels.value.find(
    (item: ThingModelData) => item.identifier === identifier,
  );
  return event?.name || identifier;
}

/** 获取事件类型 */
function getEventType(identifier: string | undefined) {
  if (!identifier) return '-';
  const event = eventThingModels.value.find(
    (item: ThingModelData) => item.identifier === identifier,
  );
  if (!event?.event?.type) return '-';
  return getEventTypeLabel(event.event.type) || '-';
}

/** 解析参数 */
function parseParams(params: string) {
  try {
    const parsed = JSON.parse(params);
    if (parsed.params) {
      return parsed.params;
    }
    return parsed;
  } catch {
    return {};
  }
}

/** 初始化 */
onMounted(() => {
  getList();
});
</script>

<template>
  <ContentWrap>
    <!-- 搜索工作栏 -->
    <Form
      :model="queryParams"
      ref="queryFormRef"
      layout="inline"
      @submit.prevent
      style="margin-bottom: 16px"
    >
      <Form.Item label="标识符" name="identifier">
        <Select
          v-model:value="queryParams.identifier"
          placeholder="请选择事件标识符"
          allow-clear
          style="width: 240px"
        >
          <Select.Option
            v-for="event in eventThingModels"
            :key="event.identifier"
            :value="event.identifier!"
          >
            {{ event.name }}({{ event.identifier }})
          </Select.Option>
        </Select>
      </Form.Item>
      <Form.Item label="时间范围" name="times">
        <RangePicker
          v-model:value="queryParams.times"
          show-time
          format="YYYY-MM-DD HH:mm:ss"
          value-format="YYYY-MM-DD HH:mm:ss"
          style="width: 360px"
        />
      </Form.Item>
      <Form.Item>
        <Button type="primary" @click="handleQuery">
          <template #icon>
            <IconifyIcon icon="ep:search" />
          </template>
          搜索
        </Button>
        <Button @click="resetQuery" style="margin-left: 8px">
          <template #icon>
            <IconifyIcon icon="ep:refresh" />
          </template>
          重置
        </Button>
      </Form.Item>
    </Form>

    <Divider style="margin: 16px 0" />

    <!-- 事件列表 -->
    <Table v-loading="loading" :data-source="list" :pagination="false">
      <Table.Column
        title="上报时间"
        align="center"
        data-index="reportTime"
        :width="180"
      >
        <template #default="{ record }">
          {{
            record.request?.reportTime
              ? formatDate(record.request.reportTime)
              : '-'
          }}
        </template>
      </Table.Column>
      <Table.Column
        title="标识符"
        align="center"
        data-index="identifier"
        :width="160"
      >
        <template #default="{ record }">
          <Tag color="blue" size="small">
            {{ record.request?.identifier }}
          </Tag>
        </template>
      </Table.Column>
      <Table.Column
        title="事件名称"
        align="center"
        data-index="eventName"
        :width="160"
      >
        <template #default="{ record }">
          {{ getEventName(record.request?.identifier) }}
        </template>
      </Table.Column>
      <Table.Column
        title="事件类型"
        align="center"
        data-index="eventType"
        :width="100"
      >
        <template #default="{ record }">
          {{ getEventType(record.request?.identifier) }}
        </template>
      </Table.Column>
      <Table.Column title="输入参数" align="center" data-index="params">
        <template #default="{ record }">
          {{ parseParams(record.request.params) }}
        </template>
      </Table.Column>
    </Table>

    <!-- 分页 -->
    <Pagination
      :total="total"
      v-model:page="queryParams.pageNo"
      v-model:limit="queryParams.pageSize"
      @pagination="getList"
    />
  </ContentWrap>
</template>
