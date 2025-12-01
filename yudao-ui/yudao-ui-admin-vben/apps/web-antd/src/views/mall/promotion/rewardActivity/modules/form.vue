<script lang="ts" setup>
import type { MallRewardActivityApi } from '#/api/mall/promotion/reward/rewardActivity';

import { computed, ref } from 'vue';

import { useVbenModal } from '@vben/common-ui';
import {
  PromotionConditionTypeEnum,
  PromotionProductScopeEnum,
} from '@vben/constants';
import { convertToInteger, formatToFraction } from '@vben/utils';

import { message } from 'ant-design-vue';

import { useVbenForm } from '#/adapter/form';
import {
  createRewardActivity,
  getReward,
  updateRewardActivity,
} from '#/api/mall/promotion/reward/rewardActivity';
import { $t } from '#/locales';
import { ProductCategorySelect } from '#/views/mall/product/category/components';
import { SpuShowcase } from '#/views/mall/product/spu/components';

import { useFormSchema } from '../data';
import RewardRule from './reward-rule.vue';

const emit = defineEmits(['success']);

const formData = ref<Partial<MallRewardActivityApi.RewardActivity>>({
  conditionType: PromotionConditionTypeEnum.PRICE.type,
  rules: [],
});

const getTitle = computed(() => {
  return formData.value?.id
    ? $t('ui.actionTitle.edit', ['满减送'])
    : $t('ui.actionTitle.create', ['满减送']);
});

const [Form, formApi] = useVbenForm({
  commonConfig: {
    componentProps: {
      class: 'w-full',
    },
    labelWidth: 100,
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
    try {
      const values = await formApi.getValues();
      // 合并表单值和 formData（含 id、productSpuIds、productCategoryIds 等）
      const data = { ...formData.value, ...values };
      if (data.startAndEndTime && Array.isArray(data.startAndEndTime)) {
        data.startTime = data.startAndEndTime[0];
        data.endTime = data.startAndEndTime[1];
        delete data.startAndEndTime;
      }
      data.rules?.forEach((item: any) => {
        item.discountPrice = convertToInteger(item.discountPrice || 0);
        if (data.conditionType === PromotionConditionTypeEnum.PRICE.type) {
          item.limit = convertToInteger(item.limit || 0);
        }
      });
      // 设置 productScopeValues
      switch (data.productScope) {
        case PromotionProductScopeEnum.CATEGORY.scope: {
          const categoryIds = data.productCategoryIds;
          data.productScopeValues = Array.isArray(categoryIds)
            ? categoryIds
            : categoryIds
              ? [categoryIds]
              : [];
          break;
        }
        case PromotionProductScopeEnum.SPU.scope: {
          data.productScopeValues = data.productSpuIds;
          break;
        }
      }

      await (data.id
        ? updateRewardActivity(data as MallRewardActivityApi.RewardActivity)
        : createRewardActivity(data as MallRewardActivityApi.RewardActivity));
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
      formData.value = {};
      return;
    }
    // 加载数据
    const data = modalApi.getData<MallRewardActivityApi.RewardActivity>();
    if (!data || !data.id) {
      return;
    }
    modalApi.lock();
    try {
      const result = await getReward(data.id);
      result.startAndEndTime = [result.startTime, result.endTime] as any[];
      result.rules?.forEach((item: any) => {
        item.discountPrice = formatToFraction(item.discountPrice || 0);
        if (result.conditionType === PromotionConditionTypeEnum.PRICE.type) {
          item.limit = formatToFraction(item.limit || 0);
        }
      });
      formData.value = result;
      // 设置到 values
      await formApi.setValues(result);
    } finally {
      modalApi.unlock();
    }
  },
});
</script>

<template>
  <Modal :title="getTitle" class="w-2/3">
    <Form class="mx-6">
      <!-- 自定义插槽：优惠规则 -->
      <template #rules>
        <RewardRule v-model="formData" />
      </template>
      <!-- 自定义插槽：商品选择 -->
      <template #productSpuIds>
        <SpuShowcase v-model="formData.productSpuIds" />
      </template>
      <!-- 自定义插槽：分类选择 -->
      <template #productCategoryIds>
        <ProductCategorySelect v-model="formData.productCategoryIds" multiple />
      </template>
    </Form>
  </Modal>
</template>
