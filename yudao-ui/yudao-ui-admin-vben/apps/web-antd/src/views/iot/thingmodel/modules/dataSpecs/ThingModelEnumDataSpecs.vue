<!-- dataType：enum 数组类型 -->
<script lang="ts" setup>
import type { Ref } from 'vue';

import type { DataSpecsEnumOrBoolData } from '#/api/iot/thingmodel';

import { isEmpty } from '@vben/utils';

import { useVModel } from '@vueuse/core';
import { Button, Form, Input, message } from 'ant-design-vue';

import { IoTDataSpecsDataTypeEnum } from '#/views/iot/utils/constants';

/** 枚举型的 dataSpecs 配置组件 */
defineOptions({ name: 'ThingModelEnumDataSpecs' });

const props = defineProps<{ modelValue: any }>();
const emits = defineEmits(['update:modelValue']);
const dataSpecsList = useVModel(props, 'modelValue', emits) as Ref<
  DataSpecsEnumOrBoolData[]
>;

/** 添加枚举项 */
function addEnum() {
  dataSpecsList.value.push({
    dataType: IoTDataSpecsDataTypeEnum.ENUM,
    name: '', // 枚举项的名称
    value: '', // 枚举值
  });
}

/** 删除枚举项 */
function deleteEnum(index: number) {
  if (dataSpecsList.value.length === 1) {
    message.warning('至少需要一个枚举项');
    return;
  }
  dataSpecsList.value.splice(index, 1);
}

/** 校验枚举值 */
function validateEnumValue(_: any, value: any, callback: any) {
  if (isEmpty(value)) {
    callback(new Error('枚举值不能为空'));
    return;
  }
  if (Number.isNaN(Number(value))) {
    callback(new Error('枚举值必须是数字'));
    return;
  }
  // 检查枚举值是否重复
  const values = dataSpecsList.value.map((item) => item.value);
  if (values.filter((v) => v === value).length > 1) {
    callback(new Error('枚举值不能重复'));
    return;
  }
  callback();
}

/** 校验枚举描述 */
function validateEnumName(_: any, value: string, callback: any) {
  if (isEmpty(value)) {
    callback(new Error('枚举描述不能为空'));
    return;
  }
  // 检查开头字符
  if (!/^[\u4E00-\u9FA5a-z0-9]/i.test(value)) {
    callback(new Error('枚举描述必须以中文、英文字母或数字开头'));
    return;
  }
  // 检查整体格式
  if (!/^[\u4E00-\u9FA5a-z0-9][\w\u4E00-\u9FA5-]*$/i.test(value)) {
    callback(new Error('枚举描述只能包含中文、英文字母、数字、下划线和短划线'));
    return;
  }
  // 检查长度（一个中文算一个字符）
  if (value.length > 20) {
    callback(new Error('枚举描述长度不能超过20个字符'));
    return;
  }
  callback();
}

/** 校验整个枚举列表 */
function validateEnumList(_: any, __: any, callback: any) {
  if (isEmpty(dataSpecsList.value)) {
    callback(new Error('请至少添加一个枚举项'));
    return;
  }

  // 检查是否存在空值
  const hasEmptyValue = dataSpecsList.value.some(
    (item) => isEmpty(item.value) || isEmpty(item.name),
  );
  if (hasEmptyValue) {
    callback(new Error('存在未填写的枚举值或描述'));
    return;
  }

  // 检查枚举值是否都是数字
  const hasInvalidNumber = dataSpecsList.value.some((item) =>
    Number.isNaN(Number(item.value)),
  );
  if (hasInvalidNumber) {
    callback(new Error('存在非数字的枚举值'));
    return;
  }

  // 检查是否有重复的枚举值
  const values = dataSpecsList.value.map((item) => item.value);
  const uniqueValues = new Set(values);
  if (values.length !== uniqueValues.size) {
    callback(new Error('存在重复的枚举值'));
    return;
  }
  callback();
}
</script>

<template>
  <Form.Item
    :rules="[
      { required: true, validator: validateEnumList, trigger: 'change' },
    ]"
    label="枚举项"
  >
    <div class="flex flex-col">
      <div class="flex items-center">
        <span class="flex-1"> 参数值 </span>
        <span class="flex-1"> 参数描述 </span>
      </div>
      <div
        v-for="(item, index) in dataSpecsList"
        :key="index"
        class="mb-5px flex items-center justify-between"
      >
        <Form.Item
          :prop="`property.dataSpecsList[${index}].value`"
          :rules="[
            { required: true, message: '枚举值不能为空' },
            { validator: validateEnumValue, trigger: 'blur' },
          ]"
          class="mb-0 flex-1"
        >
          <Input v-model="item.value" placeholder="请输入枚举值,如'0'" />
        </Form.Item>
        <span class="mx-2">~</span>
        <Form.Item
          :prop="`property.dataSpecsList[${index}].name`"
          :rules="[
            { required: true, message: '枚举描述不能为空' },
            { validator: validateEnumName, trigger: 'blur' },
          ]"
          class="mb-0 flex-1"
        >
          <Input v-model="item.name" placeholder="对该枚举项的描述" />
        </Form.Item>
        <Button class="ml-10px" link type="primary" @click="deleteEnum(index)">
          删除
        </Button>
      </div>
      <Button link type="primary" @click="addEnum">+添加枚举项</Button>
    </div>
  </Form.Item>
</template>

<style lang="scss" scoped>
:deep(.el-form-item) {
  .el-form-item {
    margin-bottom: 0;
  }
}
</style>
