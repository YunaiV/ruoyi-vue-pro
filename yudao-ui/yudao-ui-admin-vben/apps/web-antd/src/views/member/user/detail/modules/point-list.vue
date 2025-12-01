<script lang="ts" setup>
import type { VxeTableGridOptions } from '#/adapter/vxe-table';
import type { MemberPointRecordApi } from '#/api/member/point/record';

import { useVbenVxeGrid } from '#/adapter/vxe-table';
import { getRecordPage } from '#/api/member/point/record';
import {
  useGridColumns as usePointGridColumns,
  useGridFormSchema as usePointGridFormSchema,
} from '#/views/member/point/record/data';

const props = defineProps<{
  userId: number;
}>();

/** 列表的搜索表单（过滤掉用户相关字段） */
function useGridFormSchema() {
  const excludeFields = new Set(['nickname']);
  return usePointGridFormSchema().filter(
    (item) => !excludeFields.has(item.fieldName),
  );
}

/** 列表的字段（过滤掉用户相关字段） */
function useGridColumns() {
  const excludeFields = new Set(['nickname']);
  return usePointGridColumns()?.filter(
    (item) => item.field && !excludeFields.has(item.field),
  );
}

const [Grid] = useVbenVxeGrid({
  formOptions: {
    schema: useGridFormSchema(),
  },
  gridOptions: {
    columns: useGridColumns(),
    keepSource: true,
    pagerConfig: {
      pageSize: 10,
    },
    proxyConfig: {
      ajax: {
        query: async ({ page }, formValues) => {
          return await getRecordPage({
            pageNo: page.currentPage,
            pageSize: page.pageSize,
            userId: props.userId,
            ...formValues,
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
  } as VxeTableGridOptions<MemberPointRecordApi.Record>,
});
</script>

<template>
  <Grid />
</template>
