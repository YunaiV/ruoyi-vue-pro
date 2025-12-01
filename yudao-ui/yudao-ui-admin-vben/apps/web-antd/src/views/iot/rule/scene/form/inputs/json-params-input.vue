<!-- JSON参数输入组件 - 通用版本 -->
<script setup lang="ts">
import type { JsonParamsInputType } from '#/views/iot/utils/constants';

import { computed, nextTick, onMounted, ref, watch } from 'vue';

import { IconifyIcon } from '@vben/icons';

import { useVModel } from '@vueuse/core';
import { Button, Input, Popover, Tag } from 'ant-design-vue';

import {
  IoTDataSpecsDataTypeEnum,
  JSON_PARAMS_EXAMPLE_VALUES,
  JSON_PARAMS_INPUT_CONSTANTS,
  JSON_PARAMS_INPUT_ICONS,
  JsonParamsInputTypeEnum,
} from '#/views/iot/utils/constants';

/** JSON参数输入组件 - 通用版本 */
defineOptions({ name: 'JsonParamsInput' });

const props = withDefaults(defineProps<Props>(), {
  type: JsonParamsInputTypeEnum.SERVICE,
  placeholder: JSON_PARAMS_INPUT_CONSTANTS.PLACEHOLDER,
});

const emit = defineEmits<Emits>();

interface JsonParamsConfig {
  // 服务配置
  service?: {
    inputParams?: any[];
    name: string;
  };
  // 事件配置
  event?: {
    name: string;
    outputParams?: any[];
  };
  // 属性配置
  properties?: any[];
  // 自定义配置
  custom?: {
    name: string;
    params: any[];
  };
}

interface Props {
  modelValue: string;
  config: JsonParamsConfig;
  type?: JsonParamsInputType;
  placeholder?: string;
}

interface Emits {
  (e: 'update:modelValue', value: string): void;
}

const localValue = useVModel(props, 'modelValue', emit, {
  defaultValue: '',
});

const paramsJson = ref(''); // JSON参数字符串
const jsonError = ref(''); // JSON验证错误信息

// 计算属性：参数列表
const paramsList = computed(() => {
  switch (props.type) {
    case JsonParamsInputTypeEnum.CUSTOM: {
      return props.config?.custom?.params || [];
    }
    case JsonParamsInputTypeEnum.EVENT: {
      return props.config?.event?.outputParams || [];
    }
    case JsonParamsInputTypeEnum.PROPERTY: {
      return props.config?.properties || [];
    }
    case JsonParamsInputTypeEnum.SERVICE: {
      return props.config?.service?.inputParams || [];
    }
    default: {
      return [];
    }
  }
});

// 计算属性：标题
const title = computed(() => {
  switch (props.type) {
    case JsonParamsInputTypeEnum.CUSTOM: {
      return JSON_PARAMS_INPUT_CONSTANTS.TITLES.CUSTOM(
        props.config?.custom?.name,
      );
    }
    case JsonParamsInputTypeEnum.EVENT: {
      return JSON_PARAMS_INPUT_CONSTANTS.TITLES.EVENT(
        props.config?.event?.name,
      );
    }
    case JsonParamsInputTypeEnum.PROPERTY: {
      return JSON_PARAMS_INPUT_CONSTANTS.TITLES.PROPERTY;
    }
    case JsonParamsInputTypeEnum.SERVICE: {
      return JSON_PARAMS_INPUT_CONSTANTS.TITLES.SERVICE(
        props.config?.service?.name,
      );
    }
    default: {
      return JSON_PARAMS_INPUT_CONSTANTS.TITLES.DEFAULT;
    }
  }
});

