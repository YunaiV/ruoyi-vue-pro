<!-- IoT 设备选择，使用弹窗展示 -->
<script setup lang="ts">
import type { IotDeviceApi } from '#/api/iot/device/device';
import type { IotDeviceGroupApi } from '#/api/iot/device/group';
import type { IotProductApi } from '#/api/iot/product/product';

import { computed, onMounted, reactive, ref } from 'vue';

import { ContentWrap } from '@vben/common-ui';
import { DICT_TYPE } from '@vben/constants';
import { getDictOptions } from '@vben/hooks';
import { IconifyIcon } from '@vben/icons';
import { formatDate } from '@vben/utils';

import {
  Button,
  Form,
  Input,
  message,
  Modal,
  Pagination,
  Radio,
  Select,
  Table,
  Tag,
} from 'ant-design-vue';

import { getDevicePage } from '#/api/iot/device/device';
import { getSimpleDeviceGroupList } from '#/api/iot/device/group';
import { getSimpleProductList } from '#/api/iot/product/product';
import { DictTag } from '#/components/dict-tag';

defineOptions({ name: 'IoTDeviceTableSelect' });

const props = defineProps({
  multiple: {
    type: Boolean,
    default: false,
  },
  productId: {
    type: Number,
    default: null,
  },
});

/** 提交表单 */
const emit = defineEmits(['success']);

// 获取字典选项
function getIntDictOptions(dictType: string) {
  return getDictOptions(dictType, 'number');
}

// 日期格式化
function dateFormatter(_row: any, _column: any, cellValue: any) {
  return cellValue ? formatDate(cellValue, 'YYYY-MM-DD HH:mm:ss') : '';
}

const dialogVisible = ref(false);
const dialogTitle = ref('设备选择器');
const formLoading = ref(false);
const loading = ref(true); // 列表的加载中
const list = ref<IotDeviceApi.Device[]>([]); // 列表的数据
const total = ref(0); // 列表的总页数
const selectedDevices = ref<IotDeviceApi.Device[]>([]); // 选中的设备列表
const selectedId = ref<number>(); // 单选模式下选中的ID
const products = ref<IotProductApi.Product[]>([]); // 产品列表
const deviceGroups = ref<IotDeviceGroupApi.DeviceGroup[]>([]); // 设备分组列表
const selectedRowKeys = ref<number[]>([]); // 多选模式下选中的keys

const queryParams = reactive({
  pageNo: 1,
  pageSize: 10,
  deviceName: undefined as string | undefined,
  productId: undefined as number | undefined,
  deviceType: undefined as number | undefined,
  nickname: undefined as string | undefined,
  status: undefined as number | undefined,
  groupId: undefined as number | undefined,
});
const queryFormRef = ref(); // 搜索的表单

// 表格列定义
const columns = computed(() => {
  const baseColumns = [
    {
      title: 'DeviceName',
      dataIndex: 'deviceName',
      key: 'deviceName',
    },
    {
      title: '备注名称',
      dataIndex: 'nickname',
      key: 'nickname',
    },
    {
      title: '所属产品',
      key: 'productId',
    },
    {
      title: '设备类型',
      key: 'deviceType',
    },
    {
      title: '所属分组',
      key: 'groupIds',
    },
    {
      title: '设备状态',
      key: 'status',
    },
    {
      title: '最后上线时间',
      key: 'onlineTime',
      width: 180,
    },
  ];

  // 单选模式添加单选列
  if (!props.multiple) {
    baseColumns.unshift({
      title: '',
      key: 'radio',
      width: 55,
      align: 'center',
    } as any);
  }

  return baseColumns;
});

// 多选配置
const rowSelection = computed(() => ({
  selectedRowKeys: selectedRowKeys.value,
  onChange: (keys: any[], rows: IotDeviceApi.Device[]) => {
    selectedRowKeys.value = keys;
    selectedDevices.value = rows;
  },
}));

/** 查询列表 */
const getList = async () => {
  loading.value = true;
  try {
    if (props.productId) {
      queryParams.productId = props.productId;
    }
    const data = await getDevicePage(queryParams);
    list.value = data.list;
    total.value = data.total;
  } finally {
    loading.value = false;
  }
};

/** 搜索按钮操作 */
const handleQuery = () => {
  queryParams.pageNo = 1;
  getList();
};

/** 重置按钮操作 */
const resetQuery = () => {
  queryFormRef.value.resetFields();
  handleQuery();
};

/** 打开弹窗 */
const open = async () => {
  dialogVisible.value = true;
  // 重置选择状态
  selectedDevices.value = [];
  selectedId.value = undefined;
  selectedRowKeys.value = [];
  if (!props.productId) {
    // 获取产品列表
    products.value = await getSimpleProductList();
  }
  // 获取设备列表
  await getList();
};
defineExpose({ open });

/** 处理行点击事件 */
const tableRef = ref();
function handleRowClick(row: IotDeviceApi.Device) {
  if (!props.multiple) {
    selectedId.value = row.id;
    selectedDevices.value = [row];
  }
}

/** 处理单选变更事件 */
function handleRadioChange(row: IotDeviceApi.Device) {
  selectedId.value = row.id;
  selectedDevices.value = [row];
}

async function submitForm() {
  if (selectedDevices.value.length === 0) {
    message.warning({
      content: props.multiple ? '请至少选择一个设备' : '请选择一个设备',
    });
    return;
  }
  emit(
    'success',
    props.multiple ? selectedDevices.value : selectedDevices.value[0],
  );
  dialogVisible.value = false;
}

