<!-- 设备消息列表 -->
<script setup lang="ts">
import {
  computed,
  onBeforeUnmount,
  onMounted,
  reactive,
  ref,
  watch,
} from 'vue';

import { ContentWrap } from '@vben/common-ui';
import { DICT_TYPE } from '@vben/constants';
import { IconifyIcon } from '@vben/icons';
import { formatDate } from '@vben/utils';

import {
  Button,
  Form,
  Pagination,
  Select,
  Switch,
  Table,
  Tag,
} from 'ant-design-vue';

import { getDeviceMessagePage } from '#/api/iot/device/device';
import { DictTag } from '#/components/dict-tag';
import { IotDeviceMessageMethodEnum } from '#/views/iot/utils/constants';

const props = defineProps<{
  deviceId: number;
}>();

// 查询参数
const queryParams = reactive({
  deviceId: props.deviceId,
  method: undefined,
  upstream: undefined,
  pageNo: 1,
  pageSize: 10,
});

// 列表数据
const loading = ref(false);
const total = ref(0);
const list = ref<any[]>([]);
const autoRefresh = ref(false); // 自动刷新开关
let autoRefreshTimer: any = null; // 自动刷新定时器

// 消息方法选项
const methodOptions = computed(() => {
  return Object.values(IotDeviceMessageMethodEnum).map((item) => ({
    label: item.name,
    value: item.method,
  }));
});

// 表格列定义
const columns = [
  {
    title: '时间',
    dataIndex: 'ts',
    key: 'ts',
    width: 180,
  },
  {
    title: '上行/下行',
    dataIndex: 'upstream',
    key: 'upstream',
    width: 140,
  },
  {
    title: '是否回复',
    dataIndex: 'reply',
    key: 'reply',
    width: 140,
  },
  {
    title: '请求编号',
    dataIndex: 'requestId',
    key: 'requestId',
    width: 300,
  },
  {
    title: '请求方法',
    dataIndex: 'method',
    key: 'method',
    width: 140,
  },
  {
    title: '请求/响应数据',
    dataIndex: 'params',
    key: 'params',
    ellipsis: true,
  },
];

/** 查询消息列表 */
async function getMessageList() {
  if (!props.deviceId) return;
  loading.value = true;
  try {
    const data = await getDeviceMessagePage(queryParams);
    total.value = data.total;
    list.value = data.list;
  } finally {
    loading.value = false;
  }
}

/** 搜索操作 */
function handleQuery() {
  queryParams.pageNo = 1;
  getMessageList();
}

/** 监听自动刷新 */
watch(autoRefresh, (newValue) => {
  if (newValue) {
    autoRefreshTimer = setInterval(() => {
      getMessageList();
    }, 5000);
  } else {
    clearInterval(autoRefreshTimer);
    autoRefreshTimer = null;
  }
});

/** 监听设备标识变化 */
watch(
  () => props.deviceId,
  (newValue) => {
    if (newValue) {
      handleQuery();
    }
  },
);

/** 组件卸载时清除定时器 */
onBeforeUnmount(() => {
  if (autoRefreshTimer) {
    clearInterval(autoRefreshTimer);
    autoRefreshTimer = null;
  }
});

/** 初始化 */
onMounted(() => {
  if (props.deviceId) {
    getMessageList();
  }
});

/** 刷新消息列表 */
function refresh(delay = 0) {
  if (delay > 0) {
    setTimeout(() => {
      handleQuery();
    }, delay);
  } else {
    handleQuery();
  }
}

/** 暴露方法给父组件 */
defineExpose({
  refresh,
});
</script>

<template>
  <ContentWrap>
    <!-- 搜索区域 -->
    <Form :model="queryParams" layout="inline">
      <Form.Item>
        <Select
          v-model:value="queryParams.method"
          placeholder="所有方法"
          style="width: 160px"
          allow-clear
        >
          <Select.Option
            v-for="item in methodOptions"
            :key="item.value"
            :label="item.label"
            :value="item.value"
          >
            {{ item.label }}
          </Select.Option>
        </Select>
      </Form.Item>
      <Form.Item>
        <Select
          v-model:value="queryParams.upstream"
          placeholder="上行/下行"
          style="width: 160px"
          allow-clear
        >
          <Select.Option label="上行" value="true">上行</Select.Option>
          <Select.Option label="下行" value="false">下行</Select.Option>
        </Select>
      </Form.Item>
      <Form.Item>
        <Button type="primary" @click="handleQuery">
          <IconifyIcon icon="ep:search" class="mr-5px" /> 搜索
        </Button>
        <Switch
          v-model:checked="autoRefresh"
          class="ml-20px"
          checked-children="定时刷新"
          un-checked-children="定时刷新"
        />
      </Form.Item>
    </Form>

    <!-- 消息列表 -->
    <Table
      :loading="loading"
      :data-source="list"
      :columns="columns"
      :pagination="false"
      align="center"
      class="whitespace-nowrap"
    >
      <template #bodyCell="{ column, record }">
        <template v-if="column.key === 'ts'">
          {{ formatDate(record.ts) }}
        </template>
        <template v-else-if="column.key === 'upstream'">
          <Tag :color="record.upstream ? 'blue' : 'green'">
            {{ record.upstream ? '上行' : '下行' }}
          </Tag>
        </template>
        <template v-else-if="column.key === 'reply'">
          <DictTag
            :type="DICT_TYPE.INFRA_BOOLEAN_STRING"
            :value="record.reply"
          />
        </template>
        <template v-else-if="column.key === 'method'">
          {{
            methodOptions.find((item) => item.value === record.method)?.label
          }}
        </template>
        <template v-else-if="column.key === 'params'">
          <span v-if="record.reply">
            {{
              `{"code":${record.code},"msg":"${record.msg}","data":${record.data}\}`
            }}
          </span>
          <span v-else>{{ record.params }}</span>
        </template>
      </template>
    </Table>

    <!-- 分页 -->
    <div class="mt-10px flex justify-end">
      <Pagination
        :total="total"
        v-model:page="queryParams.pageNo"
        v-model:limit="queryParams.pageSize"
        @pagination="getMessageList"
      />
    </div>
  </ContentWrap>
</template>
