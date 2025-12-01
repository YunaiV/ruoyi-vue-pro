<!-- 产品的物模型表单（property 项） -->
<script lang="ts" setup>
import type { Ref } from 'vue';

import type { ThingModelProperty } from '#/api/iot/thingmodel';

import { computed, watch } from 'vue';

import { isEmpty } from '@vben/utils';

import { useVModel } from '@vueuse/core';
import { Form, Input, Radio, Select } from 'ant-design-vue';

import {
  getDataTypeOptions,
  IoTDataSpecsDataTypeEnum,
  IoTThingModelAccessModeEnum,
} from '#/views/iot/utils/constants';

import {
  ThingModelArrayDataSpecs,
  ThingModelEnumDataSpecs,
  ThingModelNumberDataSpecs,
  ThingModelStructDataSpecs,
} from './dataSpecs';

/** IoT 物模型属性 */
defineOptions({ name: 'ThingModelProperty' });

const props = defineProps<{
  isParams?: boolean;
  isStructDataSpecs?: boolean;
  modelValue: any;
}>();
const emits = defineEmits(['update:modelValue']);
const property = useVModel(
  props,
  'modelValue',
  emits,
) as Ref<ThingModelProperty>;
const getDataTypeOptions2 = computed(() => {
  if (!props.isStructDataSpecs) {
    return getDataTypeOptions();
  }
  const excludedTypes = new Set([
    IoTDataSpecsDataTypeEnum.ARRAY,
    IoTDataSpecsDataTypeEnum.STRUCT,
  ]);
  return getDataTypeOptions().filter(
    (item: any) => !excludedTypes.has(item.value),
  );
}); // 获得数据类型列表

/** 属性值的数据类型切换时初始化相关数据 */
function handleChange(dataType: any) {
  property.value.dataSpecs = {};
  property.value.dataSpecsList = [];
  // 不是列表型数据才设置 dataSpecs.dataType
  ![
    IoTDataSpecsDataTypeEnum.BOOL,
    IoTDataSpecsDataTypeEnum.ENUM,
    IoTDataSpecsDataTypeEnum.STRUCT,
  ].includes(dataType) && (property.value.dataSpecs.dataType = dataType);
  switch (dataType) {
    case IoTDataSpecsDataTypeEnum.BOOL: {
      for (let i = 0; i < 2; i++) {
        property.value.dataSpecsList.push({
          dataType: IoTDataSpecsDataTypeEnum.BOOL,
          name: '', // 布尔值的名称
          value: i, // 布尔值
        });
      }
      break;
    }
    case IoTDataSpecsDataTypeEnum.ENUM: {
      property.value.dataSpecsList.push({
        dataType: IoTDataSpecsDataTypeEnum.ENUM,
        name: '', // 枚举项的名称
        value: undefined, // 枚举值
      });
      break;
    }
  }
  // useVModel 会自动同步数据到父组件，不需要手动 emit
}

/** 默认选中读写 */
watch(
  () => property.value.accessMode,
  (val: string | undefined) => {
    if (props.isStructDataSpecs || props.isParams) {
      return;
    }
    if (isEmpty(val)) {
      property.value.accessMode = IoTThingModelAccessModeEnum.READ_WRITE.value;
    }
  },
  { immediate: true },
);
</script>

<template>
  <Form.Item label="数据类型">
    <Select
      v-model:value="property.dataType"
      placeholder="请选择数据类型"
      @change="handleChange"
    >
      <!-- ARRAY 和 STRUCT 类型数据相互嵌套时，最多支持递归嵌套 2 层（父和子） -->
      <Select.Option
        v-for="option in getDataTypeOptions2"
        :key="option.value"
        :value="option.value"
      >
        {{ `${option.value}(${option.label})` }}
      </Select.Option>
    </Select>
  </Form.Item>
  <!-- 数值型配置 -->
  <ThingModelNumberDataSpecs
    v-if="
      [
        IoTDataSpecsDataTypeEnum.INT,
        IoTDataSpecsDataTypeEnum.DOUBLE,
        IoTDataSpecsDataTypeEnum.FLOAT,
      ].includes(property.dataType || '')
    "
    v-model="property.dataSpecs"
  />
  <!-- 枚举型配置 -->
  <ThingModelEnumDataSpecs
    v-if="property.dataType === IoTDataSpecsDataTypeEnum.ENUM"
    v-model="property.dataSpecsList"
  />
  <!-- 布尔型配置 -->
  <Form.Item
    v-if="property.dataType === IoTDataSpecsDataTypeEnum.BOOL"
    label="布尔值"
  >
    <template v-for="item in property.dataSpecsList" :key="item.value">
      <div class="w-1/1 mb-5px flex items-center justify-start">
        <span>{{ item.value }}</span>
        <span class="mx-2">-</span>
        <div class="flex-1">
          <Input
            v-model:value="item.name"
            :placeholder="`如：${item.value === 0 ? '关' : '开'}`"
            class="w-255px!"
          />
        </div>
      </div>
    </template>
  </Form.Item>
  <!-- 文本型配置 -->
  <Form.Item
    v-if="property.dataType === IoTDataSpecsDataTypeEnum.TEXT"
    label="数据长度"
    name="property.dataSpecs.length"
  >
    <Input
      v-model:value="property.dataSpecs.length"
      class="w-255px!"
      placeholder="请输入文本字节长度"
    >
      <template #addonAfter>字节</template>
    </Input>
  </Form.Item>
  <!-- 时间型配置 -->
  <Form.Item
    v-if="property.dataType === IoTDataSpecsDataTypeEnum.DATE"
    label="时间格式"
    name="date"
  >
    <Input
      class="w-255px!"
      disabled
      placeholder="String 类型的 UTC 时间戳（毫秒）"
    />
  </Form.Item>
  <!-- 数组型配置-->
  <ThingModelArrayDataSpecs
    v-if="property.dataType === IoTDataSpecsDataTypeEnum.ARRAY"
    v-model="property.dataSpecs"
  />
  <!-- Struct 型配置-->
  <ThingModelStructDataSpecs
    v-if="property.dataType === IoTDataSpecsDataTypeEnum.STRUCT"
    v-model="property.dataSpecsList"
  />
  <Form.Item
    v-if="!isStructDataSpecs && !isParams"
    label="读写类型"
    name="property.accessMode"
  >
    <Radio.Group v-model:value="property.accessMode">
      <Radio
        v-for="accessMode in Object.values(IoTThingModelAccessModeEnum)"
        :key="accessMode.value"
        :value="accessMode.value"
      >
        {{ accessMode.label }}
      </Radio>
    </Radio.Group>
  </Form.Item>
</template>

<style lang="scss" scoped>
:deep(.ant-form-item) {
  .ant-form-item {
    margin-bottom: 0;
  }
}
</style>
