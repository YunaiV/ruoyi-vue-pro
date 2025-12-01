<!-- dataType：enum 数组类型 -->
<script lang="ts" setup>
import type { Ref } from 'vue';

import { useVModel } from '@vueuse/core';
import { Button, Form, Input, message } from 'ant-design-vue';

/** 枚举型的 dataSpecs 配置组件 */
defineOptions({ name: 'ThingModelEnumDataSpecs' });

const props = defineProps<{ modelValue: any }>();
const emits = defineEmits(['update:modelValue']);
const dataSpecsList = useVModel(props, 'modelValue', emits) as Ref<any[]>;

/** 添加枚举项 */
function addEnum() {
  dataSpecsList.value.push({
    name: '', // 枚举项的名称
    value: '', // 枚举值
  } as any);
}

/** 删除枚举项 */
function deleteEnum(index: number) {
  if (dataSpecsList.value.length === 1) {
    message.warning('至少需要一个枚举项');
    return;
  }
  dataSpecsList.value.splice(index, 1);
}
</script>

<template>
  <Form.Item label="枚举项">
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
        <div class="flex-1">
          <Input v-model:value="item.value" placeholder="请输入枚举值,如'0'" />
        </div>
        <span class="mx-2">~</span>
        <div class="flex-1">
          <Input v-model:value="item.name" placeholder="对该枚举项的描述" />
        </div>
        <Button class="ml-10px" type="link" @click="deleteEnum(index)">
          删除
        </Button>
      </div>
      <Button type="link" @click="addEnum">+添加枚举项</Button>
    </div>
  </Form.Item>
</template>

<style lang="scss" scoped>
:deep(.ant-form-item) {
  .ant-form-item {
    margin-bottom: 0;
  }
}
</style>
