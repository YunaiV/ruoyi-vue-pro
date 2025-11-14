<script lang="ts" setup>
import type { Demo03StudentApi } from '#/api/infra/demo/demo03/erp';

import { h, onMounted, reactive, ref } from 'vue';

import { ContentWrap, Page, useVbenModal } from '@vben/common-ui';
import { DICT_TYPE } from '@vben/constants';
import { getDictOptions } from '@vben/hooks';
import { Download, Plus, Trash2 } from '@vben/icons';
import { useTableToolbar, VbenVxeTableToolbar } from '@vben/plugins/vxe-table';
import {
  cloneDeep,
  downloadFileFromBlobPart,
  formatDateTime,
  isEmpty,
} from '@vben/utils';

import {
  ElButton,
  ElDatePicker,
  ElForm,
  ElFormItem,
  ElInput,
  ElLoading,
  ElMessage,
  ElOption,
  ElPagination,
  ElSelect,
  ElTabPane,
  ElTabs,
} from 'element-plus';

import { VxeColumn, VxeTable } from '#/adapter/vxe-table';
import {
  deleteDemo03Student,
  deleteDemo03StudentList,
  exportDemo03Student,
  getDemo03StudentPage,
} from '#/api/infra/demo/demo03/erp';
import { DictTag } from '#/components/dict-tag';
import { $t } from '#/locales';

import Demo03CourseList from './modules/demo03-course-list.vue';
import Demo03GradeList from './modules/demo03-grade-list.vue';
import Demo03StudentForm from './modules/form.vue';

/** 子表的列表 */
const subTabsName = ref('demo03Course');
const selectDemo03Student = ref<Demo03StudentApi.Demo03Student>();
async function onCellClick({ row }: { row: Demo03StudentApi.Demo03Student }) {
  selectDemo03Student.value = row;
}

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
function handleEdit(row: Demo03StudentApi.Demo03Student) {
  formModalApi.setData(row).open();
}

/** 删除学生 */
async function handleDelete(row: Demo03StudentApi.Demo03Student) {
  const loadingInstance = ElLoading.service({
    text: $t('ui.actionMessage.deleting', [row.id]),
  });
  try {
    await deleteDemo03Student(row.id!);
    ElMessage.success($t('ui.actionMessage.deleteSuccess', [row.id]));
    await getList();
  } finally {
    loadingInstance.close();
  }
}

/** 批量删除学生 */
async function handleDeleteBatch() {
  const loadingInstance = ElLoading.service({
    text: $t('ui.actionMessage.deleting'),
  });
  try {
    await deleteDemo03StudentList(checkedIds.value);
    checkedIds.value = [];
    ElMessage.success($t('ui.actionMessage.deleteSuccess'));
    await getList();
  } finally {
    loadingInstance.close();
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
      <ElForm :model="queryParams" ref="queryFormRef" inline>
        <ElFormItem label="名字">
          <ElInput
            v-model="queryParams.name"
            placeholder="请输入名字"
            clearable
            @keyup.enter="handleQuery"
            class="!w-[240px]"
          />
        </ElFormItem>
        <ElFormItem label="性别">
          <ElSelect
            v-model="queryParams.sex"
            placeholder="请选择性别"
            clearable
            class="!w-[240px]"
          >
            <ElOption
              v-for="(dict, index) in getDictOptions(
                DICT_TYPE.SYSTEM_USER_SEX,
                'number',
              )"
              :key="index"
              :value="dict.value"
              :label="dict.label"
            />
          </ElSelect>
        </ElFormItem>
        <ElFormItem label="创建时间">
          <ElDatePicker
            v-model="queryParams.createTime"
            type="daterange"
            value-format="YYYY-MM-DD"
            range-separator="至"
            start-placeholder="开始日期"
            end-placeholder="结束日期"
            class="!w-[240px]"
          />
        </ElFormItem>
        <ElFormItem>
          <ElButton class="ml-2" @click="resetQuery"> 重置 </ElButton>
          <ElButton class="ml-2" @click="handleQuery" type="primary">
            搜索
          </ElButton>
        </ElFormItem>
      </ElForm>
    </ContentWrap>

    <!-- 列表 -->
    <ContentWrap title="学生">
      <template #extra>
        <VbenVxeTableToolbar
          ref="tableToolbarRef"
          v-model:hidden-search="hiddenSearchBar"
        >
          <ElButton
            class="ml-2"
            :icon="h(Plus)"
            type="primary"
            @click="handleCreate"
            v-access:code="['infra:demo03-student:create']"
          >
            {{ $t('ui.actionTitle.create', ['学生']) }}
          </ElButton>
          <ElButton
            :icon="h(Download)"
            type="primary"
            class="ml-2"
            :loading="exportLoading"
            @click="handleExport"
            v-access:code="['infra:demo03-student:export']"
          >
            {{ $t('ui.actionTitle.export') }}
          </ElButton>
          <ElButton
            :icon="h(Trash2)"
            type="danger"
            class="ml-2"
            :disabled="isEmpty(checkedIds)"
            @click="handleDeleteBatch"
            v-access:code="['infra:demo03-student:delete']"
          >
            批量删除
          </ElButton>
        </VbenVxeTableToolbar>
      </template>
      <VxeTable
        ref="tableRef"
        :data="list"
        @cell-click="onCellClick"
        :row-config="{
          keyField: 'id',
          isHover: true,
          isCurrent: true,
        }"
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
            <ElButton
              size="small"
              type="primary"
              link
              @click="handleEdit(row as any)"
              v-access:code="['infra:demo03-student:update']"
            >
              {{ $t('ui.actionTitle.edit') }}
            </ElButton>
            <ElButton
              size="small"
              type="danger"
              link
              class="ml-2"
              @click="handleDelete(row as any)"
              v-access:code="['infra:demo03-student:delete']"
            >
              {{ $t('ui.actionTitle.delete') }}
            </ElButton>
          </template>
        </VxeColumn>
      </VxeTable>
      <!-- 分页 -->
      <div class="mt-2 flex justify-end">
        <ElPagination
          :total="total"
          v-model:current-page="queryParams.pageNo"
          v-model:page-size="queryParams.pageSize"
          :page-sizes="[10, 20, 50, 100]"
          layout="total, sizes, prev, pager, next, jumper"
          @size-change="getList"
          @current-change="getList"
        />
      </div>
    </ContentWrap>
    <ContentWrap>
      <!-- 子表的表单 -->
      <ElTabs v-model="subTabsName">
        <ElTabPane name="demo03Course" label="学生课程">
          <Demo03CourseList :student-id="selectDemo03Student?.id" />
        </ElTabPane>
        <ElTabPane name="demo03Grade" label="学生班级">
          <Demo03GradeList :student-id="selectDemo03Student?.id" />
        </ElTabPane>
      </ElTabs>
    </ContentWrap>
  </Page>
</template>
