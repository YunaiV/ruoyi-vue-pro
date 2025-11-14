<script lang="ts" setup>
import type { Demo03StudentApi } from '#/api/infra/demo/demo03/erp';

import { h, nextTick, onMounted, reactive, ref, watch } from 'vue';

import { ContentWrap, useVbenModal } from '@vben/common-ui';
import { Plus, Trash2 } from '@vben/icons';
import { useTableToolbar, VbenVxeTableToolbar } from '@vben/plugins/vxe-table';
import { cloneDeep, formatDateTime, isEmpty } from '@vben/utils';

import {
  ElButton,
  ElDatePicker,
  ElForm,
  ElFormItem,
  ElInput,
  ElLoading,
  ElMessage,
  ElPagination,
} from 'element-plus';

import { VxeColumn, VxeTable } from '#/adapter/vxe-table';
import {
  deleteDemo03Grade,
  deleteDemo03GradeList,
  getDemo03GradePage,
} from '#/api/infra/demo/demo03/erp';
import { $t } from '#/locales';

import Demo03GradeForm from './demo03-grade-form.vue';

const props = defineProps<{
  studentId?: number; // 学生编号（主表的关联字段）
}>();

const [FormModal, formModalApi] = useVbenModal({
  connectedComponent: Demo03GradeForm,
  destroyOnClose: true,
});

/** 创建学生班级 */
function handleCreate() {
  if (!props.studentId) {
    ElMessage.warning('请先选择一个学生!');
    return;
  }
  formModalApi.setData({ studentId: props.studentId }).open();
}

/** 编辑学生班级 */
function handleEdit(row: Demo03StudentApi.Demo03Grade) {
  formModalApi.setData(row).open();
}

/** 删除学生班级 */
async function handleDelete(row: Demo03StudentApi.Demo03Grade) {
  const loadingInstance = ElLoading.service({
    text: $t('ui.actionMessage.deleting', [row.id]),
  });
  try {
    await deleteDemo03Grade(row.id!);
    ElMessage.success($t('ui.actionMessage.deleteSuccess', [row.id]));
    await getList();
  } finally {
    loadingInstance.close();
  }
}

/** 批量删除学生班级 */
async function handleDeleteBatch() {
  const loadingInstance = ElLoading.service({
    text: $t('ui.actionMessage.deleting'),
  });
  try {
    await deleteDemo03GradeList(checkedIds.value);
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
  records: Demo03StudentApi.Demo03Grade[];
}) {
  checkedIds.value = records.map((item) => item.id!);
}

const loading = ref(true); // 列表的加载中
const list = ref<Demo03StudentApi.Demo03Grade[]>([]); // 列表的数据
const total = ref(0); // 列表的总页数
const queryFormRef = ref(); // 搜索的表单
const queryParams = reactive({
  pageNo: 1,
  pageSize: 10,
  studentId: undefined,
  name: undefined,
  teacher: undefined,
  createTime: undefined,
});

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
/** 查询列表 */
async function getList() {
  loading.value = true;
  try {
    if (!props.studentId) {
      return [];
    }
    const params = cloneDeep(queryParams) as any;
    if (params.createTime && Array.isArray(params.createTime)) {
      params.createTime = (params.createTime as string[]).join(',');
    }
    params.studentId = props.studentId;
    const data = await getDemo03GradePage(params);
    list.value = data.list;
    total.value = data.total;
  } finally {
    loading.value = false;
  }
}

/** 监听主表的关联字段的变化，加载对应的子表数据 */
watch(
  () => props.studentId,
  async (val) => {
    if (!val) {
      return;
    }
    await nextTick();
    await getList();
  },
  { immediate: true },
);

/** 初始化 */
const { hiddenSearchBar, tableToolbarRef, tableRef } = useTableToolbar();
onMounted(() => {
  getList();
});
</script>

<template>
  <FormModal @success="getList" />
  <div class="h-[600px]">
    <ContentWrap v-if="!hiddenSearchBar">
      <!-- 搜索工作栏 -->
      <ElForm :model="queryParams" ref="queryFormRef" inline>
        <ElFormItem label="学生编号">
          <ElInput
            v-model="queryParams.studentId"
            placeholder="请输入学生编号"
            clearable
            @keyup.enter="handleQuery"
            class="!w-[240px]"
          />
        </ElFormItem>
        <ElFormItem label="名字">
          <ElInput
            v-model="queryParams.name"
            placeholder="请输入名字"
            clearable
            @keyup.enter="handleQuery"
            class="!w-[240px]"
          />
        </ElFormItem>
        <ElFormItem label="班主任">
          <ElInput
            v-model="queryParams.teacher"
            placeholder="请输入班主任"
            clearable
            @keyup.enter="handleQuery"
            class="!w-[240px]"
          />
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
        show-overflow
        :loading="loading"
        @checkbox-all="handleRowCheckboxChange"
        @checkbox-change="handleRowCheckboxChange"
      >
        <VxeColumn type="checkbox" width="40" />
        <VxeColumn field="id" title="编号" align="center" />
        <VxeColumn field="studentId" title="学生编号" align="center" />
        <VxeColumn field="name" title="名字" align="center" />
        <VxeColumn field="teacher" title="班主任" align="center" />
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
  </div>
</template>
