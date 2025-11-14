<script lang="ts" setup>
import type { VxeTableGridOptions } from '#/adapter/vxe-table';
import type { Demo03StudentApi } from '#/api/infra/demo/demo03/inner';

import { nextTick, watch } from 'vue';

import { useVbenVxeGrid } from '#/adapter/vxe-table';
import { getDemo03CourseListByStudentId } from '#/api/infra/demo/demo03/inner';

import { useDemo03CourseGridColumns } from '../data';

const props = defineProps<{
  studentId?: number; // 学生编号（主表的关联字段）
}>();

const [Grid, gridApi] = useVbenVxeGrid({
  gridOptions: {
    columns: useDemo03CourseGridColumns(),
    pagerConfig: {
      enabled: false,
    },
    toolbarConfig: {
      enabled: false,
    },
    height: '600px',
    rowConfig: {
      keyField: 'id',
      isHover: true,
    },
  } as VxeTableGridOptions<Demo03StudentApi.Demo03Course>,
});

/** 刷新表格 */
async function handleRefresh() {
  await gridApi.grid.loadData(
    await getDemo03CourseListByStudentId(props.studentId!),
  );
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
  <Grid table-title="学生课程列表" />
</template>
