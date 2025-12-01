<script lang="ts" setup>
import { onMounted } from 'vue';

import { isEmpty } from '@vben/utils';

import { useVModel } from '@vueuse/core';
import { FormItem, Input } from 'ant-design-vue';

defineOptions({ name: 'RocketMQConfigForm' });

const props = defineProps<{
  modelValue: any;
}>();
const emit = defineEmits(['update:modelValue']);
const config = useVModel(props, 'modelValue', emit) as any;

/** 组件初始化 */
onMounted(() => {
  if (!isEmpty(config.value)) {
    return;
  }
  config.value = {
    nameServer: '',
    accessKey: '',
    secretKey: '',
    group: '',
    topic: '',
    tags: '',
  };
});
</script>

<template>
  <div class="space-y-4">
    <FormItem label="NameServer" required>
      <Input
        v-model:value="config.nameServer"
        placeholder="请输入 NameServer 地址，如：127.0.0.1:9876"
      />
    </FormItem>
    <FormItem label="AccessKey" required>
      <Input v-model:value="config.accessKey" placeholder="请输入 AccessKey" />
    </FormItem>
    <FormItem label="SecretKey" required>
      <Input.Password
        v-model:value="config.secretKey"
        placeholder="请输入 SecretKey"
      />
    </FormItem>
    <FormItem label="消费组" required>
      <Input v-model:value="config.group" placeholder="请输入消费组" />
    </FormItem>
    <FormItem label="主题" required>
      <Input v-model:value="config.topic" placeholder="请输入主题" />
    </FormItem>
    <FormItem label="标签">
      <Input v-model:value="config.tags" placeholder="请输入标签" />
    </FormItem>
  </div>
</template>
