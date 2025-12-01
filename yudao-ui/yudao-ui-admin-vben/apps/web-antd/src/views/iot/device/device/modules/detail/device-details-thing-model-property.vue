<!-- 设备属性管理 -->
<script setup lang="ts">
import type { IotDeviceApi } from '#/api/iot/device/device';

import { onBeforeUnmount, onMounted, reactive, ref, watch } from 'vue';

import { ContentWrap } from '@vben/common-ui';
import { IconifyIcon } from '@vben/icons';
import { formatDate } from '@vben/utils';

import {
  Button,
  Card,
  Col,
  Divider,
  Input,
  Row,
  Switch,
  Table,
  Tag,
} from 'ant-design-vue';

import { getLatestDeviceProperties } from '#/api/iot/device/device';

import DeviceDetailsThingModelPropertyHistory from './device-details-thing-model-property-history.vue';

const props = defineProps<{ deviceId: number }>();

const loading = ref(true); // 列表的加载中
const list = ref<IotDeviceApi.DevicePropertyDetail[]>([]); // 显示的列表数据
const filterList = ref<IotDeviceApi.DevicePropertyDetail[]>([]); // 完整的数据列表
const queryParams = reactive({
  keyword: '' as string,
});
const autoRefresh = ref(false); // 自动刷新开关
let autoRefreshTimer: any = null; // 定时器
const viewMode = ref<'card' | 'list'>('card'); // 视图模式状态

/** 查询列表 */
async function getList() {
  loading.value = true;
  try {
    const params = {
      deviceId: props.deviceId,
      identifier: undefined as string | undefined,
      name: undefined as string | undefined,
    };
    filterList.value = await getLatestDeviceProperties(params);
    handleFilter();
  } finally {
    loading.value = false;
  }
}

/** 前端筛选数据 */
function handleFilter() {
  if (queryParams.keyword.trim()) {
    const keyword = queryParams.keyword.toLowerCase();
    list.value = filterList.value.filter(
      (item: IotDeviceApi.DevicePropertyDetail) =>
        item.identifier?.toLowerCase().includes(keyword) ||
        item.name?.toLowerCase().includes(keyword),
    );
  } else {
    list.value = filterList.value;
  }
}

/** 搜索按钮操作 */
function handleQuery() {
  handleFilter();
}

/** 历史操作 */
const historyRef = ref();
function openHistory(deviceId: number, identifier: string, dataType: string) {
  historyRef.value.open(deviceId, identifier, dataType);
}

/** 格式化属性值和单位 */
function formatValueWithUnit(item: IotDeviceApi.DevicePropertyDetail) {
  if (item.value === null || item.value === undefined || item.value === '') {
    return '-';
  }
  const unitName = item.dataSpecs?.unitName;
  return unitName ? `${item.value} ${unitName}` : item.value;
}

/** 监听自动刷新 */
watch(autoRefresh, (newValue) => {
  if (newValue) {
    autoRefreshTimer = setInterval(() => {
      getList();
    }, 5000); // 每 5 秒刷新一次
  } else {
    clearInterval(autoRefreshTimer);
    autoRefreshTimer = null;
  }
});

/** 组件卸载时清除定时器 */
onBeforeUnmount(() => {
  if (autoRefreshTimer) {
    clearInterval(autoRefreshTimer);
  }
});

/** 初始化 */
onMounted(() => {
  getList();
});
</script>

