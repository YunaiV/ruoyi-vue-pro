<script setup lang="ts">
import type { VxeTableGridOptions } from '#/adapter/vxe-table';
import type { MallBrokerageUserApi } from '#/api/mall/trade/brokerage/user';

import { ref, watch } from 'vue';

import { useVbenVxeGrid } from '#/adapter/vxe-table';
import * as BrokerageUserApi from '#/api/mall/trade/brokerage/user';
import { getRangePickerDefaultProps } from '#/utils';

const props = defineProps<{
  userId: number;
}>();

// 添加当前选中的状态
const activeStatus = ref<number | string>('all');

const [Grid, gridApi] = useVbenVxeGrid({
  formOptions: {
    schema: [
      {
        fieldName: 'level',
        label: '用户类型',
        component: 'RadioGroup',
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
          isButton: true,
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
    ],
  },
  gridOptions: {
    columns: [
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
    ],
    keepSource: true,
    pagerConfig: {
      pageSize: 10,
    },
    expandConfig: {
      trigger: 'row',
      expandAll: true,
      padding: true,
    },
    proxyConfig: {
      ajax: {
        query: async ({ page }, formValues) => {
          return await BrokerageUserApi.getBrokerageUserPage({
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
  } as VxeTableGridOptions<MallBrokerageUserApi.BrokerageUser>,
  separator: false,
});

// 监听标签页变化，更新表单状态值并触发查询
watch(activeStatus, (val) => {
  // 使用formApi获取表单对象
  if (gridApi.formApi) {
    // 设置状态值
    gridApi.formApi.setFieldValue(
      'status',
      val === 'all' ? undefined : Number(val),
    );

    // 触发查询
    gridApi.query({ status: val === 'all' ? undefined : Number(val) });
  }
});
</script>

<template>
  <Grid />
</template>
