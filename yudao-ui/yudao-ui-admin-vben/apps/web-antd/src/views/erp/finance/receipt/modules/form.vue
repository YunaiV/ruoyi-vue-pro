<script lang="ts" setup>
import type { ErpFinanceReceiptApi } from '#/api/erp/finance/receipt';

import { computed, ref } from 'vue';

import { useVbenModal } from '@vben/common-ui';
import { $t } from '@vben/locales';

import { message } from 'ant-design-vue';

import { useVbenForm } from '#/adapter/form';
import { getAccountSimpleList } from '#/api/erp/finance/account';
import {
  createFinanceReceipt,
  getFinanceReceipt,
  updateFinanceReceipt,
} from '#/api/erp/finance/receipt';

import { useFormSchema } from '../data';
import ItemForm from './item-form.vue';

const emit = defineEmits(['success']);
const formData = ref<
  ErpFinanceReceiptApi.FinanceReceipt & {
    fileUrl?: string;
  }
>({
  id: undefined,
  no: '',
  customerId: 0,
  accountId: 0,
  financeUserId: 0,
  receiptTime: new Date(),
  remark: '',
  fileUrl: undefined,
  totalPrice: 0,
  discountPrice: 0,
  receiptPrice: 0,
  items: [],
  status: 0,
});

const formType = ref(''); // 表单类型：'create' | 'edit' | 'detail'
const itemFormRef = ref<InstanceType<typeof ItemForm>>();

const getTitle = computed(() => {
  if (formType.value === 'create') {
    return $t('ui.actionTitle.create', ['收款单']);
  } else if (formType.value === 'edit') {
    return $t('ui.actionTitle.edit', ['收款单']);
  } else {
    return '收款单详情';
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
      if (changedFields.includes('customerId')) {
        formData.value.customerId = values.customerId;
      }
      // 目的：同步到 item-form 组件，触发整体的价格计算
      if (changedFields.includes('discountPrice')) {
        formData.value.discountPrice = values.discountPrice;
        formData.value.receiptPrice =
          formData.value.totalPrice - values.discountPrice;
        formApi.setValues({
          receiptPrice: formData.value.receiptPrice,
        });
      }
    }
  },
});

/** 更新收款项 */
function handleUpdateItems(items: ErpFinanceReceiptApi.FinanceReceiptItem[]) {
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

/** 更新收款金额 */
function handleUpdateReceiptPrice(receiptPrice: number) {
  formData.value.receiptPrice = receiptPrice;
  formApi.setValues({
    receiptPrice: formData.value.receiptPrice,
  });
}

/** 创建或更新收款单 */
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
      (await formApi.getValues()) as ErpFinanceReceiptApi.FinanceReceipt;
    try {
      await (formType.value === 'create'
        ? createFinanceReceipt(data)
        : updateFinanceReceipt(data));
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
      formData.value = {
        id: undefined,
        no: '',
        customerId: 0,
        accountId: 0,
        financeUserId: 0,
        receiptTime: new Date(),
        remark: '',
        totalPrice: 0,
        discountPrice: 0,
        receiptPrice: 0,
        status: 0,
        items: [],
        bizNo: '',
      };
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
      formData.value = await getFinanceReceipt(data.id);
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
          :customer-id="formData?.customerId"
          :disabled="formType === 'detail'"
          :discount-price="formData?.discountPrice ?? 0"
          @update:items="handleUpdateItems"
          @update:total-price="handleUpdateTotalPrice"
          @update:receipt-price="handleUpdateReceiptPrice"
          class="w-full"
        />
      </template>
    </Form>
  </Modal>
</template>