// 计算属性：标题图标
const titleIcon = computed(() => {
  switch (props.type) {
    case JsonParamsInputTypeEnum.CUSTOM: {
      return JSON_PARAMS_INPUT_ICONS.TITLE_ICONS.CUSTOM;
    }
    case JsonParamsInputTypeEnum.EVENT: {
      return JSON_PARAMS_INPUT_ICONS.TITLE_ICONS.EVENT;
    }
    case JsonParamsInputTypeEnum.PROPERTY: {
      return JSON_PARAMS_INPUT_ICONS.TITLE_ICONS.PROPERTY;
    }
    case JsonParamsInputTypeEnum.SERVICE: {
      return JSON_PARAMS_INPUT_ICONS.TITLE_ICONS.SERVICE;
    }
    default: {
      return JSON_PARAMS_INPUT_ICONS.TITLE_ICONS.DEFAULT;
    }
  }
});

// 计算属性：参数图标
const paramsIcon = computed(() => {
  switch (props.type) {
    case JsonParamsInputTypeEnum.CUSTOM: {
      return JSON_PARAMS_INPUT_ICONS.PARAMS_ICONS.CUSTOM;
    }
    case JsonParamsInputTypeEnum.EVENT: {
      return JSON_PARAMS_INPUT_ICONS.PARAMS_ICONS.EVENT;
    }
    case JsonParamsInputTypeEnum.PROPERTY: {
      return JSON_PARAMS_INPUT_ICONS.PARAMS_ICONS.PROPERTY;
    }
    case JsonParamsInputTypeEnum.SERVICE: {
      return JSON_PARAMS_INPUT_ICONS.PARAMS_ICONS.SERVICE;
    }
    default: {
      return JSON_PARAMS_INPUT_ICONS.PARAMS_ICONS.DEFAULT;
    }
  }
});

// 计算属性：参数标签
const paramsLabel = computed(() => {
  switch (props.type) {
    case JsonParamsInputTypeEnum.CUSTOM: {
      return JSON_PARAMS_INPUT_CONSTANTS.PARAMS_LABELS.CUSTOM;
    }
    case JsonParamsInputTypeEnum.EVENT: {
      return JSON_PARAMS_INPUT_CONSTANTS.PARAMS_LABELS.EVENT;
    }
    case JsonParamsInputTypeEnum.PROPERTY: {
      return JSON_PARAMS_INPUT_CONSTANTS.PARAMS_LABELS.PROPERTY;
    }
    case JsonParamsInputTypeEnum.SERVICE: {
      return JSON_PARAMS_INPUT_CONSTANTS.PARAMS_LABELS.SERVICE;
    }
    default: {
      return JSON_PARAMS_INPUT_CONSTANTS.PARAMS_LABELS.DEFAULT;
    }
  }
});

// 计算属性：空状态消息
const emptyMessage = computed(() => {
  switch (props.type) {
    case JsonParamsInputTypeEnum.CUSTOM: {
      return JSON_PARAMS_INPUT_CONSTANTS.EMPTY_MESSAGES.CUSTOM;
    }
    case JsonParamsInputTypeEnum.EVENT: {
      return JSON_PARAMS_INPUT_CONSTANTS.EMPTY_MESSAGES.EVENT;
    }
    case JsonParamsInputTypeEnum.PROPERTY: {
      return JSON_PARAMS_INPUT_CONSTANTS.EMPTY_MESSAGES.PROPERTY;
    }
    case JsonParamsInputTypeEnum.SERVICE: {
      return JSON_PARAMS_INPUT_CONSTANTS.EMPTY_MESSAGES.SERVICE;
    }
    default: {
      return JSON_PARAMS_INPUT_CONSTANTS.EMPTY_MESSAGES.DEFAULT;
    }
  }
});

