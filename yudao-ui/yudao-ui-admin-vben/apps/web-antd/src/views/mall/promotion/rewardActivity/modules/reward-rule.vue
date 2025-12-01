<script lang="ts" setup>
import type { MallRewardActivityApi } from '#/api/mall/promotion/reward/rewardActivity';

import { computed } from 'vue';

import { PromotionConditionTypeEnum } from '@vben/constants';

import { useVModel } from '@vueuse/core';
import {
  Button,
  Col,
  Form,
  FormItem,
  Input,
  InputNumber,
  Row,
  Switch,
  Tag,
} from 'ant-design-vue';

import RewardRuleCouponSelect from './reward-rule-coupon-select.vue';

defineOptions({ name: 'RewardRule' });

const props = defineProps<{
  modelValue: Partial<MallRewardActivityApi.RewardActivity>;
}>();

const emits = defineEmits<{
  (e: 'update:modelValue', v: any): void;
}>();

const formData = useVModel(props, 'modelValue', emits);

const isPriceCondition = computed(() => {
  return (
    formData.value?.conditionType === PromotionConditionTypeEnum.PRICE.type
  );
});

/** 处理新增 */
function handleAdd() {
  if (!formData.value.rules) {
    formData.value.rules = [];
  }
  formData.value.rules.push({
    limit: 0,
    discountPrice: 0,
    freeDelivery: false,
    point: 0,
  });
}

/** 处理删除 */
function handleDelete(ruleIndex: number) {
  formData.value.rules?.splice(ruleIndex, 1);
}
</script>

<template>
  <Row :gutter="[16, 16]">
    <template v-if="formData.rules">
      <Col v-for="(rule, index) in formData.rules" :key="index" :span="24">
        <!-- 规则标题 -->
        <div class="mb-4 flex items-center">
          <span class="text-base font-bold">活动层级 {{ index + 1 }}</span>
          <Button
            v-if="index !== 0"
            type="link"
            danger
            class="ml-2"
            @click="handleDelete(index)"
          >
            删除
          </Button>
        </div>

        <Form :model="rule" layout="horizontal">
          <!-- 优惠门槛 -->
          <FormItem label="优惠门槛" :label-col="{ span: 4 }">
            <div class="flex items-center gap-2">
              <span>满</span>
              <InputNumber
                v-if="isPriceCondition"
                v-model:value="rule.limit"
                :min="0"
                :precision="2"
                :step="0.1"
                class="!w-40"
                placeholder="请输入金额"
              />
              <Input
                v-else
                v-model:value="rule.limit"
                :min="0"
                class="!w-40"
                placeholder="请输入数量"
                type="number"
              />
              <span>{{ isPriceCondition ? '元' : '件' }}</span>
            </div>
          </FormItem>
          <!-- 优惠内容 -->
          <FormItem
            label="优惠内容"
            :label-col="{ span: 4 }"
            :wrapper-col="{ span: 20 }"
          >
            <div class="flex flex-col gap-2">
              <span>订单金额优惠</span>
              <div class="flex items-center gap-2">
                <span>减</span>
                <InputNumber
                  v-model:value="rule.discountPrice"
                  :min="0"
                  :precision="2"
                  :step="0.1"
                  class="!w-32"
                  placeholder="请输入金额"
                />
                <span>元</span>
              </div>
              <div class="flex items-center gap-2">
                <span>包邮:</span>
                <Switch
                  v-model:checked="rule.freeDelivery"
                  checked-children="是"
                  un-checked-children="否"
                />
              </div>
              <div>
                <div>送积分:</div>
                <div class="mt-2 flex items-center gap-2">
                  <span>送</span>
                  <InputNumber
                    v-model:value="rule.point"
                    :min="0"
                    class="!w-32"
                    placeholder="请输入积分"
                  />
                  <span>积分</span>
                </div>
              </div>
              <div class="flex items-center gap-2">
                <span class="w-20">送优惠券:</span>
                <RewardRuleCouponSelect
                  :model-value="rule"
                  @update:model-value="(val) => (formData.rules![index] = val)"
                />
              </div>
            </div>
          </FormItem>
        </Form>
      </Col>
    </template>

    <!-- 添加规则按钮 -->
    <Col :span="24" class="mt-2">
      <Button type="primary" @click="handleAdd">+ 添加优惠规则</Button>
    </Col>

    <!-- 提示信息 -->
    <Col :span="24" class="mt-2">
      <Tag color="warning">
        提示：赠送积分为 0 时不赠送；未选择优惠券时不赠送。
      </Tag>
    </Col>
  </Row>
</template>
