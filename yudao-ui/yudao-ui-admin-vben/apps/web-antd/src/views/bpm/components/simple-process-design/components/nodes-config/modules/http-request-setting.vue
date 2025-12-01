<script setup lang="ts">
import { toRefs, watch } from 'vue';

import { IconifyIcon } from '@vben/icons';

import {
  Alert,
  Button,
  Col,
  FormItem,
  Input,
  Row,
  Select,
  SelectOption,
} from 'ant-design-vue';

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
  <FormItem>
    <Alert
      message="仅支持 POST 请求，以请求体方式接收参数"
      type="warning"
      show-icon
      :closable="false"
    />
  </FormItem>
  <!-- 请求地址-->
  <FormItem
    label="请求地址"
    :label-col="{ span: 24 }"
    :wrapper-col="{ span: 24 }"
    :name="[formItemPrefix, 'url']"
    :rules="{
      required: true,
      message: '请求地址不能为空',
      trigger: ['blur', 'change'],
    }"
  >
    <Input v-model:value="setting.url" placeholder="请输入请求地址" />
  </FormItem>
  <!-- 请求头，请求体设置-->
  <HttpRequestParamSetting
    :header="setting.header"
    :body="setting.body"
    :bind="formItemPrefix"
  />
  <!-- 返回值设置-->
  <div v-if="responseEnable">
    <FormItem
      label="返回值"
      :label-col="{ span: 24 }"
      :wrapper-col="{ span: 24 }"
    >
      <Alert
        message="通过请求返回值, 可以修改流程表单的值"
        type="warning"
        show-icon
        :closable="false"
      />
    </FormItem>
    <FormItem :wrapper-col="{ span: 24 }">
      <Row
        :gutter="8"
        v-for="(item, index) in setting.response"
        :key="index"
        class="mb-2"
      >
        <Col :span="10">
          <FormItem
            :name="[formItemPrefix, 'response', index, 'key']"
            :rules="{
              required: true,
              message: '表单字段不能为空',
              trigger: ['blur', 'change'],
            }"
          >
            <Select
              v-model:value="item.key"
              placeholder="请选择表单字段"
              allow-clear
            >
              <SelectOption
                v-for="(field, fIdx) in formFields"
                :key="fIdx"
                :label="field.title"
                :value="field.field"
                :disabled="!field.required"
              >
                {{ field.title }}
              </SelectOption>
            </Select>
          </FormItem>
        </Col>
        <Col :span="12">
          <FormItem
            :name="[formItemPrefix, 'response', index, 'value']"
            :rules="{
              required: true,
              message: '请求返回字段不能为空',
              trigger: ['blur', 'change'],
            }"
          >
            <Input v-model:value="item.value" placeholder="请求返回字段" />
          </FormItem>
        </Col>
        <Col :span="2">
          <div class="flex h-8 items-center">
            <IconifyIcon
              class="size-4 cursor-pointer text-red-500"
              icon="lucide:trash-2"
              @click="deleteHttpResponseSetting(setting.response!, index)"
            />
          </div>
        </Col>
      </Row>
      <Button
        type="link"
        @click="addHttpResponseSetting(setting.response!)"
        class="flex items-center"
      >
        <template #icon>
          <IconifyIcon class="size-4" icon="lucide:plus" />
        </template>
        添加一行
      </Button>
    </FormItem>
  </div>
</template>
