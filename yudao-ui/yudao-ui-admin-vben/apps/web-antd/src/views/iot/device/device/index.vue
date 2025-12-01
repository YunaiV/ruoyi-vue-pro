<script setup lang="ts">
import type { VxeTableGridOptions } from '#/adapter/vxe-table';

import { onMounted, ref } from 'vue';
import { useRoute, useRouter } from 'vue-router';

import { Page, useVbenModal } from '@vben/common-ui';
import { DICT_TYPE } from '@vben/constants';
import { getDictOptions } from '@vben/hooks';
import { IconifyIcon } from '@vben/icons';
import { downloadFileFromBlobPart } from '@vben/utils';

import {
  Button,
  Card,
  Input,
  message,
  Select,
  Space,
  Tag,
} from 'ant-design-vue';

import { ACTION_ICON, TableAction, useVbenVxeGrid } from '#/adapter/vxe-table';
import {
  deleteDevice,
  deleteDeviceList,
  exportDeviceExcel,
  getDevicePage,
} from '#/api/iot/device/device';
import { getSimpleDeviceGroupList } from '#/api/iot/device/group';
import { getSimpleProductList } from '#/api/iot/product/product';
import { $t } from '#/locales';

import { useGridColumns } from './data';
import DeviceCardView from './modules/device-card-view.vue';
import DeviceForm from './modules/device-form.vue';
import DeviceGroupForm from './modules/device-group-form.vue';
import DeviceImportForm from './modules/device-import-form.vue';

/** IoT 设备列表 */
defineOptions({ name: 'IoTDevice' });

const route = useRoute();
const router = useRouter();
const products = ref<any[]>([]);
const deviceGroups = ref<any[]>([]);
const viewMode = ref<'card' | 'list'>('card');
const cardViewRef = ref();

// Modal instances
// TODO @haohao：这个界面，等 product 改完，在一起看看怎么弄更好。
const [DeviceFormModal, deviceFormModalApi] = useVbenModal({
  connectedComponent: DeviceForm,
  destroyOnClose: true,
});

const [DeviceGroupFormModal, deviceGroupFormModalApi] = useVbenModal({
  connectedComponent: DeviceGroupForm,
  destroyOnClose: true,
});

const [DeviceImportFormModal, deviceImportFormModalApi] = useVbenModal({
  connectedComponent: DeviceImportForm,
  destroyOnClose: true,
});

// 搜索参数
const searchParams = ref({
  deviceName: '',
  nickname: '',
  productId: undefined as number | undefined,
  deviceType: undefined as number | undefined,
  status: undefined as number | undefined,
  groupId: undefined as number | undefined,
});

// 获取字典选项
const getIntDictOptions = (dictType: string) => {
  return getDictOptions(dictType, 'number');
};

/** 搜索 */
function handleSearch() {
  if (viewMode.value === 'list') {
    gridApi.formApi.setValues(searchParams.value);
    gridApi.query();
  } else {
    cardViewRef.value?.search(searchParams.value);
  }
}

/** 重置 */
function handleReset() {
  searchParams.value = {
    deviceName: '',
    nickname: '',
    productId: undefined,
    deviceType: undefined,
    status: undefined,
    groupId: undefined,
  };
  handleSearch();
}

/** 刷新 */
function handleRefresh() {
  if (viewMode.value === 'list') {
    gridApi.query();
  } else {
    cardViewRef.value?.reload();
  }
}

/** 导出表格 */
async function handleExport() {
  const data = await exportDeviceExcel(searchParams.value);
  downloadFileFromBlobPart({ fileName: '物联网设备.xls', source: data });
}

/** 打开设备详情 */
function openDetail(id: number) {
  router.push({ name: 'IoTDeviceDetail', params: { id } });
}

/** 跳转到产品详情页面 */
function openProductDetail(productId: number) {
  router.push({ name: 'IoTProductDetail', params: { id: productId } });
}

/** 打开物模型数据 */
function openModel(id: number) {
  router.push({
    name: 'IoTDeviceDetail',
    params: { id },
    query: { tab: 'model' },
  });
}

/** 新增设备 */
function handleCreate() {
  deviceFormModalApi.setData(null).open();
}

/** 编辑设备 */
function handleEdit(row: any) {
  deviceFormModalApi.setData(row).open();
}

