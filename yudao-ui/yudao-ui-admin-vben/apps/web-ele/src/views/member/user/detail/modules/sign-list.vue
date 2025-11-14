<script lang="ts" setup>
import type { VxeTableGridOptions } from '#/adapter/vxe-table';
import type { MemberSignInRecordApi } from '#/api/member/signin/record';

import { useVbenVxeGrid } from '#/adapter/vxe-table';
import { getSignInRecordPage } from '#/api/member/signin/record';
import { getRangePickerDefaultProps } from '#/utils';
import { useGridColumns } from '#/views/member/signin/record/data';

const props = defineProps<{
  userId: number;
}>();

const [Grid] = useVbenVxeGrid({
  formOptions: {
    schema: [
      {
        fieldName: 'day',
        label: '签到天数',
        component: 'Input',
        componentProps: {
          placeholder: '请输入签到天数',
          clearable: true,
        },
      },
      {
        fieldName: 'createTime',
        label: '签到时间',
        component: 'RangePicker',
        componentProps: {
          ...getRangePickerDefaultProps(),
          clearable: true,
        },
      },
    ],
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
  separator: false,
});
</script>

<template>
  <Grid />
</template>
