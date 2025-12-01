<!-- dataType：number 数组类型 -->
<script lang="ts" setup>
import type { Ref } from 'vue';

import type { DataSpecsNumberData } from '#/api/iot/thingmodel';

import { DICT_TYPE } from '@vben/constants';
import { getDictOptions } from '@vben/hooks';

import { useVModel } from '@vueuse/core';
import { Form, Input, Select } from 'ant-design-vue';

/** 数值型的 dataSpecs 配置组件 */
defineOptions({ name: 'ThingModelNumberDataSpecs' });

const props = defineProps<{ modelValue: any }>();
const emits = defineEmits(['update:modelValue']);
const dataSpecs = useVModel(
  props,
  'modelValue',
  emits,
) as Ref<DataSpecsNumberData>;

/** 单位发生变化时触发 */
const unitChange = (UnitSpecs: any) => {
  if (!UnitSpecs) return;
  const [unitName, unit] = String(UnitSpecs).split('-');
  dataSpecs.value.unitName = unitName;
  dataSpecs.value.unit = unit;
};
</script>

<template>
  <Form.Item label="取值范围">
    <div class="flex items-center justify-between">
      <div class="flex-1">
        <Input v-model:value="dataSpecs.min" placeholder="请输入最小值" />
      </div>
      <span class="mx-2">~</span>
      <div class="flex-1">
        <Input v-model:value="dataSpecs.max" placeholder="请输入最大值" />
      </div>
    </div>
  </Form.Item>
  <Form.Item label="步长">
    <Input v-model:value="dataSpecs.step" placeholder="请输入步长" />
  </Form.Item>
  <Form.Item label="单位">
    <Select
      :model-value="
        dataSpecs.unit ? `${dataSpecs.unitName}-${dataSpecs.unit}` : ''
      "
      show-search
      placeholder="请选择单位"
      class="w-1/1"
      @change="unitChange"
    >
      <Select.Option
        v-for="(item, index) in getDictOptions(
          DICT_TYPE.IOT_THING_MODEL_UNIT,
          'string',
        )"
        :key="index"
        :value="`${item.label}-${item.value}`"
      >
        {{ `${item.label}-${item.value}` }}
      </Select.Option>
    </Select>
  </Form.Item>
</template>

<style lang="scss" scoped>
:deep(.ant-form-item) {
  .ant-form-item {
    margin-bottom: 0;
  }
}
</style>
