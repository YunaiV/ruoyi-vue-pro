<script setup lang="ts">
import { computed, onMounted, reactive, ref } from 'vue';

import { IconifyIcon } from '@vben/icons';

import { Button, Form, Select, Table } from 'ant-design-vue';

import { getSimpleDeviceList } from '#/api/iot/device/device';
import { getSimpleProductList } from '#/api/iot/product/product';
import { getThingModelListByProductId } from '#/api/iot/thingmodel';
import {
  IotDeviceMessageMethodEnum,
  IoTThingModelTypeEnum,
} from '#/views/iot/utils/constants';

const formData = ref<any[]>([]);
const productList = ref<any[]>([]); // 产品列表
const deviceList = ref<any[]>([]); // 设备列表
const thingModelCache = ref<Map<number, any[]>>(new Map()); // 缓存物模型数据，key 为 productId

const formRules: any = reactive({
  productId: [{ required: true, message: '产品不能为空', trigger: 'change' }],
  deviceId: [{ required: true, message: '设备不能为空', trigger: 'change' }],
  method: [{ required: true, message: '消息方法不能为空', trigger: 'change' }],
});
const formRef = ref(); // 表单 Ref

const upstreamMethods = computed(() => {
  return Object.values(IotDeviceMessageMethodEnum).filter(
    (item) => item.upstream,
  );
}); // 获取上行消息方法列表

/** 根据产品 ID 过滤设备 */
function getFilteredDevices(productId: number) {
  if (!productId) return [];
  return deviceList.value.filter(
    (device: any) => device.productId === productId,
  );
}

/** 判断是否需要显示标识符选择器 */
function shouldShowIdentifierSelect(row: any) {
  return [
    IotDeviceMessageMethodEnum.EVENT_POST.method,
    IotDeviceMessageMethodEnum.PROPERTY_POST.method,
  ].includes(row.method);
}

/** 获取物模型选项 */
function getThingModelOptions(row: any) {
  if (!row.productId || !shouldShowIdentifierSelect(row)) {
    return [];
  }
  const thingModels: any[] = thingModelCache.value.get(row.productId) || [];
  let filteredModels: any[] = [];
  if (row.method === IotDeviceMessageMethodEnum.EVENT_POST.method) {
    filteredModels = thingModels.filter(
      (item: any) => item.type === IoTThingModelTypeEnum.EVENT,
    );
  } else if (row.method === IotDeviceMessageMethodEnum.PROPERTY_POST.method) {
    filteredModels = thingModels.filter(
      (item: any) => item.type === IoTThingModelTypeEnum.PROPERTY,
    );
  }
  return filteredModels.map((item: any) => ({
    label: `${item.name} (${item.identifier})`,
    value: item.identifier,
  }));
}

/** 加载产品列表 */
async function loadProductList() {
  try {
    productList.value = await getSimpleProductList();
  } catch (error) {
    console.error('加载产品列表失败:', error);
  }
}

/** 加载设备列表 */
async function loadDeviceList() {
  try {
    deviceList.value = await getSimpleDeviceList();
  } catch (error) {
    console.error('加载设备列表失败:', error);
  }
}

/** 加载物模型数据 */
async function loadThingModel(productId: number) {
  // 已缓存，无需重复加载
  if (thingModelCache.value.has(productId)) {
    return;
  }
  try {
    const thingModels = await getThingModelListByProductId(productId);
    thingModelCache.value.set(productId, thingModels);
  } catch (error) {
    console.error('加载物模型失败:', error);
  }
}

/** 产品变化时处理 */
async function handleProductChange(row: any, _index: number) {
  row.deviceId = 0;
  row.method = undefined;
  row.identifier = undefined;
  row.identifierLoading = false;
}

/** 消息方法变化时处理 */
async function handleMethodChange(row: any, _index: number) {
  // 清空标识符
  row.identifier = undefined;
  // 如果需要加载物模型数据
  if (shouldShowIdentifierSelect(row) && row.productId) {
    row.identifierLoading = true;
    await loadThingModel(row.productId);
    row.identifierLoading = false;
  }
}

/** 新增按钮操作 */
function handleAdd() {
  const row = {
    productId: undefined,
    deviceId: undefined,
    method: undefined,
    identifier: undefined,
    identifierLoading: false,
  };
  formData.value.push(row);
}

/** 删除按钮操作 */
function handleDelete(index: number) {
  formData.value.splice(index, 1);
}

/** 表单校验 */
function validate() {
  return formRef.value.validate();
}