// 计算属性：无配置消息
// const noConfigMessage = computed(() => {
//   switch (props.type) {
//     case JsonParamsInputTypeEnum.CUSTOM: {
//       return JSON_PARAMS_INPUT_CONSTANTS.NO_CONFIG_MESSAGES.CUSTOM;
//     }
//     case JsonParamsInputTypeEnum.EVENT: {
//       return JSON_PARAMS_INPUT_CONSTANTS.NO_CONFIG_MESSAGES.EVENT;
//     }
//     case JsonParamsInputTypeEnum.PROPERTY: {
//       return JSON_PARAMS_INPUT_CONSTANTS.NO_CONFIG_MESSAGES.PROPERTY;
//     }
//     case JsonParamsInputTypeEnum.SERVICE: {
//       return JSON_PARAMS_INPUT_CONSTANTS.NO_CONFIG_MESSAGES.SERVICE;
//     }
//     default: {
//       return JSON_PARAMS_INPUT_CONSTANTS.NO_CONFIG_MESSAGES.DEFAULT;
//     }
//   }
// });

/**
 * 处理参数变化事件
 */
function handleParamsChange() {
  try {
    jsonError.value = ''; // 清除之前的错误

    if (paramsJson.value.trim()) {
      const parsed = JSON.parse(paramsJson.value);
      localValue.value = paramsJson.value;

      // 额外的参数验证
      if (typeof parsed !== 'object' || parsed === null) {
        jsonError.value = JSON_PARAMS_INPUT_CONSTANTS.PARAMS_MUST_BE_OBJECT;
        return;
      }

      // 验证必填参数
      for (const param of paramsList.value) {
        if (
          param.required &&
          (!parsed[param.identifier] || parsed[param.identifier] === '')
        ) {
          jsonError.value = JSON_PARAMS_INPUT_CONSTANTS.PARAM_REQUIRED_ERROR(
            param.name,
          );
          return;
        }
      }
    } else {
      localValue.value = '';
    }

    // 验证通过
    jsonError.value = '';
  } catch (error) {
    jsonError.value = JSON_PARAMS_INPUT_CONSTANTS.JSON_FORMAT_ERROR(
      error instanceof Error
        ? error.message
        : JSON_PARAMS_INPUT_CONSTANTS.UNKNOWN_ERROR,
    );
  }
}

/**
 * 快速填充示例数据
 */
function fillExampleJson() {
  paramsJson.value = generateExampleJson();
  handleParamsChange();
}

/**
 * 清空参数
 */
function clearParams() {
  paramsJson.value = '';
  localValue.value = '';
  jsonError.value = '';
}

/**
 * 获取参数类型名称
 * @param dataType 数据类型
 * @returns 类型名称
 */
function getParamTypeName(dataType: string) {
  // 使用 constants.ts 中已有的 getDataTypeName 函数逻辑
  const typeMap: Record<string, string> = {
    [IoTDataSpecsDataTypeEnum.INT]: '整数',
    [IoTDataSpecsDataTypeEnum.FLOAT]: '浮点数',
    [IoTDataSpecsDataTypeEnum.DOUBLE]: '双精度',
    [IoTDataSpecsDataTypeEnum.TEXT]: '字符串',
    [IoTDataSpecsDataTypeEnum.BOOL]: '布尔值',
    [IoTDataSpecsDataTypeEnum.ENUM]: '枚举',
    [IoTDataSpecsDataTypeEnum.DATE]: '日期',
    [IoTDataSpecsDataTypeEnum.STRUCT]: '结构体',
    [IoTDataSpecsDataTypeEnum.ARRAY]: '数组',
  };
  return typeMap[dataType] || dataType;
}

/**
 * 获取参数类型标签样式
 * @param dataType 数据类型
 * @returns 标签样式
 */
function getParamTypeTag(dataType: string) {
  const tagMap: Record<string, string> = {
    [IoTDataSpecsDataTypeEnum.INT]: 'primary',
    [IoTDataSpecsDataTypeEnum.FLOAT]: 'success',
    [IoTDataSpecsDataTypeEnum.DOUBLE]: 'success',
    [IoTDataSpecsDataTypeEnum.TEXT]: 'info',
    [IoTDataSpecsDataTypeEnum.BOOL]: 'warning',
    [IoTDataSpecsDataTypeEnum.ENUM]: 'danger',
    [IoTDataSpecsDataTypeEnum.DATE]: 'primary',
    [IoTDataSpecsDataTypeEnum.STRUCT]: 'info',
    [IoTDataSpecsDataTypeEnum.ARRAY]: 'warning',
  };
  return tagMap[dataType] || 'info';
}

