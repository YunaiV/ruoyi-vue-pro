<script lang="ts" setup>
import type { Demo03StudentApi } from '#/api/infra/demo/demo03/normal';

import { onMounted, reactive, ref } from 'vue';

import { ContentWrap, Page, useVbenModal } from '@vben/common-ui';
import { DICT_TYPE } from '@vben/constants';
import { getDictOptions } from '@vben/hooks';
import { IconifyIcon } from '@vben/icons';
import { useTableToolbar, VbenVxeTableToolbar } from '@vben/plugins/vxe-table';
import {
  cloneDeep,
  downloadFileFromBlobPart,
  formatDateTime,
  isEmpty,
} from '@vben/utils';

import {
  Button,
  Form,
  Input,
  message,
  Pagination,
  RangePicker,
  Select,
} from 'ant-design-vue';

import { VxeColumn, VxeTable } from '#/adapter/vxe-table';
import {
  deleteDemo03Student,
  deleteDemo03StudentList,
  exportDemo03Student,
  getDemo03StudentPage,
} from '#/api/infra/demo/demo03/normal';
import { DictTag } from '#/components/dict-tag';
import { $t } from '#/locales';
import { getRangePickerDefaultProps } from '#/utils';

import Demo03StudentForm from './modules/form.vue';

const loading = ref(true); // 列表的加载中
const list = ref<Demo03StudentApi.Demo03Student[]>([]); // 列表的数据

const total = ref(0); // 列表的总页数
const queryParams = reactive({
  pageNo: 1,
  pageSize: 10,
  name: undefined,
  sex: undefined,
  description: undefined,
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
    const data = await getDemo03StudentPage(params);
    list.value = data.list;
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
  queryFormRef.value.resetFields();
  handleQuery();
}

const [FormModal, formModalApi] = useVbenModal({
  connectedComponent: Demo03StudentForm,
  destroyOnClose: true,
});

/** 创建学生 */
function handleCreate() {
  formModalApi.setData(null).open();
}

/** 编辑学生 */
function handle(row: Demo03StudentApi.Demo03Student) {
  formModalApi.setData(row).open();
}

/** 删除学生 */
async function handleDelete(row: Demo03StudentApi.Demo03Student) {
  const hideLoading = message.loading({
    content: $t('ui.actionMessage.deleting', [row.id]),
    duration: 0,
    key: 'action_process_msg',
  });
  try {
    await deleteDemo03Student(row.id!);
    message.success({
      content: $t('ui.actionMessage.deleteSuccess', [row.id]),
      key: 'action_process_msg',
    });
    await getList();
  } finally {
    hideLoading();
  }
}

/** 批量删除学生 */
async function handleDeleteBatch() {
  const hideLoading = message.loading({
    content: $t('ui.actionMessage.deleting'),
    duration: 0,
    key: 'action_process_msg',
  });
  try {
    await deleteDemo03StudentList(checkedIds.value);
    checkedIds.value = [];
    message.success($t('ui.actionMessage.deleteSuccess'));
    await getList();
  } finally {
    hideLoading();
  }
}

const checkedIds = ref<number[]>([]);
function handleRowCheckboxChange({
  records,
}: {
  records: Demo03StudentApi.Demo03Student[];
}) {
  checkedIds.value = records.map((item) => item.id!);
}

/** 导出表格 */
async function handleExport() {
  try {
    exportLoading.value = true;
    const data = await exportDemo03Student(queryParams);
    downloadFileFromBlobPart({ fileName: '学生.xls', source: data });
  } finally {
    exportLoading.value = false;
  }
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
        <Form.Item label="性别" name="sex">
          <Select
            v-model:value="queryParams.sex"
            placeholder="请选择性别"
            allow-clear
            class="w-full"
          >
            <Select.Option
              v-for="dict in getDictOptions(
                DICT_TYPE.SYSTEM_USER_SEX,
                'number',
              )"
              :key="dict.value"
              :value="dict.value"
            >
              {{ dict.label }}
            </Select.Option>
          </Select>
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
    <ContentWrap title="学生">
      <template #extra>
        <VbenVxeTableToolbar
          ref="tableToolbarRef"
          v-model:hidden-search="hiddenSearchBar"
        >
          <Button
            class="ml-2"
            type="primary"
            @click="handleCreate"
            v-access:code="['infra:demo03-student:create']"
          >
            <IconifyIcon icon="lucide:plus" />
            {{ $t('ui.actionTitle.create', ['学生']) }}
          </Button>
          <Button
            type="primary"
            class="ml-2"
            :loading="exportLoading"
            @click="handleExport"
            v-access:code="['infra:demo03-student:export']"
          >
            <IconifyIcon icon="lucide:download" />
            {{ $t('ui.actionTitle.export') }}
          </Button>
          <Button
            type="primary"
            danger
            class="ml-2"
            :disabled="isEmpty(checkedIds)"
            @click="handleDeleteBatch"
            v-access:code="['infra:demo03-student:delete']"
          >
            <IconifyIcon icon="lucide:trash-2" />
            批量删除
          </Button>
        </VbenVxeTableToolbar>
      </template>
      <VxeTable
        ref="tableRef"
        :data="list"
        show-overflow
        :loading="loading"
        @checkbox-all="handleRowCheckboxChange"
        @checkbox-change="handleRowCheckboxChange"
      >
        <VxeColumn type="checkbox" width="40" />
        <VxeColumn field="id" title="编号" align="center" />
        <VxeColumn field="name" title="名字" align="center" />
        <VxeColumn field="sex" title="性别" align="center">
          <template #default="{ row }">
            <DictTag :type="DICT_TYPE.SYSTEM_USER_SEX" :value="row.sex" />
          </template>
        </VxeColumn>
        <VxeColumn field="birthday" title="出生日期" align="center">
          <template #default="{ row }">
            {{ formatDateTime(row.birthday) }}
          </template>
        </VxeColumn>
        <VxeColumn field="description" title="简介" align="center" />
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
              @click="handle(row)"
              v-access:code="['infra:demo03-student:update']"
            >
              {{ $t('ui.actionTitle.edit') }}
            </Button>
            <Button
              size="small"
              type="link"
              danger
              class="ml-2"
              @click="handleDelete(row)"
              v-access:code="['infra:demo03-student:delete']"
            >
              {{ $t('ui.actionTitle.delete') }}
            </Button>
          </template>
        </VxeColumn>
      </VxeTable>
      <!-- 分页 -->
      <div class="mt-2 flex justify-end">
        <Pagination
          :total="total"
          v-model:current="queryParams.pageNo"
          v-model:page-size="queryParams.pageSize"
          show-size-changer
          @change="getList"
        />
      </div>
    </ContentWrap>
  </Page>
</template>
