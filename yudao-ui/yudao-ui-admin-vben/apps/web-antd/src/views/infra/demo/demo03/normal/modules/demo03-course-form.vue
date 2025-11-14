<script lang="ts" setup>
import type { Demo03StudentApi } from '#/api/infra/demo/demo03/normal';

import { nextTick, watch } from 'vue';

import { IconifyIcon } from '@vben/icons';

import { Button, Input } from 'ant-design-vue';

import { ACTION_ICON, TableAction, useVbenVxeGrid } from '#/adapter/vxe-table';
import { getDemo03CourseListByStudentId } from '#/api/infra/demo/demo03/normal';
import { $t } from '#/locales';

import { useDemo03CourseGridEditColumns } from '../data';

const props = defineProps<{
  studentId?: number; // 学生编号（主表的关联字段）
}>();

const [Grid, gridApi] = useVbenVxeGrid({
  gridOptions: {
    columns: useDemo03CourseGridEditColumns(),
    border: true,
    showOverflow: true,
    autoResize: true,
    keepSource: true,
    rowConfig: {
      keyField: 'id',
    },
    pagerConfig: {
      enabled: false,
    },
    toolbarConfig: {
      enabled: false,
    },
  },
});

/** 添加学生课程 */
async function handleAdd() {
  await gridApi.grid.insertAt({} as Demo03StudentApi.Demo03Course, -1);
}

/** 删除学生课程 */
async function handleDelete(row: Demo03StudentApi.Demo03Course) {
  await gridApi.grid.remove(row);
}

/** 提供获取表格数据的方法供父组件调用 */
defineExpose({
  getData: (): Demo03StudentApi.Demo03Course[] => {
    const data = gridApi.grid.getData() as Demo03StudentApi.Demo03Course[];
    const removeRecords =
      gridApi.grid.getRemoveRecords() as Demo03StudentApi.Demo03Course[];
    const insertRecords =
      gridApi.grid.getInsertRecords() as Demo03StudentApi.Demo03Course[];
    return [
      ...data.filter(
        (row) => !removeRecords.some((removed) => removed.id === row.id),
      ),
      ...insertRecords.map((row: any) => ({ ...row, id: undefined })),
    ];
  },
});

/** 监听主表的关联字段的变化，加载对应的子表数据 */
watch(
  () => props.studentId,
  async (val) => {
    if (!val) {
      return;
    }
    await nextTick();
    await gridApi.grid.loadData(
      await getDemo03CourseListByStudentId(props.studentId!),
    );
  },
  { immediate: true },
);
</script>

<template>
  <Grid class="mx-4">
    <template #name="{ row }">
      <Input v-model:value="row.name" />
    </template>
    <template #score="{ row }">
      <Input v-model:value="row.score" />
    </template>
    <template #actions="{ row }">
      <TableAction
        :actions="[
          {
            label: $t('common.delete'),
            danger: true,
            type: 'link',
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
  <div class="-mt-4 flex justify-center">
    <Button
      type="primary"
      ghost
      @click="handleAdd"
      v-access:code="['infra:demo03-student:create']"
    >
      <IconifyIcon icon="lucide:plus" />
      {{ $t('ui.actionTitle.create', ['学生课程']) }}
    </Button>
  </div>
</template>
