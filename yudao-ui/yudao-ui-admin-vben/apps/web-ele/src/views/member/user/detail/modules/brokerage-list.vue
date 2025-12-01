<script setup lang="ts">
import type {
  VxeGridPropTypes,
  VxeTableGridOptions,
} from '#/adapter/vxe-table';
import type { MallBrokerageUserApi } from '#/api/mall/trade/brokerage/user';

import { useVbenVxeGrid } from '#/adapter/vxe-table';
import { getBrokerageUserPage } from '#/api/mall/trade/brokerage/user';
import { getRangePickerDefaultProps } from '#/utils';

defineOptions({ name: 'BrokerageList' });

const props = defineProps<{
  userId: number;
}>();

const formSchema = (): any[] => {
  return [
    {
      fieldName: 'level',
      label: '用户类型',
      component: 'Select',
      componentProps: {
        options: [
          {
            label: '全部',
            value: 0,
          },
          {
            label: '一级',
            value: 1,
          },
          {
            label: '二级',
            value: 2,
          },
        ],
        placeholder: '请选择用户类型',
        allowClear: true,
      },
    },
    {
      fieldName: 'bindUserTime',
      label: '绑定时间',
      component: 'RangePicker',
      componentProps: {
        ...getRangePickerDefaultProps(),
        clearable: true,
      },
    },
  ];
};

const columns = (): VxeGridPropTypes.Columns => {
  return [
    {
      field: 'id',
      title: '用户编号',
    },
    {
      field: 'avatar',
      title: '头像',
      cellRender: {
        name: 'CellImage',
        props: {
          height: 40,
          width: 40,
          shape: 'circle',
        },
      },
    },
    {
      field: 'nickname',
      title: '昵称',
    },
    {
      field: 'level',
      title: '等级',
      formatter: (row: any) => {
        return row.level === 1 ? '一级' : '二级';
      },
    },
    {
      field: 'bindUserTime',
      title: '绑定时间',
      formatter: 'formatDateTime',
    },
  ];
};

const [Grid] = useVbenVxeGrid({
  formOptions: {
    schema: formSchema(),
  },
  gridOptions: {
    columns: columns(),
    keepSource: true,
    proxyConfig: {
      ajax: {
        query: async ({ page }, formValues) => {
          return await getBrokerageUserPage({
            pageNo: page.currentPage,
            pageSize: page.pageSize,
            bindUserId: props.userId,
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
  } as VxeTableGridOptions<MallBrokerageUserApi.BrokerageUser>,
});
</script>

<template>
  <Grid />
</template>
