<script lang="ts" setup>
import type { VxeTableGridOptions } from '#/adapter/vxe-table';
import type { MemberSignInRecordApi } from '#/api/member/signin/record';

import { useVbenVxeGrid } from '#/adapter/vxe-table';
import { getSignInRecordPage } from '#/api/member/signin/record';
import {
  useGridColumns as useSignInGridColumns,
  useGridFormSchema as useSignInGridFormSchema,
} from '#/views/member/signin/record/data';

const props = defineProps<{
  userId: number;
}>();

/** 列表的搜索表单（过滤掉用户相关字段） */
function useGridFormSchema() {
  const excludeFields = new Set(['nickname']);
  return useSignInGridFormSchema().filter(
    (item) => !excludeFields.has(item.fieldName),
  );
}

/** 列表的字段（过滤掉用户相关字段） */
function useGridColumns() {
  const excludeFields = new Set(['nickname']);
  return useSignInGridColumns()?.filter(
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
    proxyConfig: {
      ajax: {
        query: async ({ page }, formValues) => {
          return await getSignInRecordPage({
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
  } as VxeTableGridOptions<MemberSignInRecordApi.SignInRecord>,
});
</script>

<template>
  <Grid />
</template>