<template>
  <ContentWrap>
    <!-- 搜索工作栏 -->
    <div class="flex items-center justify-between" style="margin-bottom: 16px">
      <div class="flex items-center" style="gap: 16px">
        <Input
          v-model:value="queryParams.keyword"
          placeholder="请输入属性名称、标识符"
          allow-clear
          style="width: 240px"
          @press-enter="handleQuery"
        />
        <Switch
          v-model:checked="autoRefresh"
          class="ml-20px"
          checked-children="定时刷新"
          un-checked-children="定时刷新"
        />
      </div>
      <Button.Group>
        <Button
          :type="viewMode === 'card' ? 'primary' : 'default'"
          @click="viewMode = 'card'"
        >
          <IconifyIcon icon="ep:grid" />
        </Button>
        <Button
          :type="viewMode === 'list' ? 'primary' : 'default'"
          @click="viewMode = 'list'"
        >
          <IconifyIcon icon="ep:list" />
        </Button>
      </Button.Group>
    </div>

    <!-- 分隔线 -->
    <Divider style="margin: 16px 0" />

    <!-- 卡片视图 -->
    <template v-if="viewMode === 'card'">
      <Row :gutter="16" v-loading="loading">
        <Col
          v-for="item in list"
          :key="item.identifier"
          :xs="24"
          :sm="12"
          :md="12"
          :lg="6"
          class="mb-4"
        >
          <Card
            class="relative h-full overflow-hidden transition-colors"
            :body-style="{ padding: '0' }"
          >
            <!-- 添加渐变背景层 -->
            <div
              class="pointer-events-none absolute left-0 right-0 top-0 h-12 bg-gradient-to-b from-muted to-transparent"
            ></div>
            <div class="relative p-4">
              <!-- 标题区域 -->
              <div class="mb-3 flex items-center">
                <div class="mr-2.5 flex items-center">
                  <IconifyIcon icon="ep:cpu" class="text-lg text-primary" />
                </div>
                <div class="flex-1 text-base font-bold">{{ item.name }}</div>
                <!-- 标识符 -->
                <div class="mr-2 inline-flex items-center">
                  <Tag size="small" color="blue">
                    {{ item.identifier }}
                  </Tag>
                </div>
                <!-- 数据类型标签 -->
                <div class="mr-2 inline-flex items-center">
                  <Tag size="small">
                    {{ item.dataType }}
                  </Tag>
                </div>
                <!-- 数据图标 - 可点击 -->
                <div
                  class="flex h-8 w-8 cursor-pointer items-center justify-center rounded-full transition-colors hover:bg-blue-50"
                  @click="
                    openHistory(props.deviceId, item.identifier, item.dataType)
                  "
                >
                  <IconifyIcon
                    icon="ep:data-line"
                    class="text-lg text-primary"
                  />
                </div>
              </div>

              <!-- 信息区域 -->
              <div class="text-sm">
                <div class="mb-2.5 last:mb-0">
                  <span class="mr-2.5 text-muted-foreground">属性值</span>
                  <span class="font-bold text-foreground">
                    {{ formatValueWithUnit(item) }}
                  </span>
                </div>
                <div class="mb-2.5 last:mb-0">
                  <span class="mr-2.5 text-muted-foreground">更新时间</span>
                  <span class="text-sm text-foreground">
                    {{ item.updateTime ? formatDate(item.updateTime) : '-' }}
                  </span>
                </div>
              </div>
            </div>
          </Card>
        </Col>
      </Row>
    </template>

    <!-- 列表视图 -->
    <Table v-else v-loading="loading" :data-source="list" :pagination="false">
      <Table.Column title="属性标识符" align="center" data-index="identifier" />
      <Table.Column title="属性名称" align="center" data-index="name" />
      <Table.Column title="数据类型" align="center" data-index="dataType" />
      <Table.Column title="属性值" align="center" data-index="value">
        <template #default="{ record }">
          {{ formatValueWithUnit(record) }}
        </template>
      </Table.Column>
      <Table.Column
        title="更新时间"
        align="center"
        data-index="updateTime"
        :width="180"
      >
        <template #default="{ record }">
          {{ record.updateTime ? formatDate(record.updateTime) : '-' }}
        </template>
      </Table.Column>
      <Table.Column title="操作" align="center">
        <template #default="{ record }">
          <Button
            type="link"
            @click="
              openHistory(props.deviceId, record.identifier, record.dataType)
            "
          >
            查看数据
          </Button>
        </template>
      </Table.Column>
    </Table>

    <!-- 表单弹窗：添加/修改 -->
    <DeviceDetailsThingModelPropertyHistory
      ref="historyRef"
      :device-id="props.deviceId"
    />
  </ContentWrap>
</template>
<style scoped>
/* 移除 a-row 的额外边距 */
:deep(.ant-row) {
  margin-right: -8px !important;
  margin-left: -8px !important;
}
</style>
