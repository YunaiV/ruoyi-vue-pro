<!-- 操作符选择器组件 -->
<script setup lang="ts">
import { computed, watch } from 'vue';

import { useVModel } from '@vueuse/core';
import { Select } from 'ant-design-vue';

import {
  IoTDataSpecsDataTypeEnum,
  IotRuleSceneTriggerConditionParameterOperatorEnum,
} from '#/views/iot/utils/constants';

/** 操作符选择器组件 */
defineOptions({ name: 'OperatorSelector' });

const props = defineProps<{
  modelValue?: string;
  propertyType?: string;
}>();

const emit = defineEmits<{
  (e: 'update:modelValue', value: string): void;
  (e: 'change', value: string): void;
}>();

const localValue = useVModel(props, 'modelValue', emit);

// 基于枚举的操作符定义
const allOperators = [
  {
    value: IotRuleSceneTriggerConditionParameterOperatorEnum.EQUALS.value,
    label: IotRuleSceneTriggerConditionParameterOperatorEnum.EQUALS.name,
    symbol: '=',
    description: '值完全相等时触发',
    example: 'temperature = 25',
    supportedTypes: [
      IoTDataSpecsDataTypeEnum.INT,
      IoTDataSpecsDataTypeEnum.FLOAT,
      IoTDataSpecsDataTypeEnum.DOUBLE,
      IoTDataSpecsDataTypeEnum.TEXT,
      IoTDataSpecsDataTypeEnum.BOOL,
      IoTDataSpecsDataTypeEnum.ENUM,
    ],
  },
  {
    value: IotRuleSceneTriggerConditionParameterOperatorEnum.NOT_EQUALS.value,
    label: IotRuleSceneTriggerConditionParameterOperatorEnum.NOT_EQUALS.name,
    symbol: '≠',
    description: '值不相等时触发',
    example: 'power != false',
    supportedTypes: [
      IoTDataSpecsDataTypeEnum.INT,
      IoTDataSpecsDataTypeEnum.FLOAT,
      IoTDataSpecsDataTypeEnum.DOUBLE,
      IoTDataSpecsDataTypeEnum.TEXT,
      IoTDataSpecsDataTypeEnum.BOOL,
      IoTDataSpecsDataTypeEnum.ENUM,
    ],
  },
  {
    value: IotRuleSceneTriggerConditionParameterOperatorEnum.GREATER_THAN.value,
    label: IotRuleSceneTriggerConditionParameterOperatorEnum.GREATER_THAN.name,
    symbol: '>',
    description: '值大于指定值时触发',
    example: 'temperature > 30',
    supportedTypes: [
      IoTDataSpecsDataTypeEnum.INT,
      IoTDataSpecsDataTypeEnum.FLOAT,
      IoTDataSpecsDataTypeEnum.DOUBLE,
      IoTDataSpecsDataTypeEnum.DATE,
    ],
  },
  {
    value:
      IotRuleSceneTriggerConditionParameterOperatorEnum.GREATER_THAN_OR_EQUALS
        .value,
    label:
      IotRuleSceneTriggerConditionParameterOperatorEnum.GREATER_THAN_OR_EQUALS
        .name,
    symbol: '≥',
    description: '值大于或等于指定值时触发',
    example: 'humidity >= 80',
    supportedTypes: [
      IoTDataSpecsDataTypeEnum.INT,
      IoTDataSpecsDataTypeEnum.FLOAT,
      IoTDataSpecsDataTypeEnum.DOUBLE,
      IoTDataSpecsDataTypeEnum.DATE,
    ],
  },
  {
    value: IotRuleSceneTriggerConditionParameterOperatorEnum.LESS_THAN.value,
    label: IotRuleSceneTriggerConditionParameterOperatorEnum.LESS_THAN.name,
    symbol: '<',
    description: '值小于指定值时触发',
    example: 'temperature < 10',
    supportedTypes: [
      IoTDataSpecsDataTypeEnum.INT,
      IoTDataSpecsDataTypeEnum.FLOAT,
      IoTDataSpecsDataTypeEnum.DOUBLE,
      IoTDataSpecsDataTypeEnum.DATE,
    ],
  },
  {
    value:
      IotRuleSceneTriggerConditionParameterOperatorEnum.LESS_THAN_OR_EQUALS
        .value,
    label:
      IotRuleSceneTriggerConditionParameterOperatorEnum.LESS_THAN_OR_EQUALS
        .name,
    symbol: '≤',
    description: '值小于或等于指定值时触发',
    example: 'battery <= 20',
    supportedTypes: [
      IoTDataSpecsDataTypeEnum.INT,
      IoTDataSpecsDataTypeEnum.FLOAT,
      IoTDataSpecsDataTypeEnum.DOUBLE,
      IoTDataSpecsDataTypeEnum.DATE,
    ],
  },
  {
    value: IotRuleSceneTriggerConditionParameterOperatorEnum.IN.value,
    label: IotRuleSceneTriggerConditionParameterOperatorEnum.IN.name,
    symbol: '∈',
    description: '值在指定列表中时触发',
    example: 'status in [1,2,3]',
    supportedTypes: [
      IoTDataSpecsDataTypeEnum.INT,
      IoTDataSpecsDataTypeEnum.FLOAT,
      IoTDataSpecsDataTypeEnum.TEXT,
      IoTDataSpecsDataTypeEnum.ENUM,
    ],
  },
  {
    value: IotRuleSceneTriggerConditionParameterOperatorEnum.NOT_IN.value,
    label: IotRuleSceneTriggerConditionParameterOperatorEnum.NOT_IN.name,
    symbol: '∉',
    description: '值不在指定列表中时触发',
    example: 'status not in [1,2,3]',
    supportedTypes: [
      IoTDataSpecsDataTypeEnum.INT,
      IoTDataSpecsDataTypeEnum.FLOAT,
      IoTDataSpecsDataTypeEnum.TEXT,
      IoTDataSpecsDataTypeEnum.ENUM,
    ],
  },
  {
    value: IotRuleSceneTriggerConditionParameterOperatorEnum.BETWEEN.value,
    label: IotRuleSceneTriggerConditionParameterOperatorEnum.BETWEEN.name,
    symbol: '⊆',
    description: '值在指定范围内时触发',
    example: 'temperature between 20,30',
    supportedTypes: [
      IoTDataSpecsDataTypeEnum.INT,
      IoTDataSpecsDataTypeEnum.FLOAT,
      IoTDataSpecsDataTypeEnum.DOUBLE,
      IoTDataSpecsDataTypeEnum.DATE,
    ],
  },
  {
    value: IotRuleSceneTriggerConditionParameterOperatorEnum.NOT_BETWEEN.value,
    label: IotRuleSceneTriggerConditionParameterOperatorEnum.NOT_BETWEEN.name,
    symbol: '⊄',
    description: '值不在指定范围内时触发',
    example: 'temperature not between 20,30',
    supportedTypes: [
      IoTDataSpecsDataTypeEnum.INT,
      IoTDataSpecsDataTypeEnum.FLOAT,
      IoTDataSpecsDataTypeEnum.DOUBLE,
      IoTDataSpecsDataTypeEnum.DATE,
    ],
  },
  {
    value: IotRuleSceneTriggerConditionParameterOperatorEnum.LIKE.value,
    label: IotRuleSceneTriggerConditionParameterOperatorEnum.LIKE.name,
    symbol: '≈',
    description: '字符串匹配指定模式时触发',
    example: 'message like "%error%"',
    supportedTypes: [IoTDataSpecsDataTypeEnum.TEXT],
  },
  {
    value: IotRuleSceneTriggerConditionParameterOperatorEnum.NOT_NULL.value,
    label: IotRuleSceneTriggerConditionParameterOperatorEnum.NOT_NULL.name,
    symbol: '≠∅',
    description: '值非空时触发',
    example: 'data not null',
    supportedTypes: [
      IoTDataSpecsDataTypeEnum.INT,
      IoTDataSpecsDataTypeEnum.FLOAT,
      IoTDataSpecsDataTypeEnum.DOUBLE,
      IoTDataSpecsDataTypeEnum.TEXT,
      IoTDataSpecsDataTypeEnum.BOOL,
      IoTDataSpecsDataTypeEnum.ENUM,
      IoTDataSpecsDataTypeEnum.DATE,
    ],
  },
];

