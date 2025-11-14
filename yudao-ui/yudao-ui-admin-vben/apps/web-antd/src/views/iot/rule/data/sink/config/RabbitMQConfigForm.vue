<script lang="ts" setup>
import { onMounted } from 'vue';

import { isEmpty } from '@vben/utils';

import { useVModel } from '@vueuse/core';
import { FormItem, Input, InputNumber } from 'ant-design-vue';

defineOptions({ name: 'RabbitMQConfigForm' });

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
    host: '',
    port: 5672,
    username: '',
    password: '',
    virtualHost: '/',
    exchange: '',
    routingKey: '',
    queue: '',
  };
});
</script>

<template>
  <div class="space-y-4">
    <FormItem label="主机地址" required>
      <Input
        v-model:value="config.host"
        placeholder="请输入主机地址，如：localhost"
      />
    </FormItem>
    <FormItem label="端口" required>
      <InputNumber
        v-model:value="config.port"
        :min="1"
        :max="65535"
        placeholder="请输入端口，如：5672"
        class="w-full"
      />
    </FormItem>
    <FormItem label="用户名" required>
      <Input v-model:value="config.username" placeholder="请输入用户名" />
    </FormItem>
    <FormItem label="密码" required>
      <Input.Password
        v-model:value="config.password"
        placeholder="请输入密码"
      />
    </FormItem>
    <FormItem label="虚拟主机" required>
      <Input
        v-model:value="config.virtualHost"
        placeholder="请输入虚拟主机，如：/"
      />
    </FormItem>
    <FormItem label="交换机" required>
      <Input v-model:value="config.exchange" placeholder="请输入交换机名称" />
    </FormItem>
    <FormItem label="路由键" required>
      <Input v-model:value="config.routingKey" placeholder="请输入路由键" />
    </FormItem>
    <FormItem label="队列" required>
      <Input v-model:value="config.queue" placeholder="请输入队列名称" />
    </FormItem>
  </div>
</template>
