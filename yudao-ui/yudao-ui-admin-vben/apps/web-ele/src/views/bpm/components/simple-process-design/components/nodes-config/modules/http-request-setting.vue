<script setup lang="ts">
import { toRefs, watch } from 'vue';

import { IconifyIcon } from '@vben/icons';

import {
  ElAlert,
  ElButton,
  ElCol,
  ElFormItem,
  ElInput,
  ElOption,
  ElRow,
  ElSelect,
} from 'element-plus';

import { useFormFields } from '../../../helpers';
import HttpRequestParamSetting from './http-request-param-setting.vue';

defineOptions({ name: 'HttpRequestSetting' });

const props = defineProps({
  setting: {
    type: Object,
    required: true,
  },
  responseEnable: {
    type: Boolean,
    required: true,
  },
  formItemPrefix: {
    type: String,
    required: true,
  },
});

const emits = defineEmits(['update:setting']);

const { setting } = toRefs(props);

watch(
  () => setting,
  (val) => {
    emits('update:setting', val);
  },
);

/** 流程表单字段 */
const formFields = useFormFields();

/** 添加 HTTP 请求返回值设置项 */
function addHttpResponseSetting(responseSetting: Record<string, string>[]) {
  responseSetting.push({
    key: '',
    value: '',
  });
}

/** 删除 HTTP 请求返回值设置项 */
function deleteHttpResponseSetting(
  responseSetting: Record<string, string>[],
  index: number,
) {
  responseSetting.splice(index, 1);
}
</script>
<template>
  <ElFormItem>
    <ElAlert
      title="仅支持 POST 请求，以请求体方式接收参数"
      type="warning"
      show-icon
      :closable="false"
    />
  </ElFormItem>
  <!-- 请求地址-->
  <ElFormItem
    label="请求地址"
    :prop="`${formItemPrefix}.url`"
    :rules="{
      required: true,
      message: '请求地址不能为空',
      trigger: ['blur', 'change'],
    }"
  >
    <ElInput v-model="setting.url" placeholder="请输入请求地址" />
  </ElFormItem>
  <!-- 请求头，请求体设置-->
  <HttpRequestParamSetting
    :header="setting.header"
    :body="setting.body"
    :bind="formItemPrefix"
  />
  <!-- 返回值设置-->
  <div v-if="responseEnable">
    <ElFormItem label="返回值">
      <ElAlert
        title="通过请求返回值, 可以修改流程表单的值"
        type="warning"
        show-icon
        :closable="false"
      />
    </ElFormItem>
    <ElRow
      :gutter="8"
      v-for="(item, index) in setting.response"
      :key="index"
      class="mb-2"
      align="middle"
    >
      <ElCol :span="10">
        <ElFormItem
          class="!mb-0"
          :prop="`${formItemPrefix}.response.${index}.key`"
          :rules="{
            required: true,
            message: '表单字段不能为空',
            trigger: ['blur', 'change'],
          }"
        >
          <ElSelect v-model="item.key" placeholder="请选择表单字段" clearable>
            <ElOption
              v-for="(field, fIdx) in formFields"
              :key="fIdx"
              :label="field.title"
              :value="field.field"
              :disabled="!field.required"
            />
          </ElSelect>
        </ElFormItem>
      </ElCol>
      <ElCol :span="12">
        <ElFormItem
          class="!mb-0"
          :prop="`${formItemPrefix}.response.${index}.value`"
          :rules="{
            required: true,
            message: '请求返回字段不能为空',
            trigger: ['blur', 'change'],
          }"
        >
          <ElInput v-model="item.value" placeholder="请求返回字段" />
        </ElFormItem>
      </ElCol>
      <ElCol :span="2">
        <ElFormItem class="!mb-0">
          <div class="flex h-8 items-center">
            <IconifyIcon
              class="size-4 cursor-pointer text-red-500"
              icon="lucide:trash-2"
              @click="deleteHttpResponseSetting(setting.response!, index)"
            />
          </div>
        </ElFormItem>
      </ElCol>
    </ElRow>
    <div>
      <ElButton link @click="addHttpResponseSetting(setting.response!)">
        <template #icon>
          <IconifyIcon class="size-4" icon="lucide:plus" />
        </template>
        添加一行
      </ElButton>
    </div>
  </div>
</template>