/**
 * 获取示例值
 * @param param 参数对象
 * @returns 示例值
 */
function getExampleValue(param: any) {
  const exampleConfig: any =
    JSON_PARAMS_EXAMPLE_VALUES[param.dataType] ||
    JSON_PARAMS_EXAMPLE_VALUES.DEFAULT;
  return exampleConfig.display;
}

/**
 * 生成示例JSON
 * @returns JSON字符串
 */
function generateExampleJson() {
  if (paramsList.value.length === 0) {
    return '{}';
  }

  const example: Record<string, any> = {};
  paramsList.value.forEach((param) => {
    const exampleConfig: any =
      JSON_PARAMS_EXAMPLE_VALUES[param.dataType] ||
      JSON_PARAMS_EXAMPLE_VALUES.DEFAULT;
    example[param.identifier] = exampleConfig.value;
  });

  return JSON.stringify(example, null, 2);
}

/**
 * 处理数据回显
 * @param value 值字符串
 */
function handleDataDisplay(value: string) {
  if (!value || !value.trim()) {
    paramsJson.value = '';
    jsonError.value = '';
    return;
  }

  try {
    // 尝试解析JSON，如果成功则格式化
    const parsed = JSON.parse(value);
    paramsJson.value = JSON.stringify(parsed, null, 2);
    jsonError.value = '';
  } catch {
    // 如果不是有效的JSON，直接使用原字符串
    paramsJson.value = value;
    jsonError.value = '';
  }
}

// 监听外部值变化（编辑模式数据回显）
watch(
  () => localValue.value,
  async (newValue, oldValue) => {
    // 避免循环更新
    if (newValue === oldValue) return;

    // 使用 nextTick 确保在下一个 tick 中处理数据
    await nextTick();
    handleDataDisplay(newValue || '');
  },
  { immediate: true },
);

// 组件挂载后也尝试处理一次数据回显
onMounted(async () => {
  await nextTick();
  if (localValue.value) {
    handleDataDisplay(localValue.value);
  }
});

// 监听配置变化
watch(
  () => props.config,
  (newConfig, oldConfig) => {
    // 只有在配置真正变化时才清空数据
    if (
      JSON.stringify(newConfig) !== JSON.stringify(oldConfig) && // 如果没有外部传入的值，才清空数据
      !localValue.value
    ) {
      paramsJson.value = '';
      jsonError.value = '';
    }
  },
);
</script>