// 计算属性：可用的操作符
const availableOperators = computed(() => {
  if (!props.propertyType) {
    return allOperators;
  }
  return allOperators.filter((op) =>
    (op.supportedTypes as any[]).includes(props.propertyType || ''),
  );
});

// 计算属性：当前选中的操作符
const selectedOperator = computed(() => {
  return allOperators.find((op) => op.value === localValue.value);
});

/**
 * 处理选择变化事件
 * @param value 选中的操作符值
 */
function handleChange(value: any) {
  emit('change', value);
}

/** 监听属性类型变化 */
watch(
  () => props.propertyType,
  () => {
    // 如果当前选择的操作符不支持新的属性类型，则清空选择
    if (
      localValue.value &&
      selectedOperator.value &&
      !(selectedOperator.value.supportedTypes as any[]).includes(
        props.propertyType || '',
      )
    ) {
      localValue.value = '';
    }
  },
);
</script>

<template>
  <div class="w-full">
    <Select
      v-model="localValue"
      placeholder="请选择操作符"
      @change="handleChange"
      class="w-full"
    >
      <Select.Option
        v-for="operator in availableOperators"
        :key="operator.value"
        :label="operator.label"
        :value="operator.value"
      >
        <div class="py-4px flex w-full items-center justify-between">
          <div class="gap-8px flex items-center">
            <div class="text-14px font-500 text-primary">
              {{ operator.label }}
            </div>
            <div
              class="text-12px px-6px py-2px rounded-4px bg-primary-light-9 font-mono text-primary"
            >
              {{ operator.symbol }}
            </div>
          </div>
          <div class="text-12px text-secondary">
            {{ operator.description }}
          </div>
        </div>
      </Select.Option>
    </Select>
  </div>
</template>

<style scoped>
:deep(.el-select-dropdown__item) {
  height: auto;
  padding: 8px 20px;
}
</style>
