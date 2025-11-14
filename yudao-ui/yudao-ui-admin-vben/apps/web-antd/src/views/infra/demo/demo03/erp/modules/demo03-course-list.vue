<script lang="ts" setup>
import type { VxeTableGridOptions } from '#/adapter/vxe-table';
import type { Demo03StudentApi } from '#/api/infra/demo/demo03/erp';

import { nextTick, ref, watch } from 'vue';

import { confirm, useVbenModal } from '@vben/common-ui';
import { isEmpty } from '@vben/utils';

import { message } from 'ant-design-vue';

import { ACTION_ICON, TableAction, useVbenVxeGrid } from '#/adapter/vxe-table';
import {
  deleteDemo03Course,
  deleteDemo03CourseList,
  getDemo03CoursePage,
} from '#/api/infra/demo/demo03/erp';
import { $t } from '#/locales';

import {
  useDemo03CourseGridColumns,
  useDemo03CourseGridFormSchema,
} from '../data';
import Demo03CourseForm from './demo03-course-form.vue';

const props = defineProps<{
  studentId?: number; // 学生编号（主表的关联字段）
}>();

const [FormModal, formModalApi] = useVbenModal({
  connectedComponent: Demo03CourseForm,
  destroyOnClose: true,
});

/** 创建学生课程 */
function handleCreate() {
  if (!props.studentId) {
    message.warning('请先选择一个学生!');
    return;
  }
  formModalApi.setData({ studentId: props.studentId }).open();
}

/** 编辑学生课程 */
function handleEdit(row: Demo03StudentApi.Demo03Course) {
  formModalApi.setData(row).open();
}

/** 删除学生课程 */
async function handleDelete(row: Demo03StudentApi.Demo03Course) {
  const hideLoading = message.loading({
    content: $t('ui.actionMessage.deleting', [row.id]),
    duration: 0,
  });
  try {
    await deleteDemo03Course(row.id!);
    message.success($t('ui.actionMessage.deleteSuccess', [row.id]));
    await handleRefresh();
  } finally {
    hideLoading();
  }
}

/** 批量删除学生课程 */
async function handleDeleteBatch() {
  await confirm($t('ui.actionMessage.deleteBatchConfirm'));
  const hideLoading = message.loading({
    content: $t('ui.actionMessage.deletingBatch'),
    duration: 0,
  });
  try {
    await deleteDemo03CourseList(checkedIds.value);
    checkedIds.value = [];
    message.success($t('ui.actionMessage.deleteSuccess'));
    await handleRefresh();
  } finally {
    hideLoading();
  }
}

const checkedIds = ref<number[]>([]);
function handleRowCheckboxChange({
  records,
}: {
  records: Demo03StudentApi.Demo03Course[];
}) {
  checkedIds.value = records.map((item) => item.id!);
}

const [Grid, gridApi] = useVbenVxeGrid({
  formOptions: {
    schema: useDemo03CourseGridFormSchema(),
  },
  gridOptions: {
    columns: useDemo03CourseGridColumns(),
    proxyConfig: {
      ajax: {
        query: async ({ page }, formValues) => {
          if (!props.studentId) {
            return [];
          }
          return await getDemo03CoursePage({
            pageNo: page.currentPage,
            pageSize: page.pageSize,
            studentId: props.studentId,
            ...formValues,
          });
        },
      },
    },
    pagerConfig: {
      enabled: true,
    },
    toolbarConfig: {
      refresh: true,
      search: true,
    },
    height: '600px',
    rowConfig: {
      keyField: 'id',
      isHover: true,
    },
  } as VxeTableGridOptions<Demo03StudentApi.Demo03Course>,
  gridEvents: {
    checkboxAll: handleRowCheckboxChange,
    checkboxChange: handleRowCheckboxChange,
  },
});

/** 刷新表格 */
async function handleRefresh() {
  await gridApi.query();
}

/** 监听主表的关联字段的变化，加载对应的子表数据 */
watch(
  () => props.studentId,
  async (val) => {
    if (!val) {
      return;
    }
    await nextTick();
    await handleRefresh();
  },
  { immediate: true },
);
</script>

<template>
  <FormModal @success="handleRefresh" />
  <Grid table-title="学生课程列表">
    <template #toolbar-tools>
      <TableAction
        :actions="[
          {
            label: $t('ui.actionTitle.create', ['学生课程']),
            type: 'primary',
            icon: ACTION_ICON.ADD,
            auth: ['infra:demo03-student:create'],
            onClick: handleCreate,
          },
          {
            label: $t('ui.actionTitle.deleteBatch'),
            type: 'primary',
            danger: true,
            icon: ACTION_ICON.DELETE,
            auth: ['infra:demo03-student:delete'],
            disabled: isEmpty(checkedIds),
            onClick: handleDeleteBatch,
          },
        ]"
      />
    </template>
    <template #actions="{ row }">
      <TableAction
        :actions="[
          {
            label: $t('common.edit'),
            type: 'link',
            icon: ACTION_ICON.EDIT,
            auth: ['infra:demo03-student:update'],
            onClick: handleEdit.bind(null, row),
          },
          {
            label: $t('common.delete'),
            type: 'link',
            danger: true,
            icon: ACTION_ICON.DELETE,
            auth: ['infra:demo03-student:delete'],
            popConfirm: {
              title: $t('ui.actionMessage.deleteConfirm', [row.id]),
              confirm: handleDelete.bind(null, row),
            },
          },
        ]"
      />
    </template>
  </Grid>
</template>