/** 初始化 */
onMounted(async () => {
  // 获取产品列表
  products.value = await getSimpleProductList();
  // 获取分组列表
  deviceGroups.value = await getSimpleDeviceGroupList();
});
</script>

<template>
  <Modal
    :title="dialogTitle"
    v-model:open="dialogVisible"
    width="60%"
    :footer="null"
  >
    <ContentWrap>
      <!-- 搜索工作栏 -->
      <Form
        ref="queryFormRef"
        layout="inline"
        :model="queryParams"
        class="-mb-15px"
      >
        <Form.Item v-if="!props.productId" label="产品" name="productId">
          <Select
            v-model:value="queryParams.productId"
            placeholder="请选择产品"
            allow-clear
            style="width: 240px"
          >
            <Select.Option
              v-for="product in products"
              :key="product.id"
              :value="product.id"
            >
              {{ product.name }}
            </Select.Option>
          </Select>
        </Form.Item>
        <Form.Item label="DeviceName" name="deviceName">
          <Input
            v-model:value="queryParams.deviceName"
            placeholder="请输入 DeviceName"
            allow-clear
            @press-enter="handleQuery"
            style="width: 240px"
          />
        </Form.Item>
        <Form.Item label="备注名称" name="nickname">
          <Input
            v-model:value="queryParams.nickname"
            placeholder="请输入备注名称"
            allow-clear
            @press-enter="handleQuery"
            style="width: 240px"
          />
        </Form.Item>
        <Form.Item label="设备类型" name="deviceType">
          <Select
            v-model:value="queryParams.deviceType"
            placeholder="请选择设备类型"
            allow-clear
            style="width: 240px"
          >
            <Select.Option
              v-for="dict in getIntDictOptions(
                DICT_TYPE.IOT_PRODUCT_DEVICE_TYPE,
              )"
              :key="dict.value"
              :value="dict.value"
            >
              {{ dict.label }}
            </Select.Option>
          </Select>
        </Form.Item>
        <Form.Item label="设备状态" name="status">
          <Select
            v-model:value="queryParams.status"
            placeholder="请选择设备状态"
            allow-clear
            style="width: 240px"
          >
            <Select.Option
              v-for="dict in getIntDictOptions(DICT_TYPE.IOT_DEVICE_STATUS)"
              :key="dict.value"
              :value="dict.value"
            >
              {{ dict.label }}
            </Select.Option>
          </Select>
        </Form.Item>
        <Form.Item label="设备分组" name="groupId">
          <Select
            v-model:value="queryParams.groupId"
            placeholder="请选择设备分组"
            allow-clear
            style="width: 240px"
          >
            <Select.Option
              v-for="group in deviceGroups"
              :key="group.id"
              :value="group.id"
            >
              {{ group.name }}
            </Select.Option>
          </Select>
        </Form.Item>
        <Form.Item>
          <Button @click="handleQuery">
            <IconifyIcon class="mr-5px" icon="ep:search" />
            搜索
          </Button>
          <Button @click="resetQuery">
            <IconifyIcon class="mr-5px" icon="ep:refresh" />
            重置
          </Button>
        </Form.Item>
      </Form>
    </ContentWrap>

    <!-- 列表 -->
    <ContentWrap>
      <Table
        ref="tableRef"
        :loading="loading"
        :data-source="list"
        :columns="columns"
        :pagination="false"
        :row-selection="multiple ? rowSelection : undefined"
        @row-click="handleRowClick"
        :row-key="(record: IotDeviceApi.Device) => record.id?.toString() ?? ''"
      >
        <template #bodyCell="{ column, record }">
          <template v-if="column.key === 'radio'">
            <Radio
              :checked="selectedId === record.id"
              @click="() => handleRadioChange(record as IotDeviceApi.Device)"
            />
          </template>
          <template v-else-if="column.key === 'productId'">
            {{ products.find((p) => p.id === record.productId)?.name || '-' }}
          </template>
          <template v-else-if="column.key === 'deviceType'">
            <DictTag
              :type="DICT_TYPE.IOT_PRODUCT_DEVICE_TYPE"
              :value="record.deviceType"
            />
          </template>
          <template v-else-if="column.key === 'groupIds'">
            <template v-if="record.groupIds?.length">
              <Tag
                v-for="id in record.groupIds"
                :key="id"
                class="ml-5px"
                size="small"
              >
                {{ deviceGroups.find((g) => g.id === id)?.name }}
              </Tag>
            </template>
          </template>
          <template v-else-if="column.key === 'status'">
            <DictTag
              :type="DICT_TYPE.IOT_DEVICE_STATUS"
              :value="record.status"
            />
          </template>
          <template v-else-if="column.key === 'onlineTime'">
            {{ dateFormatter(null, null, record.onlineTime) }}
          </template>
        </template>
      </Table>

      <!-- 分页 -->
      <Pagination
        v-model:limit="queryParams.pageSize"
        v-model:page="queryParams.pageNo"
        :total="total"
        @pagination="getList"
      />
    </ContentWrap>

    <template #footer>
      <Button @click="submitForm" type="primary" :disabled="formLoading">
        确 定
      </Button>
      <Button @click="dialogVisible = false">取 消</Button>
    </template>
  </Modal>
</template>
