<script setup lang="ts">
import { computed, ref } from 'vue';

import {
  ElAlert,
  ElDivider,
  ElForm,
  ElFormItem,
  ElInput,
  ElSwitch,
  ElText,
} from 'element-plus';

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
  <ElForm ref="listenerFormRef" :model="configForm">
    <div
      v-for="(listener, listenerIdx) in taskListener"
      :key="listenerIdx"
      class="pl-2"
    >
      <ElDivider orientation="left">
        <ElText tag="b" size="large">
          {{ listener.name }}
        </ElText>
      </ElDivider>
      <ElFormItem>
        <ElSwitch
          v-model="configForm[`task${listener.type}ListenerEnable`]"
          active-text="开启"
          inactive-text="关闭"
        />
      </ElFormItem>
      <div v-if="configForm[`task${listener.type}ListenerEnable`]">
        <ElFormItem>
          <ElAlert
            title="仅支持 POST 请求，以请求体方式接收参数"
            type="warning"
            show-icon
            :closable="false"
          />
        </ElFormItem>
        <ElFormItem
          label="请求地址"
          :name="`task${listener.type}ListenerPath`"
          :rules="{
            required: true,
            message: '请求地址不能为空',
            trigger: ['blur', 'change'],
          }"
        >
          <ElInput v-model="configForm[`task${listener.type}ListenerPath`]" />
        </ElFormItem>
        <HttpRequestParamSetting
          :header="configForm[`task${listener.type}Listener`].header"
          :body="configForm[`task${listener.type}Listener`].body"
          :bind="`task${listener.type}Listener`"
        />
      </div>
    </div>
  </ElForm>
</template>