<template>
  <!-- 参数配置 -->
  <div class="w-full space-y-3">
    <!-- JSON 输入框 -->
    <div class="relative">
      <Input.TextArea
        v-model="paramsJson"
        type="text"
        :rows="4"
        :placeholder="placeholder"
        @input="handleParamsChange"
        :class="{ 'is-error': jsonError }"
      />
      <!-- 查看详细示例弹出层 -->
      <div class="absolute right-2 top-2">
        <Popover
          placement="leftTop"
          :width="450"
          trigger="click"
          :show-arrow="true"
          :offset="8"
          popper-class="json-params-detail-popover"
        >
          <template #reference>
            <Button
              text
              type="primary"
              circle
              size="small"
              :title="JSON_PARAMS_INPUT_CONSTANTS.VIEW_EXAMPLE_TITLE"
            >
              <IconifyIcon icon="ep:info-filled" />
            </Button>
          </template>

          <!-- 弹出层内容 -->
          <div class="json-params-detail-content">
            <div class="mb-4 flex items-center gap-2">
              <IconifyIcon :icon="titleIcon" class="text-lg text-primary" />
              <span class="text-base font-bold text-primary">
                {{ title }}
              </span>
            </div>

            <div class="space-y-4">
              <!-- 参数列表 -->
              <div v-if="paramsList.length > 0">
                <div class="mb-2 flex items-center gap-2">
                  <IconifyIcon
                    :icon="paramsIcon"
                    class="text-base text-primary"
                  />
                  <span class="text-base font-bold text-primary">
                    {{ paramsLabel }}
                  </span>
                </div>
                <div class="ml-6 space-y-2">
                  <div
                    v-for="param in paramsList"
                    :key="param.identifier"
                    class="flex items-center justify-between rounded-lg bg-card p-2"
                  >
                    <div class="flex-1">
                      <div class="text-base font-bold text-primary">
                        {{ param.name }}
                        <Tag
                          v-if="param.required"
                          size="small"
                          type="danger"
                          class="ml-1"
                        >
                          {{ JSON_PARAMS_INPUT_CONSTANTS.REQUIRED_TAG }}
                        </Tag>
                      </div>
                      <div class="text-xs text-secondary">
                        {{ param.identifier }}
                      </div>
                    </div>
                    <div class="flex items-center gap-2">
                      <Tag :type="getParamTypeTag(param.dataType)" size="small">
                        {{ getParamTypeName(param.dataType) }}
                      </Tag>
                      <span class="text-xs text-secondary">
                        {{ getExampleValue(param) }}
                      </span>
                    </div>
                  </div>
                </div>

                <div class="ml-6 mt-3">
                  <div class="mb-1 text-xs text-secondary">
                    {{ JSON_PARAMS_INPUT_CONSTANTS.COMPLETE_JSON_FORMAT }}
                  </div>
                  <pre
                    class="border-l-3px overflow-x-auto rounded-lg border-primary bg-card p-3 text-sm text-primary"
                  >
                      <code>{{ generateExampleJson() }}</code>
                    </pre>
                </div>
              </div>

              <!-- 无参数提示 -->
              <div v-else>
                <div class="py-4 text-center">
                  <p class="text-sm text-secondary">
                    {{ emptyMessage }}
                  </p>
                </div>
              </div>
            </div>
          </div>
        </Popover>
      </div>
    </div>

    <!-- 验证状态和错误提示 -->
    <div class="flex items-center justify-between">
      <div class="flex items-center gap-2">
        <IconifyIcon
          :icon="
            jsonError
              ? JSON_PARAMS_INPUT_ICONS.STATUS_ICONS.ERROR
              : JSON_PARAMS_INPUT_ICONS.STATUS_ICONS.SUCCESS
          "
          :class="jsonError ? 'text-danger' : 'text-success'"
          class="text-sm"
        />
        <span
          :class="jsonError ? 'text-danger' : 'text-success'"
          class="text-xs"
        >
          {{ jsonError || JSON_PARAMS_INPUT_CONSTANTS.JSON_FORMAT_CORRECT }}
        </span>
      </div>

      <!-- 快速填充按钮 -->
      <div v-if="paramsList.length > 0" class="flex items-center gap-2">
        <span class="text-xs text-secondary">
          {{ JSON_PARAMS_INPUT_CONSTANTS.QUICK_FILL_LABEL }}
        </span>
        <Button size="small" type="primary" plain @click="fillExampleJson">
          {{ JSON_PARAMS_INPUT_CONSTANTS.EXAMPLE_DATA_BUTTON }}
        </Button>
        <Button size="small" danger type="primary" @click="clearParams">
          {{ JSON_PARAMS_INPUT_CONSTANTS.CLEAR_BUTTON }}
        </Button>
      </div>
    </div>
  </div>
</template>

<style scoped>
/* 弹出层内容样式 */
.json-params-detail-content {
  padding: 4px 0;
}

/* 弹出层自定义样式 */
:global(.json-params-detail-popover) {
  max-width: 500px !important;
}

:global(.json-params-detail-popover .ant-popover__content) {
  padding: 16px !important;
}

/* JSON 代码块样式 */
.json-params-detail-content pre {
  max-height: 200px;
  overflow-y: auto;
}
</style>
