<script setup lang="ts">
import type { VxeTableGridOptions } from '#/adapter/vxe-table';
import type { MallAfterSaleApi } from '#/api/mall/trade/afterSale/index';

import { ref, watch } from 'vue';

import { ElTabPane, ElTabs } from 'element-plus';

import { useVbenVxeGrid } from '#/adapter/vxe-table';
import { getAfterSalePage } from '#/api/mall/trade/afterSale';
import { DICT_TYPE } from '@vben/constants';
import { getDictOptions } from '@vben/hooks';

import { getRangePickerDefaultProps } from '#/utils';
import { useGridColumns } from '#/views/mall/trade/order/data';

const props = defineProps<{
  userId: number;
}>();

// 添加当前选中的售后状态
const activeStatus = ref<number | string>('all');

const [Grid, gridApi] = useVbenVxeGrid({
  formOptions: {
    schema: [
      {
        fieldName: 'spuName',
        label: '商品名称',
        component: 'Input',
      },
      {
        fieldName: 'no',
        label: '退款编号',
        component: 'Input',
      },
      {
        fieldName: 'orderNo',
        label: '订单编号',
        component: 'Input',
      },
      {
        fieldName: 'status',
        label: '售后状态',
        component: 'Select',
        componentProps: {
          options: getDictOptions(DICT_TYPE.TRADE_AFTER_SALE_STATUS, 'number'),
        },
      },
      {
        fieldName: 'type',
        label: '售后方式',
        component: 'Select',
        componentProps: {
          options: getDictOptions(DICT_TYPE.TRADE_AFTER_SALE_WAY, 'number'),
        },
      },
      {
        fieldName: 'type',
        label: '售后类型',
        component: 'Select',
        componentProps: {
          options: getDictOptions(DICT_TYPE.TRADE_AFTER_SALE_TYPE, 'number'),
        },
      },
      {
        fieldName: 'createTime',
        label: '创建时间',
        component: 'RangePicker',
        componentProps: {
          ...getRangePickerDefaultProps(),
          clearable: true,
        },
      },
    ],
    // 监听表单值变化
    handleValuesChange: (values, changedFields) => {
      // 如果状态字段发生变化
      if (changedFields.includes('status')) {
        // 同步更新标签页选中状态
        activeStatus.value = values.status ? String(values.status) : 'all';
      }
    },
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
          return await getAfterSalePage({
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
      slots: {
        buttons: 'customTop',
      },
    },
  } as VxeTableGridOptions<MallAfterSaleApi.AfterSale>,
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
  <Grid>
    <template #customTop>
      <ElTabs v-model="activeStatus">
        <ElTabPane label="全部" name="all" />
        <ElTabPane
          v-for="item in getDictOptions(
            DICT_TYPE.TRADE_AFTER_SALE_STATUS,
            'number',
          )"
          :key="String(item.value)"
          :label="item.label"
          :name="String(item.value)"
        />
      </ElTabs>
    </template>
  </Grid>
</template>
