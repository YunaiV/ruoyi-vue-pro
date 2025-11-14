<script lang="ts" setup>
import { onMounted } from 'vue';

import { isEmpty } from '@vben/utils';

import { useVModel } from '@vueuse/core';
import { FormItem, Input, Switch } from 'ant-design-vue';

defineOptions({ name: 'KafkaMQConfigForm' });

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
    bootstrapServers: '',
    username: '',
    password: '',
    ssl: false,
    topic: '',
  };
});
</script>

<template>
  <div class="space-y-4">
    <FormItem label="服务地址" required>
      <Input
        v-model:value="config.bootstrapServers"
        placeholder="请输入服务地址，如：localhost:9092"
      />
    </FormItem>
    <FormItem label="用户名">
      <Input v-model:value="config.username" placeholder="请输入用户名" />
    </FormItem>
    <FormItem label="密码">
      <Input.Password
        v-model:value="config.password"
        placeholder="请输入密码"
      />
    </FormItem>
    <FormItem label="启用 SSL" required>
      <Switch v-model:checked="config.ssl" />
    </FormItem>
    <FormItem label="主题" required>
      <Input v-model:value="config.topic" placeholder="请输入主题" />
    </FormItem>
  </div>
</template>
