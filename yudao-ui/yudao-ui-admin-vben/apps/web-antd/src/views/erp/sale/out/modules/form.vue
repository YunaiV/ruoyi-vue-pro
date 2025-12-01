<script lang="ts" setup>
import type { ErpSaleOrderApi } from '#/api/erp/sale/order';
import type { ErpSaleOutApi } from '#/api/erp/sale/out';

import { computed, ref } from 'vue';

import { useVbenModal } from '@vben/common-ui';
import { $t } from '@vben/locales';

import { message } from 'ant-design-vue';

import { useVbenForm } from '#/adapter/form';
import { getAccountSimpleList } from '#/api/erp/finance/account';
import { createSaleOut, getSaleOut, updateSaleOut } from '#/api/erp/sale/out';

import { useFormSchema } from '../data';
import ItemForm from './item-form.vue';
import SaleOrderSelect from './sale-order-select.vue';

const emit = defineEmits(['success']);
const formData = ref<
  ErpSaleOutApi.SaleOut & {
    accountId?: number;
    customerId?: number;
    discountPercent?: number;
    fileUrl?: string;
    order?: ErpSaleOrderApi.SaleOrder;
    orderId?: number;
    orderNo?: string;
  }
>({
  id: undefined,
  no: undefined,
  accountId: undefined,
  outTime: undefined,
  remark: undefined,
  fileUrl: undefined,
  discountPercent: 0,
  customerId: undefined,
  discountPrice: 0,
  totalPrice: 0,
  otherPrice: 0,
  items: [],
});
const formType = ref(''); // 表单类型：'create' | 'edit' | 'detail'
const itemFormRef = ref<InstanceType<typeof ItemForm>>();

/* eslint-disable unicorn/no-nested-ternary */
const getTitle = computed(() =>
  formType.value === 'create'
    ? $t('ui.actionTitle.create', ['销售出库'])
    : formType.value === 'edit'
      ? $t('ui.actionTitle.edit', ['销售出库'])
      : '销售出库详情',
);

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
    if (formData.value) {
      if (changedFields.includes('otherPrice')) {
        formData.value.otherPrice = values.otherPrice;
      }
      if (changedFields.includes('discountPercent')) {
        formData.value.discountPercent = values.discountPercent;
      }
    }
  },
});

/** 更新销售出库项 */
function handleUpdateItems(items: ErpSaleOutApi.SaleOutItem[]) {
  formData.value.items = items;
  formApi.setValues({
    items,
  });
}

/** 更新其他费用 */
function handleUpdateOtherPrice(otherPrice: number) {
  formApi.setValues({
    otherPrice,
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

/** 选择销售订单 */
function handleUpdateOrder(order: ErpSaleOrderApi.SaleOrder) {
  formData.value = {
    ...formData.value,
    orderId: order.id,
    orderNo: order.no!,
    customerId: order.customerId!,
    accountId: order.accountId!,
    remark: order.remark!,
    discountPercent: order.discountPercent!,
    fileUrl: order.fileUrl!,
  };
  // 将订单项设置到出库单项
  order.items!.forEach((item: any) => {
    item.totalCount = item.count;
    item.count = item.totalCount - item.outCount;
    item.orderItemId = item.id;
    item.id = undefined;
  });
  formData.value.items = order.items!.filter(
    (item) => item.count && item.count > 0,
  ) as ErpSaleOutApi.SaleOutItem[];
  formApi.setValues(formData.value, false);
}

/** 创建或更新销售出库 */
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
    const data = (await formApi.getValues()) as ErpSaleOutApi.SaleOut;
    try {
      await (formType.value === 'create'
        ? createSaleOut(data)
        : updateSaleOut(data));
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
      formData.value = {} as ErpSaleOutApi.SaleOut;
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
      formData.value = await getSaleOut(data.id);
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
          :disabled="formType === 'detail'"
          :discount-percent="formData?.discountPercent ?? 0"
          :other-price="formData?.otherPrice ?? 0"
          @update:items="handleUpdateItems"
          @update:discount-price="handleUpdateDiscountPrice"
          @update:other-price="handleUpdateOtherPrice"
          @update:total-price="handleUpdateTotalPrice"
        />
      </template>
      <template #orderNo>
        <SaleOrderSelect
          :order-no="formData?.orderNo"
          @update:order="handleUpdateOrder"
        />
      </template>
    </Form>
  </Modal>
</template>
