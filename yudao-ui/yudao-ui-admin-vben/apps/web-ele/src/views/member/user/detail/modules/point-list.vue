<script lang="ts" setup>
import type { VxeTableGridOptions } from '#/adapter/vxe-table';
import type { MemberPointRecordApi } from '#/api/member/point/record';

import { useVbenVxeGrid } from '#/adapter/vxe-table';
import { getRecordPage } from '#/api/member/point/record';
import { DICT_TYPE } from '@vben/constants';
import { getDictOptions } from '@vben/hooks';

import { getRangePickerDefaultProps } from '#/utils';
import { useGridColumns } from '#/views/member/point/record/data';

const props = defineProps<{
  userId: number;
}>();

const [Grid] = useVbenVxeGrid({
  formOptions: {
    schema: [
      {
        fieldName: 'bizType',
        label: '业务类型',
        component: 'Select',
        componentProps: {
          clearable: true,
          placeholder: '请选择业务类型',
          options: getDictOptions(DICT_TYPE.MEMBER_POINT_BIZ_TYPE, 'number'),
        },
      },
      {
        fieldName: 'title',
        label: '积分标题',
        component: 'Input',
      },
      {
        fieldName: 'createDate',
        label: '获得时间',
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
    },
    toolbarConfig: {
      refresh: true,
      search: true,
    },
  } as VxeTableGridOptions<MemberPointRecordApi.Record>,
  separator: false,
});
</script>

<template>
  <Grid />
</template>
