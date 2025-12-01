<!-- dataType：array 数组类型 -->
<script lang="ts" setup>
import type { Ref } from 'vue';

import { useVModel } from '@vueuse/core';
import { Form, Input, Radio } from 'ant-design-vue';

import {
  getDataTypeOptions,
  IoTDataSpecsDataTypeEnum,
} from '#/views/iot/utils/constants';

import ThingModelStructDataSpecs from './thing-model-struct-data-specs.vue';

/** 数组型的 dataSpecs 配置组件 */
defineOptions({ name: 'ThingModelArrayDataSpecs' });

const props = defineProps<{ modelValue: any }>();
const emits = defineEmits(['update:modelValue']);
const dataSpecs = useVModel(props, 'modelValue', emits) as Ref<any>;

/** 元素类型改变时间。当值为 struct 时，对 dataSpecs 中的 dataSpecsList 进行初始化 */
function handleChange(val: any) {
  if (val !== IoTDataSpecsDataTypeEnum.STRUCT) {
    return;
  }
  dataSpecs.value.dataSpecsList = [];
}
</script>

<template>
  <Form.Item label="元素类型" name="property.dataSpecs.childDataType">
    <Radio.Group v-model:value="dataSpecs.childDataType" @change="handleChange">
      <template v-for="item in getDataTypeOptions()" :key="item.value">
        <Radio
          v-if="
            !(
              [
                IoTDataSpecsDataTypeEnum.ENUM,
                IoTDataSpecsDataTypeEnum.ARRAY,
                IoTDataSpecsDataTypeEnum.DATE,
              ] as any[]
            ).includes(item.value)
          "
          :value="item.value"
          class="w-1/3"
        >
          {{ `${item.value}(${item.label})` }}
        </Radio>
      </template>
    </Radio.Group>
  </Form.Item>
  <Form.Item label="元素个数" name="property.dataSpecs.size">
    <Input
      v-model:value="dataSpecs.size"
      placeholder="请输入数组中的元素个数"
    />
  </Form.Item>
  <!-- Struct 型配置-->
  <ThingModelStructDataSpecs
    v-if="dataSpecs.childDataType === IoTDataSpecsDataTypeEnum.STRUCT"
    v-model="dataSpecs.dataSpecsList"
  />
</template>