/** 表单值 */
function getData() {
  return formData.value;
}

/** 设置表单值 */
function setData(data: any[]) {
  // 确保每个项都有必要的字段
  formData.value = (data || []).map((item) => ({
    ...item,
    identifierLoading: false,
  }));
  // 为已有数据预加载物模型
  data?.forEach(async (item) => {
    if (item.productId && shouldShowIdentifierSelect(item)) {
      await loadThingModel(item.productId);
    }
  });
}

/** 初始化 */
onMounted(async () => {
  await Promise.all([loadProductList(), loadDeviceList()]);
});

const columns = [
  {
    title: '产品',
    dataIndex: 'productId',
    width: 200,
  },
  {
    title: '设备',
    dataIndex: 'deviceId',
    width: 200,
  },
  {
    title: '消息',
    dataIndex: 'method',
    width: 200,
  },
  {
    title: '标识符',
    dataIndex: 'identifier',
    width: 250,
  },
  {
    title: '操作',
    width: 80,
    fixed: 'right',
  },
];

defineExpose({ validate, getData, setData });
</script>

<template>
  <Form ref="formRef" :model="{ data: formData }">
    <!-- TODO @haohao：貌似有告警。 -->
    <!-- TODO @haohao：是不是搞成 web-antd/src/views/erp/finance/receipt/modules/item-form.vue 的做法，通过 Grid；或 apps/web-antd/src/views/infra/demo/demo03/erp/modules/demo03-grade-list.vue；目的：后续 ele 通用性更好！ -->
    <Table
      :columns="columns"
      :data-source="formData"
      :pagination="false"
      size="small"
      bordered
    >
      <template #bodyCell="{ column, record, index }">
        <template v-if="column.dataIndex === 'productId'">
          <Form.Item
            :name="['data', index, 'productId']"
            :rules="formRules.productId"
            class="mb-0"
          >
            <Select
              v-model:value="record.productId"
              placeholder="请选择产品"
              show-search
              :filter-option="
                (input: string, option: any) =>
                  option.label.toLowerCase().includes(input.toLowerCase())
              "
              :options="
                productList.map((p: any) => ({ label: p.name, value: p.id }))
              "
              @change="() => handleProductChange(record, index)"
            />
          </Form.Item>
        </template>
        <template v-else-if="column.dataIndex === 'deviceId'">
          <Form.Item
            :name="['data', index, 'deviceId']"
            :rules="formRules.deviceId"
            class="mb-0"
          >
            <Select
              v-model:value="record.deviceId"
              placeholder="请选择设备"
              show-search
              :filter-option="
                (input: string, option: any) =>
                  option.label.toLowerCase().includes(input.toLowerCase())
              "
              :options="[
                { label: '全部设备', value: 0 },
                ...getFilteredDevices(record.productId).map((d: any) => ({
                  label: d.deviceName,
                  value: d.id,
                })),
              ]"
            />
          </Form.Item>
        </template>
        <template v-else-if="column.dataIndex === 'method'">
          <Form.Item
            :name="['data', index, 'method']"
            :rules="formRules.method"
            class="mb-0"
          >
            <Select
              v-model:value="record.method"
              placeholder="请选择消息"
              show-search
              :filter-option="
                (input: string, option: any) =>
                  option.label.toLowerCase().includes(input.toLowerCase())
              "
              :options="
                upstreamMethods.map((m: any) => ({
                  label: m.name,
                  value: m.method,
                }))
              "
              @change="() => handleMethodChange(record, index)"
            />
          </Form.Item>
        </template>
        <template v-else-if="column.dataIndex === 'identifier'">
          <Form.Item :name="['data', index, 'identifier']" class="mb-0">
            <Select
              v-if="shouldShowIdentifierSelect(record)"
              v-model:value="record.identifier"
              placeholder="请选择标识符"
              show-search
              :loading="record.identifierLoading"
              :filter-option="
                (input: string, option: any) =>
                  option.label.toLowerCase().includes(input.toLowerCase())
              "
              :options="getThingModelOptions(record)"
            />
          </Form.Item>
        </template>
        <template v-else-if="column.title === '操作'">
          <Button type="link" danger @click="handleDelete(index)">删除</Button>
        </template>
      </template>
    </Table>
    <div class="mt-3 text-center">
      <Button type="primary" @click="handleAdd">
        <IconifyIcon icon="ant-design:plus-outlined" class="mr-1" />
        添加数据源
      </Button>
    </div>
  </Form>
</template>
