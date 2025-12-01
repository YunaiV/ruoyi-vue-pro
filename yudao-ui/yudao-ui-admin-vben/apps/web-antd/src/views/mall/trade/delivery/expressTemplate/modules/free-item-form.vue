<script lang="ts" setup>
import type { MallDeliveryExpressTemplateApi } from '#/api/mall/trade/delivery/expressTemplate';
import type { SystemAreaApi } from '#/api/system/area';

import { computed, nextTick, ref, watch } from 'vue';

import { InputNumber, TreeSelect } from 'ant-design-vue';

import { TableAction, useVbenVxeGrid } from '#/adapter/vxe-table';

import { FREE_MODE_TITLE_MAP, useFreesColumns } from '../data';

interface Props {
  items?: MallDeliveryExpressTemplateApi.DeliveryExpressTemplateFree[];
  chargeMode?: number;
  areaTree?: SystemAreaApi.Area[];
}

const props = withDefaults(defineProps<Props>(), {
  items: () => [],
  chargeMode: 1,
  areaTree: () => [],
});

const emit = defineEmits(['update:items']);

const tableData = ref<any[]>([]);
const columnTitle = computed(() => FREE_MODE_TITLE_MAP[props.chargeMode]);

/** 表格配置 */
const [Grid, gridApi] = useVbenVxeGrid({
  gridOptions: {
    columns: useFreesColumns(props.chargeMode),
    data: tableData.value,
    minHeight: 200,
    autoResize: true,
    border: true,
    rowConfig: {
      keyField: 'seq',
      isHover: true,
    },
    pagerConfig: {
      enabled: false,
    },
    toolbarConfig: {
      enabled: false,
    },
  },
});

/** 监听外部传入的数据 */
watch(
  () => props.items,
  async (items) => {
    if (!items) {
      return;
    }
    tableData.value = [...items];
    await nextTick();
    await gridApi.grid.reloadData(tableData.value);
  },
  {
    immediate: true,
  },
);

/** 监听计费方式变化 */
watch(
  () => props.chargeMode,
  () => {
    const columns = useFreesColumns(props.chargeMode);
    if (gridApi.grid && columns) {
      gridApi.grid.reloadColumn(columns);
    }
  },
);

/** 处理新增 */
function handleAdd() {
  const newRow = {
    areaIds: [],
    freeCount: undefined,
    freePrice: undefined,
  };
  tableData.value.push(newRow);
  emit('update:items', [...tableData.value]);
}

/** 处理删除 */
function handleDelete(row: any) {
  const index = tableData.value.findIndex((item) => item.seq === row.seq);
  if (index !== -1) {
    tableData.value.splice(index, 1);
  }
  emit('update:items', [...tableData.value]);
}

/** 处理行数据变更 */
function handleRowChange(row: any) {
  const index = tableData.value.findIndex((item) => item.seq === row.seq);
  if (index === -1) {
    tableData.value.push(row);
  } else {
    tableData.value[index] = row;
  }
  emit('update:items', [...tableData.value]);
}

/** 表单校验 */
function validate() {
  for (let i = 0; i < tableData.value.length; i++) {
    const item = tableData.value[i];
    if (!item.areaIds || item.areaIds.length === 0) {
      throw new Error(`包邮设置第 ${i + 1} 行：区域不能为空`);
    }
    if (!item.freeCount || item.freeCount <= 0) {
      throw new Error(
        `包邮设置第 ${i + 1} 行：${columnTitle.value?.freeCountTitle}必须大于 0`,
      );
    }
    if (!item.freePrice || item.freePrice <= 0) {
      throw new Error(`包邮设置第 ${i + 1} 行：包邮金额必须大于 0`);
    }
  }
}

defineExpose({
  validate,
});
</script>

<template>
  <Grid class="w-full">
    <template #areaIds="{ row }">
      <!-- TODO 芋艿：可优化，使用 Cascade。不过貌似 antd 在 multiple 貌似有 bug！ -->
      <TreeSelect
        v-model:value="row.areaIds"
        :tree-data="areaTree"
        :field-names="{
          label: 'name',
          value: 'id',
          children: 'children',
        }"
        placeholder="请选择地区"
        class="w-full"
        multiple
        tree-checkable
        :max-tag-count="1"
        @change="handleRowChange(row)"
      />
    </template>
    <template #freeCount="{ row }">
      <InputNumber
        v-model:value="row.freeCount"
        :min="1"
        @change="handleRowChange(row)"
      />
    </template>
    <template #freePrice="{ row }">
      <InputNumber
        v-model:value="row.freePrice"
        :min="0"
        :precision="2"
        @change="handleRowChange(row)"
      />
    </template>
    <template #actions="{ row }">
      <TableAction
        :actions="[
          {
            label: '删除',
            type: 'link',
            danger: true,
            popConfirm: {
              title: '确认删除该区域吗？',
              confirm: handleDelete.bind(null, row),
            },
          },
        ]"
      />
    </template>
    <template #bottom>
      <TableAction
        class="mt-2 flex justify-center"
        :actions="[
          {
            label: '添加包邮区域',
            type: 'default',
            onClick: handleAdd,
          },
        ]"
      />
    </template>
  </Grid>
</template>
