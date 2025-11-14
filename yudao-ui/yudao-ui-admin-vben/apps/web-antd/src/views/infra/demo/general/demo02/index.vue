<script lang="ts" setup>
import type { Demo02CategoryApi } from '#/api/infra/demo/demo02';

import { onMounted, reactive, ref } from 'vue';

import { ContentWrap, Page, useVbenModal } from '@vben/common-ui';
import { IconifyIcon } from '@vben/icons';
import { useTableToolbar, VbenVxeTableToolbar } from '@vben/plugins/vxe-table';
import {
  cloneDeep,
  downloadFileFromBlobPart,
  formatDateTime,
  isEmpty,
} from '@vben/utils';

import { Button, Form, Input, message, RangePicker } from 'ant-design-vue';

import { VxeColumn, VxeTable } from '#/adapter/vxe-table';
import {
  deleteDemo02Category,
  exportDemo02Category,
  getDemo02CategoryList,
} from '#/api/infra/demo/demo02';
import { $t } from '#/locales';
import { getRangePickerDefaultProps } from '#/utils';

import Demo02CategoryForm from './modules/form.vue';

const loading = ref(true); // 列表的加载中
const list = ref<any[]>([]); // 树列表的数据

const queryParams = reactive({
  name: undefined,
  parentId: undefined,
  createTime: undefined,
});
const queryFormRef = ref(); // 搜索的表单
const exportLoading = ref(false); // 导出的加载中

/** 查询列表 */
async function getList() {
  loading.value = true;
  try {
    const params = cloneDeep(queryParams) as any;
    if (params.createTime && Array.isArray(params.createTime)) {
      params.createTime = (params.createTime as string[]).join(',');
    }
    list.value = await getDemo02CategoryList(params);
  } finally {
    loading.value = false;
  }
}

/** 搜索按钮操作 */
function handleQuery() {
  getList();
}

/** 重置按钮操作 */
function resetQuery() {
  queryFormRef.value.resetFields();
  handleQuery();
}

const [FormModal, formModalApi] = useVbenModal({
  connectedComponent: Demo02CategoryForm,
  destroyOnClose: true,
});

/** 创建示例分类 */
function handleCreate() {
  formModalApi.setData(null).open();
}

/** 编辑示例分类 */
function handleEdit(row: Demo02CategoryApi.Demo02Category) {
  formModalApi.setData(row).open();
}

/** 新增下级示例分类 */
function handleAppend(row: Demo02CategoryApi.Demo02Category) {
  formModalApi.setData({ parentId: row.id }).open();
}

/** 删除示例分类 */
async function handleDelete(row: Demo02CategoryApi.Demo02Category) {
  const hideLoading = message.loading({
    content: $t('ui.actionMessage.deleting', [row.id]),
    duration: 0,
  });
  try {
    await deleteDemo02Category(row.id!);
    message.success({
      content: $t('ui.actionMessage.deleteSuccess', [row.id]),
    });
    await getList();
  } finally {
    hideLoading();
  }
}

/** 导出表格 */
async function handleExport() {
  try {
    exportLoading.value = true;
    const data = await exportDemo02Category(queryParams);
    downloadFileFromBlobPart({ fileName: '示例分类.xls', source: data });
  } finally {
    exportLoading.value = false;
  }
}

/** 切换树形展开/收缩状态 */
const isExpanded = ref(true);
function toggleExpand() {
  isExpanded.value = !isExpanded.value;
  tableRef.value?.setAllTreeExpand(isExpanded.value);
}

/** 初始化 */
const { hiddenSearchBar, tableToolbarRef, tableRef } = useTableToolbar();
onMounted(() => {
  getList();
});
</script>

<template>
  <Page auto-content-height>
    <FormModal @success="getList" />

    <ContentWrap v-if="!hiddenSearchBar">
      <!-- 搜索工作栏 -->
      <Form :model="queryParams" ref="queryFormRef" layout="inline">
        <Form.Item label="名字" name="name">
          <Input
            v-model:value="queryParams.name"
            placeholder="请输入名字"
            allow-clear
            @press-enter="handleQuery"
            class="w-full"
          />
        </Form.Item>
        <Form.Item label="父级编号" name="parentId">
          <Input
            v-model:value="queryParams.parentId"
            placeholder="请输入父级编号"
            allow-clear
            @press-enter="handleQuery"
            class="w-full"
          />
        </Form.Item>
        <Form.Item label="创建时间" name="createTime">
          <RangePicker
            v-model:value="queryParams.createTime"
            v-bind="getRangePickerDefaultProps()"
            class="w-full"
          />
        </Form.Item>
        <Form.Item>
          <Button class="ml-2" @click="resetQuery"> 重置 </Button>
          <Button class="ml-2" @click="handleQuery" type="primary">
            搜索
          </Button>
        </Form.Item>
      </Form>
    </ContentWrap>

    <!-- 列表 -->
    <ContentWrap title="示例分类">
      <template #extra>
        <VbenVxeTableToolbar
          ref="tableToolbarRef"
          v-model:hidden-search="hiddenSearchBar"
        >
          <Button @click="toggleExpand" class="mr-2">
            {{ isExpanded ? '收缩' : '展开' }}
          </Button>
          <Button
            class="ml-2"
            type="primary"
            @click="handleCreate"
            v-access:code="['infra:demo02-category:create']"
          >
            <IconifyIcon icon="lucide:plus" />
            {{ $t('ui.actionTitle.create', ['示例分类']) }}
          </Button>
          <Button
            type="primary"
            class="ml-2"
            :loading="exportLoading"
            @click="handleExport"
            v-access:code="['infra:demo02-category:export']"
          >
            <IconifyIcon icon="lucide:download" />
            {{ $t('ui.actionTitle.export') }}
          </Button>
        </VbenVxeTableToolbar>
      </template>
      <VxeTable
        ref="tableRef"
        :data="list"
        :tree-config="{
          parentField: 'parentId',
          rowField: 'id',
          transform: true,
          expandAll: true,
          reserve: true,
        }"
        show-overflow
        :loading="loading"
      >
        <VxeColumn field="id" title="编号" align="center" />
        <VxeColumn field="name" title="名字" align="center" tree-node />
        <VxeColumn field="parentId" title="父级编号" align="center" />
        <VxeColumn field="createTime" title="创建时间" align="center">
          <template #default="{ row }">
            {{ formatDateTime(row.createTime) }}
          </template>
        </VxeColumn>
        <VxeColumn field="operation" title="操作" align="center">
          <template #default="{ row }">
            <Button
              size="small"
              type="link"
              @click="handleAppend(row)"
              v-access:code="['infra:demo02-category:create']"
            >
              新增下级
            </Button>
            <Button
              size="small"
              type="link"
              @click="handleEdit(row)"
              v-access:code="['infra:demo02-category:update']"
            >
              {{ $t('ui.actionTitle.edit') }}
            </Button>
            <Button
              size="small"
              type="link"
              danger
              class="ml-2"
              :disabled="!isEmpty(row.children)"
              @click="handleDelete(row)"
              v-access:code="['infra:demo02-category:delete']"
            >
              {{ $t('ui.actionTitle.delete') }}
            </Button>
          </template>
        </VxeColumn>
      </VxeTable>
    </ContentWrap>
  </Page>
</template>
