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

import ConditionConfig from './ConditionConfig.vue';

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
const addCondition = async () => {
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
};

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
  <div class="p-16px">
    <!-- 空状态 -->
    <div v-if="!subGroup || subGroup.length === 0" class="py-24px text-center">
      <div class="gap-12px flex flex-col items-center">
        <IconifyIcon
          icon="ep:plus"
          class="text-32px text-[var(--el-text-color-placeholder)]"
        />
        <div class="text-[var(--el-text-color-secondary)]">
          <p class="text-14px font-500 mb-4px">暂无条件</p>
          <p class="text-12px">点击下方按钮添加第一个条件</p>
        </div>
        <Button type="primary" @click="addCondition">
          <IconifyIcon icon="ep:plus" />
          添加条件
        </Button>
      </div>
    </div>

    <!-- 条件列表 -->
    <div v-else class="space-y-16px">
      <div
        v-for="(condition, conditionIndex) in subGroup"
        :key="`condition-${conditionIndex}`"
        class="relative"
      >
        <!-- 条件配置 -->
        <div
          class="rounded-6px border border-[var(--el-border-color-lighter)] bg-[var(--el-fill-color-blank)] shadow-sm"
        >
          <div
            class="p-12px rounded-t-4px flex items-center justify-between border-b border-[var(--el-border-color-lighter)] bg-[var(--el-fill-color-light)]"
          >
            <div class="gap-8px flex items-center">
              <div
                class="w-20px h-20px text-10px flex items-center justify-center rounded-full bg-blue-500 font-bold text-white"
              >
                {{ conditionIndex + 1 }}
              </div>
              <span
                class="text-12px font-500 text-[var(--el-text-color-primary)]"
              >
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
              <IconifyIcon icon="ep:delete" />
            </Button>
          </div>

          <div class="p-12px">
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
        class="py-16px text-center"
      >
        <Button type="primary" plain @click="addCondition">
          <IconifyIcon icon="ep:plus" />
          继续添加条件
        </Button>
        <span
          class="mt-8px text-12px block text-[var(--el-text-color-secondary)]"
        >
          最多可添加 {{ maxConditions }} 个条件
        </span>
      </div>
    </div>
  </div>
</template>
