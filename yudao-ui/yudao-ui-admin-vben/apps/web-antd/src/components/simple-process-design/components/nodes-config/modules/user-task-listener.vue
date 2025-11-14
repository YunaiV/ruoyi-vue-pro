<script setup lang="ts">
import { computed, ref } from 'vue';

import {
  Alert,
  Divider,
  Form,
  FormItem,
  Input,
  Switch,
  TypographyText,
} from 'ant-design-vue';

import HttpRequestParamSetting from './http-request-param-setting.vue';

const props = defineProps({
  modelValue: {
    type: Object,
    required: true,
  },
  formFieldOptions: {
    type: Object,
    required: true,
  },
});

const emit = defineEmits(['update:modelValue']);

const listenerFormRef = ref();

const configForm = computed({
  get() {
    return props.modelValue;
  },
  set(newValue) {
    emit('update:modelValue', newValue);
  },
});

const taskListener = ref([
  {
    name: '创建任务',
    type: 'Create',
  },
  {
    name: '指派任务执行人员',
    type: 'Assign',
  },
  {
    name: '完成任务',
    type: 'Complete',
  },
]);

async function validate() {
  if (!listenerFormRef.value) return false;
  return await listenerFormRef.value.validate();
}

defineExpose({ validate });
</script>
<template>
  <Form ref="listenerFormRef" :model="configForm" :label-col="{ span: 24 }">
    <div
      v-for="(listener, listenerIdx) in taskListener"
      :key="listenerIdx"
      class="pl-2"
    >
      <Divider orientation="left">
        <TypographyText tag="b" size="large">
          {{ listener.name }}
        </TypographyText>
      </Divider>
      <FormItem>
        <Switch
          v-model:checked="configForm[`task${listener.type}ListenerEnable`]"
          checked-children="开启"
          un-checked-children="关闭"
        />
      </FormItem>
      <div v-if="configForm[`task${listener.type}ListenerEnable`]">
        <FormItem>
          <Alert
            message="仅支持 POST 请求，以请求体方式接收参数"
            type="warning"
            show-icon
            :closable="false"
          />
        </FormItem>
        <FormItem
          label="请求地址"
          :name="`task${listener.type}ListenerPath`"
          :rules="{
            required: true,
            message: '请求地址不能为空',
            trigger: ['blur', 'change'],
          }"
        >
          <Input
            v-model:value="configForm[`task${listener.type}ListenerPath`]"
          />
        </FormItem>
        <HttpRequestParamSetting
          :header="configForm[`task${listener.type}Listener`].header"
          :body="configForm[`task${listener.type}Listener`].body"
          :bind="`task${listener.type}Listener`"
        />
      </div>
    </div>
  </Form>
</template>
