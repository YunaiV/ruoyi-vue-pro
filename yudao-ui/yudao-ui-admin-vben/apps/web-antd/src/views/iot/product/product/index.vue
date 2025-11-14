<script setup lang="ts">
import type { VxeTableGridOptions } from '#/adapter/vxe-table';

import { onMounted, ref } from 'vue';
import { useRouter } from 'vue-router';

import { Page, useVbenModal } from '@vben/common-ui';
import { IconifyIcon } from '@vben/icons';
import { downloadFileFromBlobPart } from '@vben/utils';

import { Button, Card, Image, Input, message, Space } from 'ant-design-vue';

import { ACTION_ICON, TableAction, useVbenVxeGrid } from '#/adapter/vxe-table';
import {
  deleteProduct,
  exportProduct,
  getProductPage,
} from '#/api/iot/product/product';
import { getSimpleProductCategoryList } from '#/api/iot/product/category';
import { $t } from '#/locales';

import { useGridColumns, useImagePreview } from './data';
// @ts-ignore
import ProductCardView from './modules/ProductCardView.vue';
import ProductForm from './modules/ProductForm.vue';

defineOptions({ name: 'IoTProduct' });

const router = useRouter();
const categoryList = ref<any[]>([]);
const viewMode = ref<'card' | 'list'>('card');
const cardViewRef = ref();

// 搜索参数
const searchParams = ref({
  name: '',
  productKey: '',
});

// 图片预览
const { previewVisible, previewImage, handlePreviewImage } = useImagePreview();

const [FormModal, formModalApi] = useVbenModal({
  connectedComponent: ProductForm,
  destroyOnClose: true,
});

// 加载产品分类列表
async function loadCategories() {
  categoryList.value = await getSimpleProductCategoryList();
}

// 获取分类名称
function getCategoryNameByValue(categoryId: number) {
  const category = categoryList.value.find((c: any) => c.id === categoryId);
  return category?.name || '未分类';
}

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
  searchParams.value.name = '';
  searchParams.value.productKey = '';
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
  const data = await exportProduct(searchParams.value);
  await downloadFileFromBlobPart({ fileName: '产品列表.xls', source: data });
}

/** 打开产品详情 */
function openProductDetail(productId: number) {
  router.push({
    name: 'IoTProductDetail',
    params: { id: productId },
  });
}

/** 打开物模型管理 */
function openThingModel(productId: number) {
  router.push({
    name: 'IoTProductDetail',
    params: { id: productId },
    query: { tab: 'thingModel' },
  });
}

/** 新增产品 */
function handleCreate() {
  formModalApi.setData(null).open();
}

/** 编辑产品 */
function handleEdit(row: any) {
  formModalApi.setData(row).open();
}

/** 删除产品 */
async function handleDelete(row: any) {
  const hideLoading = message.loading({
    content: `正在删除 ${row.name}...`,
    duration: 0,
  });
  try {
    await deleteProduct(row.id);
    message.success(`删除 ${row.name} 成功`);
    handleRefresh();
  } finally {
    hideLoading();
  }
}

const [Grid, gridApi] = useVbenVxeGrid({
  formOptions: {
    schema: [],
  },
  gridOptions: {
    columns: useGridColumns(),
    height: 'auto',
    keepSource: true,
    proxyConfig: {
      ajax: {
        query: async ({ page }) => {
          return await getProductPage({
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

onMounted(() => {
  loadCategories();
});
</script>

<template>
  <Page auto-content-height>
    <FormModal @success="handleRefresh" />

    <!-- 统一搜索工具栏 -->
    <Card :body-style="{ padding: '16px' }" class="mb-4">
      <!-- 搜索表单 -->
      <div class="mb-3 flex items-center gap-3">
        <Input
          v-model:value="searchParams.name"
          placeholder="请输入产品名称"
          allow-clear
          style="width: 200px"
          @press-enter="handleSearch"
        >
          <template #prefix>
            <span class="text-gray-400">产品名称</span>
          </template>
        </Input>
        <Input
          v-model:value="searchParams.productKey"
          placeholder="请输入产品标识"
          allow-clear
          style="width: 200px"
          @press-enter="handleSearch"
        >
          <template #prefix>
            <span class="text-gray-400">ProductKey</span>
          </template>
        </Input>
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
          <Button type="primary" @click="handleCreate">
            <IconifyIcon icon="ant-design:plus-outlined" class="mr-1" />
            新增产品
          </Button>
          <Button type="primary" @click="handleExport">
            <IconifyIcon icon="ant-design:download-outlined" class="mr-1" />
            导出
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

      <!-- 产品分类列 -->
      <template #category="{ row }">
        <span>{{ getCategoryNameByValue(row.categoryId) }}</span>
      </template>

      <!-- 产品图标列 -->
      <template #icon="{ row }">
        <Button
          v-if="row.icon"
          type="link"
          size="small"
          @click="handlePreviewImage(row.icon)"
        >
          <IconifyIcon icon="ant-design:eye-outlined" class="text-lg" />
        </Button>
        <span v-else class="text-gray-400">-</span>
      </template>

      <!-- 产品图片列 -->
      <template #picUrl="{ row }">
        <Button
          v-if="row.picUrl"
          type="link"
          size="small"
          @click="handlePreviewImage(row.picUrl)"
        >
          <IconifyIcon icon="ant-design:eye-outlined" class="text-lg" />
        </Button>
        <span v-else class="text-gray-400">-</span>
      </template>

      <!-- 操作列 -->
      <template #actions="{ row }">
        <TableAction
          :actions="[
            {
              label: '详情',
              type: 'link',
              onClick: openProductDetail.bind(null, row.id),
            },
            {
              label: '物模型',
              type: 'link',
              onClick: openThingModel.bind(null, row.id),
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
                title: `确认删除产品 ${row.name} 吗?`,
                confirm: handleDelete.bind(null, row),
              },
            },
          ]"
        />
      </template>
    </Grid>

    <!-- 卡片视图 -->
    <ProductCardView
      v-show="viewMode === 'card'"
      ref="cardViewRef"
      :category-list="categoryList"
      :search-params="searchParams"
      @create="handleCreate"
      @edit="handleEdit"
      @delete="handleDelete"
      @detail="openProductDetail"
      @thing-model="openThingModel"
    />

    <!-- 图片预览 -->
    <div style="display: none">
      <Image.PreviewGroup
        :preview="{
          visible: previewVisible,
          onVisibleChange: (visible) => (previewVisible = visible),
        }"
      >
        <Image :src="previewImage" />
      </Image.PreviewGroup>
    </div>
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

<style>
/* 控制图片预览的大小 */
.ant-image-preview-img {
  max-width: 80% !important;
  max-height: 80% !important;
  object-fit: contain !important;
}

.ant-image-preview-operations {
  background: rgb(0 0 0 / 70%) !important;
}
</style>
