<!-- 设备服务调用 -->
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
  Select,
  Table,
  Tag,
} from 'ant-design-vue';

import { getDeviceMessagePairPage } from '#/api/iot/device/device';
import {
  getThingModelServiceCallTypeLabel,
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
  method: IotDeviceMessageMethodEnum.SERVICE_INVOKE.method, // 固定筛选服务调用消息
  identifier: '',
  times: [] as any[],
  pageNo: 1,
  pageSize: 10,
});
const queryFormRef = ref(); // 搜索的表单

/** 服务类型的物模型数据 */
const serviceThingModels = computed(() => {
  return props.thingModelList.filter(
    (item: ThingModelData) =>
      String(item.type) === String(IoTThingModelTypeEnum.SERVICE),
  );
});

/** 查询列表 */
async function getList() {
  if (!props.deviceId) return;
  loading.value = true;
  try {
    const data = await getDeviceMessagePairPage(queryParams);
    list.value = data.list || [];
    total.value = data.total;
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
  queryParams.times = [];
  handleQuery();
}

/** 获取服务名称 */
function getServiceName(identifier: string | undefined) {
  if (!identifier) return '-';
  const service = serviceThingModels.value.find(
    (item: ThingModelData) => item.identifier === identifier,
  );
  return service?.name || identifier;
}

/** 获取调用方式 */
function getCallType(identifier: string | undefined) {
  if (!identifier) return '-';
  const service = serviceThingModels.value.find(
    (item: ThingModelData) => item.identifier === identifier,
  );
  if (!service?.service?.callType) return '-';
  return getThingModelServiceCallTypeLabel(service.service.callType) || '-';
}

/** 解析参数 */
function parseParams(params: string) {
  if (!params) return '-';
  try {
    const parsed = JSON.parse(params);
    if (parsed.params) {
      return JSON.stringify(parsed.params, null, 2);
    }
    return JSON.stringify(parsed, null, 2);
  } catch {
    return params;
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
          placeholder="请选择服务标识符"
          allow-clear
          style="width: 240px"
        >
          <Select.Option
            v-for="service in serviceThingModels"
            :key="service.identifier"
            :value="service.identifier!"
          >
            {{ service.name }}({{ service.identifier }})
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

    <!-- 服务调用列表 -->
    <Table v-loading="loading" :data-source="list" :pagination="false">
      <Table.Column
        title="调用时间"
        align="center"
        data-index="requestTime"
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
        title="响应时间"
        align="center"
        data-index="responseTime"
        :width="180"
      >
        <template #default="{ record }">
          {{
            record.reply?.reportTime ? formatDate(record.reply.reportTime) : '-'
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
        title="服务名称"
        align="center"
        data-index="serviceName"
        :width="160"
      >
        <template #default="{ record }">
          {{ getServiceName(record.request?.identifier) }}
        </template>
      </Table.Column>
      <Table.Column
        title="调用方式"
        align="center"
        data-index="callType"
        :width="100"
      >
        <template #default="{ record }">
          {{ getCallType(record.request?.identifier) }}
        </template>
      </Table.Column>
      <Table.Column title="输入参数" align="center" data-index="inputParams">
        <template #default="{ record }">
          {{ parseParams(record.request?.params) }}
        </template>
      </Table.Column>
      <Table.Column title="输出参数" align="center" data-index="outputParams">
        <template #default="{ record }">
          <span v-if="record.reply">
            {{
              `{"code":${record.reply.code},"msg":"${record.reply.msg}","data":${record.reply.data}\}`
            }}
          </span>
          <span v-else>-</span>
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