/** 删除设备 */
async function handleDelete(row: any) {
  const hideLoading = message.loading({
    content: `正在删除设备...`,
    duration: 0,
  });
  try {
    await deleteDevice(row.id);
    message.success($t('ui.actionMessage.deleteSuccess'));
    handleRefresh();
  } finally {
    hideLoading();
  }
}

/** 批量删除设备 */
async function handleDeleteBatch() {
  const checkedRows = gridApi.grid?.getCheckboxRecords() || [];
  if (checkedRows.length === 0) {
    message.warning('请选择要删除的设备');
    return;
  }
  const hideLoading = message.loading({
    content: '正在批量删除...',
    duration: 0,
  });
  try {
    const ids = checkedRows.map((row: any) => row.id);
    await deleteDeviceList(ids);
    message.success($t('ui.actionMessage.deleteSuccess'));
    handleRefresh();
  } finally {
    hideLoading();
  }
}

/** 添加到分组 */
function handleAddToGroup() {
  const checkedRows = gridApi.grid?.getCheckboxRecords() || [];
  if (checkedRows.length === 0) {
    message.warning('请选择要添加到分组的设备');
    return;
  }
  const ids = checkedRows.map((row: any) => row.id);
  deviceGroupFormModalApi.setData(ids).open();
}

/** 设备导入 */
function handleImport() {
  deviceImportFormModalApi.open();
}

const [Grid, gridApi] = useVbenVxeGrid({
  formOptions: {
    schema: [],
  },
  gridOptions: {
    checkboxConfig: {
      highlight: true,
      reserve: true,
    },
    columns: useGridColumns(),
    height: 'auto',
    keepSource: true,
    proxyConfig: {
      ajax: {
        query: async ({ page }) => {
          return await getDevicePage({
            pageNo: page.currentPage,
            pageSize: page.pageSize,
            ...searchParams.value,
          });
        },
      },
    },
    rowConfig: {
      keyField: 'id',
      isHover: true,
    },
    toolbarConfig: {
      refresh: true,
      search: true,
    },
  } as VxeTableGridOptions,
});

/** 初始化 */
onMounted(async () => {
  // 获取产品列表
  products.value = await getSimpleProductList();
  // 获取分组列表
  deviceGroups.value = await getSimpleDeviceGroupList();

  // 处理 productId 参数
  const { productId } = route.query;
  if (productId) {
    searchParams.value.productId = Number(productId);
    // 自动触发搜索
    handleSearch();
  }
});
</script>

