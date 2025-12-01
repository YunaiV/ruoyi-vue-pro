<script lang="ts" setup>
import type { CrmBusinessStatusApi } from '#/api/crm/business/status';

import { computed, nextTick, ref } from 'vue';

import { useVbenModal } from '@vben/common-ui';

import { ElInput, ElInputNumber, ElMessage } from 'element-plus';

import { useVbenForm } from '#/adapter/form';
import { TableAction, useVbenVxeGrid } from '#/adapter/vxe-table';
import {
  createBusinessStatus,
  DEFAULT_STATUSES,
  getBusinessStatus,
  updateBusinessStatus,
} from '#/api/crm/business/status';
import { $t } from '#/locales';

import { useFormColumns, useFormSchema } from '../data';

const emit = defineEmits(['success']);
const formData = ref<CrmBusinessStatusApi.BusinessStatus>();
const getTitle = computed(() => {
  return formData.value?.id
    ? $t('ui.actionTitle.edit', ['商机状态'])
    : $t('ui.actionTitle.create', ['商机状态']);
});

const [Form, formApi] = useVbenForm({
  commonConfig: {
    componentProps: {
      class: 'w-full',
    },
    formItemClass: 'col-span-2',
    labelWidth: 80,
  },
  layout: 'horizontal',
  schema: useFormSchema(),
  showDefaultActions: false,
});

const [Modal, modalApi] = useVbenModal({
  async onConfirm() {
    const { valid } = await formApi.validate();
    if (!valid) {
      return;
    }
    modalApi.lock();
    // 提交表单
    const data =
      (await formApi.getValues()) as CrmBusinessStatusApi.BusinessStatus;
    try {
      if (formData.value?.statuses && formData.value.statuses.length > 0) {
        data.statuses = formData.value.statuses;
        data.statuses.splice(-3, 3);
      }
      await (formData.value?.id
        ? updateBusinessStatus(data)
        : createBusinessStatus(data));
      // 关闭并提示
      await modalApi.close();
      emit('success');
      ElMessage.success($t('ui.actionMessage.operationSuccess'));
    } finally {
      modalApi.unlock();
    }
  },
  async onOpenChange(isOpen: boolean) {
    if (!isOpen) {
      return;
    }
    // 加载数据
    const data = modalApi.getData<CrmBusinessStatusApi.BusinessStatus>();
    modalApi.lock();
    try {
      if (!data || !data.id) {
        formData.value = {
          id: undefined,
          name: '',
          deptIds: [],
          statuses: [],
        };
        await handleAddStatus();
      } else {
        formData.value = await getBusinessStatus(data.id);
        if (
          !formData.value?.statuses?.length ||
          formData.value?.statuses?.length === 0
        ) {
          await handleAddStatus();
        }
      }
      // 设置到 values
      await formApi.setValues(formData.value as any);
      await gridApi.grid.reloadData(
        (formData.value!.statuses =
          formData.value?.statuses?.concat(DEFAULT_STATUSES)) as any,
      );
    } finally {
      modalApi.unlock();
    }
  },
});

/** 添加状态 */
async function handleAddStatus() {
  formData.value!.statuses!.splice(-3, 0, {
    name: '',
    percent: undefined,
  } as any);
  await nextTick();
  await gridApi.grid.reloadData(formData.value!.statuses as any);
}

/** 删除状态 */
async function deleteStatusArea(row: any, rowIndex: number) {
  await gridApi.grid.remove(row);
  formData.value!.statuses!.splice(rowIndex, 1);
  await gridApi.grid.reloadData(formData.value!.statuses as any);
}

/** 表格配置 */
const [Grid, gridApi] = useVbenVxeGrid({
  gridOptions: {
    editConfig: {
      trigger: 'click',
      mode: 'cell',
    },
    columns: useFormColumns(),
    data: formData.value?.statuses?.concat(DEFAULT_STATUSES),
    border: true,
    showOverflow: true,
    autoResize: true,
    keepSource: true,
    rowConfig: {
      keyField: 'row_id',
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
</script>

<template>
  <Modal :title="getTitle" class="w-1/2">
    <Form class="mx-4">
      <template #statuses>
        <Grid class="w-full">
          <template #defaultStatus="{ row, rowIndex }">
            <span>
              {{ row.defaultStatus ? '结束' : `阶段${rowIndex + 1}` }}
            </span>
          </template>
          <template #name="{ row }">
            <ElInput
              v-if="!row.endStatus"
              v-model="row.name"
              placeholder="请输入状态名"
            />
            <span v-else>{{ row.name }}</span>
          </template>
          <template #percent="{ row }">
            <ElInputNumber
              v-if="!row.endStatus"
              v-model="row.percent"
              :min="0"
              :max="100"
              :precision="2"
              placeholder="请输入赢单率"
              controls-position="right"
              class="!w-full"
            />
            <span v-else>{{ row.percent }}</span>
          </template>
          <template #actions="{ row, rowIndex }">
            <TableAction
              :actions="[
                {
                  label: $t('ui.actionTitle.create'),
                  type: 'primary',
                  link: true,
                  ifShow: () => !row.endStatus,
                  onClick: handleAddStatus,
                },
                {
                  label: $t('common.delete'),
                  type: 'danger',
                  link: true,
                  ifShow: () => !row.endStatus,
                  popConfirm: {
                    title: $t('ui.actionMessage.deleteConfirm', [row.name]),
                    confirm: deleteStatusArea.bind(null, row, rowIndex),
                  },
                },
              ]"
            />
          </template>
        </Grid>
      </template>
    </Form>
  </Modal>
</template>
