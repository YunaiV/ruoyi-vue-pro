<script lang="ts" setup>
import type { ErpFinancePaymentApi } from '#/api/erp/finance/payment';

import { computed, ref } from 'vue';

import { useVbenModal } from '@vben/common-ui';
import { $t } from '@vben/locales';

import { message } from 'ant-design-vue';

import { useVbenForm } from '#/adapter/form';
import { getAccountSimpleList } from '#/api/erp/finance/account';
import {
  createFinancePayment,
  getFinancePayment,
  updateFinancePayment,
} from '#/api/erp/finance/payment';

import { useFormSchema } from '../data';
import ItemForm from './item-form.vue';

const emit = defineEmits(['success']);
const formData = ref<
  ErpFinancePaymentApi.FinancePayment & {
    fileUrl?: string;
  }
>({
  id: undefined,
  no: '',
  supplierId: undefined,
  accountId: undefined,
  financeUserId: undefined,
  paymentTime: undefined,
  remark: '',
  fileUrl: undefined,
  totalPrice: 0,
  discountPrice: 0,
  paymentPrice: 0,
  items: [],
  status: 0,
});

const formType = ref(''); // 表单类型：'create' | 'edit' | 'detail'
const itemFormRef = ref<InstanceType<typeof ItemForm>>();

const getTitle = computed(() => {
  if (formType.value === 'create') {
    return $t('ui.actionTitle.create', ['付款单']);
  } else if (formType.value === 'edit') {
    return $t('ui.actionTitle.edit', ['付款单']);
  } else {
    return '付款单详情';
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
    if (formData.value) {
      if (changedFields.includes('supplierId')) {
        formData.value.supplierId = values.supplierId;
      }
      // 目的：同步到 item-form 组件，触发整体的价格计算
      if (changedFields.includes('discountPrice')) {
        formData.value.discountPrice = values.discountPrice;
        formData.value.paymentPrice =
          formData.value.totalPrice - values.discountPrice;
        formApi.setValues({
          paymentPrice: formData.value.paymentPrice,
        });
      }
    }
  },
});

/** 更新付款项 */
function handleUpdateItems(items: ErpFinancePaymentApi.FinancePaymentItem[]) {
  formData.value.items = items;
  formApi.setValues({
    items,
  });
}

/** 更新总金额 */
function handleUpdateTotalPrice(totalPrice: number) {
  formData.value.totalPrice = totalPrice;
  formApi.setValues({
    totalPrice: formData.value.totalPrice,
  });
}

/** 更新付款金额 */
function handleUpdatePaymentPrice(paymentPrice: number) {
  formData.value.paymentPrice = paymentPrice;
  formApi.setValues({
    paymentPrice: formData.value.paymentPrice,
  });
}

/** 创建或更新付款单 */
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
    const data =
      (await formApi.getValues()) as ErpFinancePaymentApi.FinancePayment;
    try {
      await (formType.value === 'create'
        ? createFinancePayment(data)
        : updateFinancePayment(data));
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
      formData.value = {} as ErpFinancePaymentApi.FinancePayment;
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
      formData.value = await getFinancePayment(data.id);
      // 设置到 values
      await formApi.setValues(formData.value, false);
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
          :supplier-id="formData?.supplierId"
          :disabled="formType === 'detail'"
          :discount-price="formData?.discountPrice ?? 0"
          @update:items="handleUpdateItems"
          @update:total-price="handleUpdateTotalPrice"
          @update:payment-price="handleUpdatePaymentPrice"
          class="w-full"
        />
      </template>
    </Form>
  </Modal>
</template>
