<!-- 拼团活动选择弹窗组件 -->
<script lang="ts" setup>
import type { VbenFormSchema } from '#/adapter/form';
import type { VxeGridProps } from '#/adapter/vxe-table';
import type { MallCategoryApi } from '#/api/mall/product/category';
import type { MallCombinationActivityApi } from '#/api/mall/promotion/combination/combinationActivity';

import { computed, onMounted, ref } from 'vue';

import { useVbenModal } from '@vben/common-ui';
import { DICT_TYPE } from '@vben/constants';
import { getDictOptions } from '@vben/hooks';
import { fenToYuan, formatDate, handleTree } from '@vben/utils';

import { useVbenVxeGrid } from '#/adapter/vxe-table';
import { getCategoryList } from '#/api/mall/product/category';
import { getCombinationActivityPage } from '#/api/mall/promotion/combination/combinationActivity';

interface CombinationTableSelectProps {
  multiple?: boolean; // 是否多选：true - checkbox；false - radio
}

const props = withDefaults(defineProps<CombinationTableSelectProps>(), {
  multiple: false,
});

const emit = defineEmits<{
  change: [
    activity:
      | MallCombinationActivityApi.CombinationActivity
      | MallCombinationActivityApi.CombinationActivity[],
  ];
}>();

const categoryList = ref<MallCategoryApi.Category[]>([]); // 分类列表
const categoryTreeList = ref<any[]>([]); // 分类树

/** 单选：处理选中变化 */
function handleRadioChange() {
  const selectedRow =
    gridApi.grid.getRadioRecord() as MallCombinationActivityApi.CombinationActivity;
  if (selectedRow) {
    emit('change', selectedRow);
    modalApi.close();
  }
}

/**
 * 格式化拼团价格
 * @param products
 */
const formatCombinationPrice = (
  products: MallCombinationActivityApi.CombinationProduct[],
) => {
  if (!products || products.length === 0) return '-';
  const combinationPrice = Math.min(
    ...products.map((item) => item.combinationPrice || 0),
  );
  return `￥${fenToYuan(combinationPrice)}`;
};

/** 搜索表单 Schema */
const formSchema = computed<VbenFormSchema[]>(() => [
  {
    fieldName: 'name',
    label: '活动名称',
    component: 'Input',
    componentProps: {
      placeholder: '请输入活动名称',
      clearable: true,
    },
  },
  {
    fieldName: 'status',
    label: '活动状态',
    component: 'Select',
    componentProps: {
      placeholder: '请选择活动状态',
      clearable: true,
      options: getDictOptions(DICT_TYPE.COMMON_STATUS, 'number'),
    },
  },
]);

/** 表格列配置 */
const gridColumns = computed<VxeGridProps['columns']>(() => {
  const columns: VxeGridProps['columns'] = [];
  if (props.multiple) {
    columns.push({ type: 'checkbox', width: 55 });
  } else {
    columns.push({ type: 'radio', width: 55 });
  }
  columns.push(
    {
      field: 'id',
      title: '活动编号',
      minWidth: 80,
    },
    {
      field: 'name',
      title: '活动名称',
      minWidth: 140,
    },
    {
      field: 'activityTime',
      title: '活动时间',
      minWidth: 210,
      formatter: ({ row }) => {
        return `${formatDate(row.startTime, 'YYYY-MM-DD')} ~ ${formatDate(row.endTime, 'YYYY-MM-DD')}`;
      },
    },
    {
      field: 'picUrl',
      title: '商品图片',
      width: 100,
      cellRender: {
        name: 'CellImage',
      },
    },
    {
      field: 'spuName',
      title: '商品标题',
      minWidth: 300,
    },
    {
      field: 'marketPrice',
      title: '原价',
      minWidth: 100,
      formatter: ({ cellValue }) => {
        return cellValue ? `￥${fenToYuan(cellValue)}` : '-';
      },
    },
    {
      field: 'products',
      title: '拼团价',
      minWidth: 100,
      formatter: ({ cellValue }) => {
        return formatCombinationPrice(cellValue);
      },
    },
    {
      field: 'groupCount',
      title: '开团组数',
      minWidth: 100,
    },
    {
      field: 'groupSuccessCount',
      title: '成团组数',
      minWidth: 100,
    },
    {
      field: 'recordCount',
      title: '购买次数',
      minWidth: 100,
    },
    {
      field: 'status',
      title: '活动状态',
      minWidth: 100,
      cellRender: {
        name: 'CellDict',
        props: { type: DICT_TYPE.COMMON_STATUS },
      },
    },
    {
      field: 'createTime',
      title: '创建时间',
      width: 180,
      formatter: 'formatDateTime',
    },
  );
  return columns;
});

const [Grid, gridApi] = useVbenVxeGrid({
  formOptions: {
    schema: formSchema.value,
    layout: 'horizontal',
    collapsed: false,
  },
  gridOptions: {
    columns: gridColumns.value,
    height: 500,
    border: true,
    checkboxConfig: {
      reserve: true,
    },
    radioConfig: {
      reserve: true,
    },
    rowConfig: {
      keyField: 'id',
      isHover: true,
    },
    proxyConfig: {
      ajax: {
        async query({ page }: any, formValues: any) {
          return await getCombinationActivityPage({
            pageNo: page.currentPage,
            pageSize: page.pageSize,
            ...formValues,
          });
        },
      },
    },
  },
  gridEvents: {
    radioChange: handleRadioChange,
  },
});

const [Modal, modalApi] = useVbenModal({
  destroyOnClose: true,
  showConfirmButton: props.multiple, // 特殊：radio 单选情况下，走 handleRadioChange 处理。
  onConfirm: () => {
    const selectedRows =
      gridApi.grid.getCheckboxRecords() as MallCombinationActivityApi.CombinationActivity[];
    emit('change', selectedRows);
    modalApi.close();
  },
  async onOpenChange(isOpen: boolean) {
    if (!isOpen) {
      gridApi.grid.clearCheckboxRow();
      gridApi.grid.clearRadioRow();
      return;
    }

    // 1. 先查询数据
    await gridApi.query();
    // 2. 设置已选中行
    const data = modalApi.getData<
      | MallCombinationActivityApi.CombinationActivity
      | MallCombinationActivityApi.CombinationActivity[]
    >();
    if (props.multiple && Array.isArray(data) && data.length > 0) {
      setTimeout(() => {
        const tableData = gridApi.grid.getTableData().fullData;
        data.forEach((activity) => {
          const row = tableData.find(
            (item: MallCombinationActivityApi.CombinationActivity) =>
              item.id === activity.id,
          );
          if (row) {
            gridApi.grid.setCheckboxRow(row, true);
          }
        });
      }, 300);
    } else if (!props.multiple && data && !Array.isArray(data)) {
      setTimeout(() => {
        const tableData = gridApi.grid.getTableData().fullData;
        const row = tableData.find(
          (item: MallCombinationActivityApi.CombinationActivity) =>
            item.id === data.id,
        );
        if (row) {
          gridApi.grid.setRadioRow(row);
        }
      }, 300);
    }
  },
});

/** 对外暴露的方法 */
defineExpose({
  open: (
    data?:
      | MallCombinationActivityApi.CombinationActivity
      | MallCombinationActivityApi.CombinationActivity[],
  ) => {
    modalApi.setData(data).open();
  },
});

/** 初始化分类数据 */
onMounted(async () => {
  categoryList.value = await getCategoryList({});
  categoryTreeList.value = handleTree(categoryList.value, 'id', 'parentId');
});
</script>

<template>
  <Modal title="选择活动" class="w-[950px]">
    <Grid />
  </Modal>
</template>
