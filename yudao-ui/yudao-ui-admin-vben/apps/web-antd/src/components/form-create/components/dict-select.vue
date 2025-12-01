<!-- 数据字典 Select 选择器 -->
<script lang="ts" setup>
import type { DictSelectProps } from '../typing';

import { computed, useAttrs } from 'vue';

import { getDictOptions } from '@vben/hooks';

import {
  Checkbox,
  CheckboxGroup,
  Radio,
  RadioGroup,
  Select,
  SelectOption,
} from 'ant-design-vue';

defineOptions({ name: 'DictSelect' });

const props = withDefaults(defineProps<DictSelectProps>(), {
  valueType: 'str',
  selectType: 'select',
});

const attrs = useAttrs();

/** 获得字典配置 */
const getDictOption = computed(() => {
  switch (props.valueType) {
    case 'bool': {
      return getDictOptions(props.dictType, 'boolean');
    }
    case 'int': {
      return getDictOptions(props.dictType, 'number');
    }
    case 'str': {
      return getDictOptions(props.dictType, 'string');
    }
    default: {
      return [];
    }
  }
});
</script>

<template>
  <Select v-if="selectType === 'select'" class="w-full" v-bind="attrs">
    <SelectOption
      v-for="(dict, index) in getDictOption"
      :key="index"
      :value="dict.value"
    >
      {{ dict.label }}
    </SelectOption>
  </Select>
  <RadioGroup v-if="selectType === 'radio'" class="w-full" v-bind="attrs">
    <Radio
      v-for="(dict, index) in getDictOption"
      :key="index"
      :value="dict.value"
    >
      {{ dict.label }}
    </Radio>
  </RadioGroup>
  <CheckboxGroup v-if="selectType === 'checkbox'" class="w-full" v-bind="attrs">
    <Checkbox
      v-for="(dict, index) in getDictOption"
      :key="index"
      :value="dict.value"
    >
      {{ dict.label }}
    </Checkbox>
  </CheckboxGroup>
</template>