<template>
  <Page auto-content-height>
    <DeviceFormModal @success="handleRefresh" />
    <DeviceGroupFormModal @success="handleRefresh" />
    <DeviceImportFormModal @success="handleRefresh" />

    <!-- 统一搜索工具栏 -->
    <Card :body-style="{ padding: '16px' }" class="mb-4">
      <!-- 搜索表单 -->
      <div class="mb-3 flex flex-wrap items-center gap-3">
        <Select
          v-model:value="searchParams.productId"
          placeholder="请选择产品"
          allow-clear
          style="width: 200px"
        >
          <Select.Option
            v-for="product in products"
            :key="product.id"
            :value="product.id"
          >
            {{ product.name }}
          </Select.Option>
        </Select>
        <Input
          v-model:value="searchParams.deviceName"
          placeholder="请输入 DeviceName"
          allow-clear
          style="width: 200px"
          @press-enter="handleSearch"
        />
        <Input
          v-model:value="searchParams.nickname"
          placeholder="请输入备注名称"
          allow-clear
          style="width: 200px"
          @press-enter="handleSearch"
        />
        <Select
          v-model:value="searchParams.deviceType"
          placeholder="请选择设备类型"
          allow-clear
          style="width: 200px"
        >
          <Select.Option
            v-for="dict in getIntDictOptions(DICT_TYPE.IOT_PRODUCT_DEVICE_TYPE)"
            :key="dict.value"
            :value="dict.value"
          >
            {{ dict.label }}
          </Select.Option>
        </Select>
        <Select
          v-model:value="searchParams.status"
          placeholder="请选择设备状态"
          allow-clear
          style="width: 200px"
        >
          <Select.Option
            v-for="dict in getIntDictOptions(DICT_TYPE.IOT_DEVICE_STATE)"
            :key="dict.value"
            :value="dict.value"
          >
            {{ dict.label }}
          </Select.Option>
        </Select>
        <Select
          v-model:value="searchParams.groupId"
          placeholder="请选择设备分组"
          allow-clear
          style="width: 200px"
        >
          <Select.Option
            v-for="group in deviceGroups"
            :key="group.id"
            :value="group.id"
          >
            {{ group.name }}
          </Select.Option>
        </Select>
        <Button type="primary" @click="handleSearch">
          <IconifyIcon icon="ant-design:search-outlined" class="mr-1" />
          搜索
        </Button>
        <Button @click="handleReset">
          <IconifyIcon icon="ant-design:reload-outlined" class="mr-1" />
          重置
        </Button>
      </div>

      <!-- 操作按钮 -->
      <div class="flex items-center justify-between">
        <Space :size="12">
          <Button
            type="primary"
            @click="handleCreate"
            v-access:code="['iot:device:create']"
          >
            <IconifyIcon icon="ant-design:plus-outlined" class="mr-1" />
            新增
          </Button>
          <Button
            type="primary"
            @click="handleExport"
            v-access:code="['iot:device:export']"
          >
            <IconifyIcon icon="ant-design:download-outlined" class="mr-1" />
            导出
          </Button>
          <Button @click="handleImport" v-access:code="['iot:device:import']">
            <IconifyIcon icon="ant-design:upload-outlined" class="mr-1" />
            导入
          </Button>
          <Button
            v-show="viewMode === 'list'"
            @click="handleAddToGroup"
            v-access:code="['iot:device:update']"
          >
            <IconifyIcon icon="ant-design:folder-add-outlined" class="mr-1" />
            添加到分组
          </Button>
          <Button
            v-show="viewMode === 'list'"
            danger
            @click="handleDeleteBatch"
            v-access:code="['iot:device:delete']"
          >
            <IconifyIcon icon="ant-design:delete-outlined" class="mr-1" />
            批量删除
          </Button>
        </Space>

        <!-- 视图切换 -->
        <Space :size="4">
          <Button
            :type="viewMode === 'card' ? 'primary' : 'default'"
            @click="viewMode = 'card'"
          >
            <IconifyIcon icon="ant-design:appstore-outlined" />
          </Button>
          <Button
            :type="viewMode === 'list' ? 'primary' : 'default'"
            @click="viewMode = 'list'"
          >
            <IconifyIcon icon="ant-design:unordered-list-outlined" />
          </Button>
        </Space>
      </div>
    </Card>

    <Grid v-show="viewMode === 'list'">
      <template #toolbar-tools>
        <div></div>
      </template>

      <!-- 所属产品列 -->
      <template #product="{ row }">
        <a
          class="cursor-pointer text-primary"
          @click="openProductDetail(row.productId)"
        >
          {{ products.find((p: any) => p.id === row.productId)?.name || '-' }}
        </a>
      </template>

      <!-- 所属分组列 -->
      <template #groups="{ row }">
        <template v-if="row.groupIds?.length">
          <Tag
            v-for="groupId in row.groupIds"
            :key="groupId"
            size="small"
            class="mr-1"
          >
            {{ deviceGroups.find((g: any) => g.id === groupId)?.name }}
          </Tag>
        </template>
        <span v-else>-</span>
      </template>

      <!-- 操作列 -->
      <template #actions="{ row }">
        <TableAction
          :actions="[
            {
              label: '查看',
              type: 'link',
              onClick: openDetail.bind(null, row.id),
            },
            {
              label: '日志',
              type: 'link',
              onClick: openModel.bind(null, row.id),
            },
            {
              label: $t('common.edit'),
              type: 'link',
              icon: ACTION_ICON.EDIT,
              onClick: handleEdit.bind(null, row),
            },
            {
              label: $t('common.delete'),
              type: 'link',
              danger: true,
              icon: ACTION_ICON.DELETE,
              popConfirm: {
                title: `确认删除设备吗?`,
                confirm: handleDelete.bind(null, row),
              },
            },
          ]"
        />
      </template>
    </Grid>

    <!-- 卡片视图 -->
    <DeviceCardView
      v-show="viewMode === 'card'"
      ref="cardViewRef"
      :products="products"
      :device-groups="deviceGroups"
      :search-params="searchParams"
      @create="handleCreate"
      @edit="handleEdit"
      @delete="handleDelete"
      @detail="openDetail"
      @model="openModel"
      @product-detail="openProductDetail"
    />
  </Page>
</template>

<style scoped>
:deep(.vxe-toolbar div) {
  z-index: 1;
}

/* 隐藏 VxeGrid 自带的搜索表单区域 */
:deep(.vxe-grid--form-wrapper) {
  display: none !important;
}
</style>
