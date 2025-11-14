<script lang="ts" setup>
import { onMounted } from 'vue';

import { isEmpty } from '@vben/utils';

import { useVModel } from '@vueuse/core';
import { FormItem, Input, InputNumber } from 'ant-design-vue';

defineOptions({ name: 'RedisStreamConfigForm' });

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
    url: '',
    password: '',
    database: 0,
    streamKey: '',
  };
});
</script>

<template>
  <div class="space-y-4">
    <FormItem label="服务地址" required>
      <Input
        v-model:value="config.url"
        placeholder="请输入Redis服务地址，如：redis://127.0.0.1:6379"
      />
    </FormItem>
    <FormItem label="密码">
      <Input.Password
        v-model:value="config.password"
        placeholder="请输入密码"
      />
    </FormItem>
    <FormItem label="数据库索引" required>
      <InputNumber
        v-model:value="config.database"
        :min="0"
        :max="15"
        placeholder="请输入数据库索引"
        class="w-full"
      />
    </FormItem>
    <FormItem label="Stream Key" required>
      <Input v-model:value="config.streamKey" placeholder="请输入Stream Key" />
    </FormItem>
  </div>
</template>
