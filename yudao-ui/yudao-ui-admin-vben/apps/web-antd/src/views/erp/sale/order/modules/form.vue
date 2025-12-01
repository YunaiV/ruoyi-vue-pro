<script lang="ts" setup>
import type { ErpSaleOrderApi } from '#/api/erp/sale/order';

import { computed, ref } from 'vue';

import { useVbenModal } from '@vben/common-ui';

import { message } from 'ant-design-vue';

import { useVbenForm } from '#/adapter/form';
import { getAccountSimpleList } from '#/api/erp/finance/account';
import {
  createSaleOrder,
  getSaleOrder,
  updateSaleOrder,
} from '#/api/erp/sale/order';
import { $t } from '#/locales';

import { useFormSchema } from '../data';
import ItemForm from './item-form.vue';

const emit = defineEmits(['success']);
const formData = ref<ErpSaleOrderApi.SaleOrder>();
const formType = ref(''); // 表单类型：'create' | 'edit' | 'detail'
const itemFormRef = ref<InstanceType<typeof ItemForm>>();

const getTitle = computed(() => {
  if (formType.value === 'create') {
    return $t('ui.actionTitle.create', ['销售订单']);
  } else if (formType.value === 'edit') {
    return $t('ui.actionTitle.edit', ['销售订单']);
  } else {
    return '销售订单详情';
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
  handleValuesChange: (values, changedFields) => {
    // 目的：同步到 item-form 组件，触发整体的价格计算
    if (formData.value && changedFields.includes('discountPercent')) {
      formData.value.discountPercent = values.discountPercent;
    }
  },
});

/** 更新销售订单项 */
function handleUpdateItems(items: ErpSaleOrderApi.SaleOrderItem[]) {
  formData.value = modalApi.getData<ErpSaleOrderApi.SaleOrder>();
  formData.value.items = items;
  formApi.setValues({
    items,
  });
}

/** 更新优惠金额 */
function handleUpdateDiscountPrice(discountPrice: number) {
  formApi.setValues({
    discountPrice,
  });
}

/** 更新总金额 */
function handleUpdateTotalPrice(totalPrice: number) {
  formApi.setValues({
    totalPrice,
  });
}

/** 创建或更新销售订单 */
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
      message.error(error.message || '子表单验证失败');
      return;
    }

    modalApi.lock();
    // 提交表单
    const data = (await formApi.getValues()) as ErpSaleOrderApi.SaleOrder;
    try {
      await (formType.value === 'create'
        ? createSaleOrder(data)
        : updateSaleOrder(data));
      // 关闭并提示
      await modalApi.close();
      emit('success');
      message.success($t('ui.actionMessage.operationSuccess'));
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
      // 新增时，默认选中账户
      const accountList = await getAccountSimpleList();
      const defaultAccount = accountList.find((item) => item.defaultStatus);
      if (defaultAccount) {
        await formApi.setValues({ accountId: defaultAccount.id });
      }
      return;
    }
    modalApi.lock();
    try {
      formData.value = await getSaleOrder(data.id);
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
          :discount-percent="formData?.discountPercent ?? 0"
          @update:items="handleUpdateItems"
          @update:discount-price="handleUpdateDiscountPrice"
          @update:total-price="handleUpdateTotalPrice"
        />
      </template>
    </Form>
  </Modal>
</template>
