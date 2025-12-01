<script setup lang="ts">
import type { TriggerCondition } from '#/api/iot/rule/scene';

import { computed, nextTick } from 'vue';

import { IconifyIcon } from '@vben/icons';

import { useVModel } from '@vueuse/core';
import { Button } from 'ant-design-vue';

import {
  IotRuleSceneTriggerConditionParameterOperatorEnum,
  IotRuleSceneTriggerConditionTypeEnum,
} from '#/views/iot/utils/constants';

import ConditionConfig from './condition-config.vue';

/** 子条件组配置组件 */
defineOptions({ name: 'SubConditionGroupConfig' });

const props = defineProps<{
  maxConditions?: number;
  modelValue: TriggerCondition[];
  triggerType: number;
}>();

const emit = defineEmits<{
  (e: 'update:modelValue', value: TriggerCondition[]): void;
}>();

const subGroup = useVModel(props, 'modelValue', emit);

const maxConditions = computed(() => props.maxConditions || 3); // 最大条件数量

/** 添加条件 */
async function addCondition() {
  // 确保 subGroup.value 是一个数组
  if (!subGroup.value) {
    subGroup.value = [];
  }

  // 检查是否达到最大条件数量限制
  if (subGroup.value?.length >= maxConditions.value) {
    return;
  }

  const newCondition: TriggerCondition = {
    type: IotRuleSceneTriggerConditionTypeEnum.DEVICE_PROPERTY.toString(), // 默认为设备属性
    productId: undefined,
    deviceId: undefined,
    identifier: '',
    operator: IotRuleSceneTriggerConditionParameterOperatorEnum.EQUALS.value, // 使用枚举默认值
    param: '',
  };

  // 使用 nextTick 确保响应式更新完成后再添加新条件
  await nextTick();
  if (subGroup.value) {
    subGroup.value.push(newCondition);
  }
}

/**
 * 移除条件
 * @param index 条件索引
 */
function removeCondition(index: number) {
  if (subGroup.value) {
    subGroup.value.splice(index, 1);
  }
}

/**
 * 更新条件
 * @param index 条件索引
 * @param condition 条件对象
 */
function updateCondition(index: number, condition: TriggerCondition) {
  if (subGroup.value) {
    subGroup.value[index] = condition;
  }
}
</script>

<template>
  <div class="p-4">
    <!-- 空状态 -->
    <div v-if="!subGroup || subGroup.length === 0" class="py-6 text-center">
      <div class="flex flex-col items-center gap-3">
        <IconifyIcon icon="lucide:plus" class="text-8 text-secondary" />
        <div class="text-secondary">
          <p class="mb-1 text-base font-bold">暂无条件</p>
          <p class="text-xs">点击下方按钮添加第一个条件</p>
        </div>
        <Button type="primary" @click="addCondition">
          <IconifyIcon icon="lucide:plus" />
          添加条件
        </Button>
      </div>
    </div>

    <!-- 条件列表 -->
    <div v-else class="space-y-4">
      <div
        v-for="(condition, conditionIndex) in subGroup"
        :key="`condition-${conditionIndex}`"
        class="relative"
      >
        <!-- 条件配置 -->
        <div
          class="rounded-3px bg-fill-color-blank border border-border shadow-sm"
        >
          <div
            class="rounded-t-1 bg-fill-color-blank flex items-center justify-between border-b border-border p-3"
          >
            <div class="flex items-center gap-2">
              <div
                class="flex size-5 items-center justify-center rounded-full bg-primary text-xs font-bold text-white"
              >
                {{ conditionIndex + 1 }}
              </div>
              <span class="text-base font-bold text-primary">
                条件 {{ conditionIndex + 1 }}
              </span>
            </div>
            <Button
              danger
              size="small"
              text
              @click="removeCondition(conditionIndex)"
              v-if="subGroup!.length > 1"
              class="hover:bg-red-50"
            >
              <IconifyIcon icon="lucide:trash-2" />
            </Button>
          </div>

          <div class="p-3">
            <ConditionConfig
              :model-value="condition"
              @update:model-value="
                (value: TriggerCondition) =>
                  updateCondition(conditionIndex, value)
              "
              :trigger-type="triggerType"
            />
          </div>
        </div>
      </div>

      <!-- 添加条件按钮 -->
      <div
        v-if="
          subGroup && subGroup.length > 0 && subGroup.length < maxConditions
        "
        class="py-4 text-center"
      >
        <Button type="primary" plain @click="addCondition">
          <IconifyIcon icon="lucide:plus" />
          继续添加条件
        </Button>
        <span class="mt-2 block text-xs text-secondary">
          最多可添加 {{ maxConditions }} 个条件
        </span>
      </div>
    </div>
  </div>
</template>
