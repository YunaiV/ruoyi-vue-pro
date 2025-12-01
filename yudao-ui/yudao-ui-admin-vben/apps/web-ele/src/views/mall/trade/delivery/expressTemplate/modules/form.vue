<script lang="ts" setup>
import type { MallDeliveryExpressTemplateApi } from '#/api/mall/trade/delivery/expressTemplate';
import type { SystemAreaApi } from '#/api/system/area';

import { computed, onMounted, ref } from 'vue';

import { useVbenModal } from '@vben/common-ui';
import { cloneDeep, fenToYuan, yuanToFen } from '@vben/utils';

import { ElMessage } from 'element-plus';

import { useVbenForm } from '#/adapter/form';
import {
  createDeliveryExpressTemplate,
  getDeliveryExpressTemplate,
  updateDeliveryExpressTemplate,
} from '#/api/mall/trade/delivery/expressTemplate';
import { getAreaTree } from '#/api/system/area';
import { $t } from '#/locales';

import { useFormSchema } from '../data';
import ChargeItemForm from './charge-item-form.vue';
import FreeItemForm from './free-item-form.vue';

const emit = defineEmits(['success']);
const formData = ref<MallDeliveryExpressTemplateApi.DeliveryExpressTemplate>();
const chargeItemFormRef = ref<InstanceType<typeof ChargeItemForm>>();
const freeItemFormRef = ref<InstanceType<typeof FreeItemForm>>();
const areaTree = ref<SystemAreaApi.Area[]>([]);

const getTitle = computed(() => {
  return formData.value?.id
    ? $t('ui.actionTitle.edit', ['快递模板'])
    : $t('ui.actionTitle.create', ['快递模板']);
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
  schema: useFormSchema(),
  showDefaultActions: false,
  handleValuesChange: (values, changedFields) => {
    // 目的：触发子表单的 columns 变化
    if (changedFields.includes('chargeMode')) {
      formData.value!.chargeMode = values.chargeMode;
    }
  },
});

/** 更新运费设置 */
const handleUpdateCharges = async (
  charges: MallDeliveryExpressTemplateApi.DeliveryExpressTemplateCharge[],
) => {
  formData.value =
    await formApi.getValues<MallDeliveryExpressTemplateApi.DeliveryExpressTemplate>();
  formData.value.charges = charges;
  await formApi.setValues({
    charges,
  });
};

/** 更新包邮设置 */
const handleUpdateFrees = async (
  frees: MallDeliveryExpressTemplateApi.DeliveryExpressTemplateFree[],
) => {
  formData.value =
    await formApi.getValues<MallDeliveryExpressTemplateApi.DeliveryExpressTemplate>();
  formData.value.frees = frees;
  await formApi.setValues({
    frees,
  });
};

/** 创建或更新快递模板 */
const [Modal, modalApi] = useVbenModal({
  async onConfirm() {
    const { valid } = await formApi.validate();
    if (!valid) {
      return;
    }
    const chargeFormInstance = Array.isArray(chargeItemFormRef.value)
      ? chargeItemFormRef.value[0]
      : chargeItemFormRef.value;
    const freeFormInstance = Array.isArray(freeItemFormRef.value)
      ? freeItemFormRef.value[0]
      : freeItemFormRef.value;
    try {
      chargeFormInstance.validate();
      freeFormInstance.validate();
    } catch (error: any) {
      ElMessage.error(error.message || '子表单验证失败');
      return;
    }

    modalApi.lock();
    // 提交表单
    const data = cloneDeep(
      await formApi.getValues(),
    ) as MallDeliveryExpressTemplateApi.DeliveryExpressTemplate;
    try {
      // 转换金额单位
      data.charges?.forEach(
        (
          item: MallDeliveryExpressTemplateApi.DeliveryExpressTemplateCharge,
        ) => {
          item.startPrice = yuanToFen(item.startPrice);
          item.extraPrice = yuanToFen(item.extraPrice);
        },
      );
      data.frees?.forEach(
        (item: MallDeliveryExpressTemplateApi.DeliveryExpressTemplateFree) => {
          item.freePrice = yuanToFen(item.freePrice);
        },
      );
      await (formData.value?.id
        ? updateDeliveryExpressTemplate(data)
        : createDeliveryExpressTemplate(data));
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
    const data =
      modalApi.getData<MallDeliveryExpressTemplateApi.DeliveryExpressTemplate>();
    if (!data || !data.id) {
      return;
    }
    modalApi.lock();
    try {
      formData.value = await getDeliveryExpressTemplate(data.id);
      // 转换金额单位
      formData.value.charges?.forEach(
        (
          item: MallDeliveryExpressTemplateApi.DeliveryExpressTemplateCharge,
        ) => {
          item.startPrice = Number.parseFloat(fenToYuan(item.startPrice));
          item.extraPrice = Number.parseFloat(fenToYuan(item.extraPrice));
        },
      );
      formData.value.frees?.forEach(
        (item: MallDeliveryExpressTemplateApi.DeliveryExpressTemplateFree) => {
          item.freePrice = Number.parseFloat(fenToYuan(item.freePrice));
        },
      );
      // 设置到 values
      await formApi.setValues(formData.value);
    } finally {
      modalApi.unlock();
    }
  },
});

/** 初始化 */
onMounted(async () => {
  areaTree.value = await getAreaTree();
});
</script>

<template>
  <Modal class="w-1/2" :title="getTitle">
    <Form class="mx-3">
      <template #charges>
        <ChargeItemForm
          ref="chargeItemFormRef"
          :items="formData?.charges"
          :charge-mode="formData?.chargeMode"
          :area-tree="areaTree"
          @update:items="handleUpdateCharges"
        />
      </template>
      <template #frees>
        <FreeItemForm
          ref="freeItemFormRef"
          :items="formData?.frees"
          :charge-mode="formData?.chargeMode"
          :area-tree="areaTree"
          @update:items="handleUpdateFrees"
        />
      </template>
    </Form>
  </Modal>
</template>
