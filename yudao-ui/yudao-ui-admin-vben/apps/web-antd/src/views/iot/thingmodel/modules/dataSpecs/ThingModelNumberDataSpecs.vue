<!-- dataType：number 数组类型 -->
<script lang="ts" setup>
import type { Ref } from 'vue';

import type { DataSpecsNumberData } from '#/api/iot/thingmodel';

import { DICT_TYPE } from '@vben/constants';
import { getDictOptions } from '@vben/hooks';

import { useVModel } from '@vueuse/core';

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
const unitChange = (UnitSpecs: string) => {
  const [unitName, unit] = UnitSpecs.split('-');
  dataSpecs.value.unitName = unitName;
  dataSpecs.value.unit = unit;
};

/** 校验最小值 */
const validateMin = (_: any, __: any, callback: any) => {
  const min = Number(dataSpecs.value.min);
  const max = Number(dataSpecs.value.max);
  if (Number.isNaN(min)) {
    callback(new Error('请输入有效的数值'));
    return;
  }
  if (max !== undefined && !Number.isNaN(max) && min >= max) {
    callback(new Error('最小值必须小于最大值'));
    return;
  }

  callback();
};

/** 校验最大值 */
const validateMax = (_: any, __: any, callback: any) => {
  const min = Number(dataSpecs.value.min);
  const max = Number(dataSpecs.value.max);
  if (Number.isNaN(max)) {
    callback(new Error('请输入有效的数值'));
    return;
  }
  if (min !== undefined && !Number.isNaN(min) && max <= min) {
    callback(new Error('最大值必须大于最小值'));
    return;
  }

  callback();
};

/** 校验步长 */
const validateStep = (_: any, __: any, callback: any) => {
  const step = Number(dataSpecs.value.step);
  if (Number.isNaN(step)) {
    callback(new Error('请输入有效的数值'));
    return;
  }
  if (step <= 0) {
    callback(new Error('步长必须大于0'));
    return;
  }
  const min = Number(dataSpecs.value.min);
  const max = Number(dataSpecs.value.max);
  if (!Number.isNaN(min) && !Number.isNaN(max) && step > max - min) {
    callback(new Error('步长不能大于最大值和最小值的差值'));
    return;
  }

  callback();
};
</script>

<template>
  <el-form-item label="取值范围">
    <div class="flex items-center justify-between">
      <el-form-item
        :rules="[
          { required: true, message: '最小值不能为空' },
          { validator: validateMin, trigger: 'blur' },
        ]"
        class="mb-0"
        prop="property.dataSpecs.min"
      >
        <el-input v-model="dataSpecs.min" placeholder="请输入最小值" />
      </el-form-item>
      <span class="mx-2">~</span>
      <el-form-item
        :rules="[
          { required: true, message: '最大值不能为空' },
          { validator: validateMax, trigger: 'blur' },
        ]"
        class="mb-0"
        prop="property.dataSpecs.max"
      >
        <el-input v-model="dataSpecs.max" placeholder="请输入最大值" />
      </el-form-item>
    </div>
  </el-form-item>
  <el-form-item
    :rules="[
      { required: true, message: '步长不能为空' },
      { validator: validateStep, trigger: 'blur' },
    ]"
    label="步长"
    prop="property.dataSpecs.step"
  >
    <el-input v-model="dataSpecs.step" placeholder="请输入步长" />
  </el-form-item>
  <el-form-item
    :rules="[{ required: true, message: '请选择单位' }]"
    label="单位"
    prop="property.dataSpecs.unit"
  >
    <el-select
      :model-value="
        dataSpecs.unit ? `${dataSpecs.unitName}-${dataSpecs.unit}` : ''
      "
      filterable
      placeholder="请选择单位"
      class="w-1/1"
      @change="unitChange"
    >
      <el-option
        v-for="(item, index) in getDictOptions(
          DICT_TYPE.IOT_THING_MODEL_UNIT,
          'string',
        )"
        :key="index"
        :label="`${item.label}-${item.value}`"
        :value="`${item.label}-${item.value}`"
      />
    </el-select>
  </el-form-item>
</template>

<style lang="scss" scoped>
:deep(.el-form-item) {
  .el-form-item {
    margin-bottom: 0;
  }
}
</style>
