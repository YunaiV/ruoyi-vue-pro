<script lang="ts" setup>
import { computed, onMounted, ref, watch } from 'vue';

import { isEmpty } from '@vben/utils';

import { useVModel } from '@vueuse/core';
import { FormItem, Input, Select } from 'ant-design-vue';

import KeyValueEditor from './components/key-value-editor.vue';

defineOptions({ name: 'HttpConfigForm' });

const props = defineProps<{
  modelValue: any;
}>();
const emit = defineEmits(['update:modelValue']);
const config = useVModel(props, 'modelValue', emit) as any;

// noinspection HttpUrlsUsage
/** URL处理 */
const urlPrefix = ref('http://');
const urlPath = ref('');
const fullUrl = computed(() => {
  return urlPath.value ? urlPrefix.value + urlPath.value : '';
});

/** 监听 URL 变化 */
watch([urlPrefix, urlPath], () => {
  config.value.url = fullUrl.value;
});

/** 组件初始化 */
onMounted(() => {
  if (!isEmpty(config.value)) {
    // 初始化 URL
    if (config.value.url) {
      if (config.value.url.startsWith('https://')) {
        urlPrefix.value = 'https://';
        urlPath.value = config.value.url.slice(8);
      } else if (config.value.url.startsWith('http://')) {
        urlPrefix.value = 'http://';
        urlPath.value = config.value.url.slice(7);
      } else {
        urlPath.value = config.value.url;
      }
    }
    return;
  }

  config.value = {
    url: '',
    method: 'POST',
    headers: {},
    query: {},
    body: '',
  };
});

const methodOptions = [
  { label: 'GET', value: 'GET' },
  { label: 'POST', value: 'POST' },
  { label: 'PUT', value: 'PUT' },
  { label: 'DELETE', value: 'DELETE' },
];
</script>

<template>
  <div class="space-y-4">
    <FormItem label="请求地址" required>
      <Input v-model:value="urlPath" placeholder="请输入请求地址">
        <template #addonBefore>
          <Select
            v-model:value="urlPrefix"
            placeholder="Select"
            style="width: 115px"
            :options="[
              { label: 'http://', value: 'http://' },
              { label: 'https://', value: 'https://' },
            ]"
          />
        </template>
      </Input>
    </FormItem>
    <FormItem label="请求方法" required>
      <Select
        v-model:value="config.method"
        placeholder="请选择请求方法"
        :options="methodOptions"
      />
    </FormItem>
    <FormItem label="请求头">
      <KeyValueEditor v-model="config.headers" add-button-text="添加请求头" />
    </FormItem>
    <FormItem label="请求参数">
      <KeyValueEditor v-model="config.query" add-button-text="添加参数" />
    </FormItem>
    <FormItem label="请求体">
      <Input.TextArea
        v-model:value="config.body"
        placeholder="请输入内容"
        :rows="3"
      />
    </FormItem>
  </div>
</template>
