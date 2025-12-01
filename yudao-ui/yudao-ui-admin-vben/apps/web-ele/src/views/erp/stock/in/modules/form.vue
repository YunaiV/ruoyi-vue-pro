<script lang="ts" setup>
import type { ErpStockInApi } from '#/api/erp/stock/in';

import { computed, ref } from 'vue';

import { useVbenModal } from '@vben/common-ui';

import { ElMessage } from 'element-plus';

import { useVbenForm } from '#/adapter/form';
import { createStockIn, getStockIn, updateStockIn } from '#/api/erp/stock/in';
import { $t } from '#/locales';

import { useFormSchema } from '../data';
import ItemForm from './item-form.vue';

const emit = defineEmits(['success']);
const formData = ref<ErpStockInApi.StockIn>();
const formType = ref(''); // 表单类型：'create' | 'edit' | 'detail'
const itemFormRef = ref<InstanceType<typeof ItemForm>>();

const getTitle = computed(() => {
  if (formType.value === 'create') {
    return $t('ui.actionTitle.create', ['其它入库单']);
  } else if (formType.value === 'edit') {
    return $t('ui.actionTitle.edit', ['其它入库单']);
  } else {
    return '其它入库单详情';
  }
});

const [Form, formApi] = useVbenForm({
  commonConfig: {
    componentProps: {
      class: 'w-full',
    },
    labelWidth: 120,
  },
  wrapperClass: 'grid-cols-3',
  layout: 'vertical',
  schema: useFormSchema(formType.value),
  showDefaultActions: false,
});

/** 更新入库单项 */
function handleUpdateItems(items: ErpStockInApi.StockInItem[]) {
  formData.value = modalApi.getData<ErpStockInApi.StockIn>();
  formData.value.items = items;
  formApi.setValues({
    items,
  });
}

/** 创建或更新其它入库单 */
const [Modal, modalApi] = useVbenModal({
  async onConfirm() {
    const { valid } = await formApi.validate();
    if (!valid) {
      return;
    }
    const itemFormInstance = Array.isArray(itemFormRef.value)
      ? itemFormRef.value[0]
      : itemFormRef.value;
    try {
      itemFormInstance.validate();
    } catch (error: any) {
      ElMessage.error(error.message || '子表单验证失败');
      return;
    }

    modalApi.lock();
    // 提交表单
    const data = (await formApi.getValues()) as ErpStockInApi.StockIn;
    try {
      await (formType.value === 'create'
        ? createStockIn(data)
        : updateStockIn(data));
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
      formData.value = undefined;
      return;
    }
    // 加载数据
    const data = modalApi.getData<{ id?: number; type: string }>();
    formType.value = data.type;
    formApi.setDisabled(formType.value === 'detail');
    formApi.updateSchema(useFormSchema(formType.value));
    if (!data || !data.id) {
      return;
    }
    modalApi.lock();
    try {
      formData.value = await getStockIn(data.id);
      // 设置到 values
      await formApi.setValues(formData.value);
    } finally {
      modalApi.unlock();
    }
  },
});
</script>

<template>
  <Modal
    :title="getTitle"
    class="w-3/4"
    :show-confirm-button="formType !== 'detail'"
  >
    <Form class="mx-3">
      <template #items>
        <ItemForm
          ref="itemFormRef"
          :items="formData?.items ?? []"
          :disabled="formType === 'detail'"
          @update:items="handleUpdateItems"
        />
      </template>
    </Form>
  </Modal>
</template>
