<script setup lang="ts">
import type { HttpRequestParam } from '../../../consts';

import { IconifyIcon } from '@vben/icons';

import {
  Button,
  Col,
  FormItem,
  Input,
  Row,
  Select,
  SelectOption,
} from 'ant-design-vue';

import {
  BPM_HTTP_REQUEST_PARAM_TYPES,
  BpmHttpRequestParamTypeEnum,
} from '../../../consts';
import { useFormFieldsAndStartUser } from '../../../helpers';

defineOptions({ name: 'HttpRequestParamSetting' });

const props = defineProps({
  header: {
    type: Array as () => HttpRequestParam[],
    required: false,
    default: () => [],
  },
  body: {
    type: Array as () => HttpRequestParam[],
    required: false,
    default: () => [],
  },
  bind: {
    type: String,
    required: true,
  },
});

// 流程表单字段，发起人字段
const formFieldOptions = useFormFieldsAndStartUser();

/** 添加请求配置项 */
function addHttpRequestParam(arr: HttpRequestParam[]) {
  arr.push({
    key: '',
    type: BpmHttpRequestParamTypeEnum.FIXED_VALUE,
    value: '',
  });
}

/** 删除请求配置项 */
function deleteHttpRequestParam(arr: HttpRequestParam[], index: number) {
  arr.splice(index, 1);
}
</script>
<template>
  <FormItem
    label="请求头"
    :label-col="{ span: 24 }"
    :wrapper-col="{ span: 24 }"
  >
    <Row :gutter="8" v-for="(item, index) in props.header" :key="index">
      <Col :span="7">
        <FormItem
          :name="[bind, 'header', index, 'key']"
          :rules="{
            required: true,
            message: '参数名不能为空',
            trigger: ['blur', 'change'],
          }"
        >
          <Input placeholder="参数名不能为空" v-model:value="item.key" />
        </FormItem>
      </Col>
      <Col :span="5">
        <Select v-model:value="item.type">
          <SelectOption
            v-for="types in BPM_HTTP_REQUEST_PARAM_TYPES"
            :key="types.value"
            :label="types.label"
            :value="types.value"
          >
            {{ types.label }}
          </SelectOption>
        </Select>
      </Col>
      <Col :span="10">
        <FormItem
          :name="[bind, 'header', index, 'value']"
          :rules="{
            required: true,
            message: '参数值不能为空',
            trigger: ['blur', 'change'],
          }"
          v-if="item.type === BpmHttpRequestParamTypeEnum.FIXED_VALUE"
        >
          <Input placeholder="请求头" v-model:value="item.value" />
        </FormItem>
        <FormItem
          :name="[bind, 'header', index, 'value']"
          :rules="{
            required: true,
            message: '参数值不能为空',
            trigger: 'change',
          }"
          v-if="item.type === BpmHttpRequestParamTypeEnum.FROM_FORM"
        >
          <Select v-model:value="item.value" placeholder="请选择表单字段">
            <SelectOption
              v-for="(field, fIdx) in formFieldOptions"
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
      <Col :span="2">
        <div class="flex h-8 items-center">
          <IconifyIcon
            class="size-4 cursor-pointer text-red-500"
            icon="lucide:trash-2"
            @click="deleteHttpRequestParam(props.header, index)"
          />
        </div>
      </Col>
    </Row>
    <Button
      type="link"
      @click="addHttpRequestParam(props.header)"
      class="flex items-center"
    >
      <template #icon>
        <IconifyIcon class="size-4" icon="lucide:plus" />
      </template>
      添加一行
    </Button>
  </FormItem>
  <FormItem
    label="请求体"
    :label-col="{ span: 24 }"
    :wrapper-col="{ span: 24 }"
  >
    <Row :gutter="8" v-for="(item, index) in props.body" :key="index">
      <Col :span="7">
        <FormItem
          :name="[bind, 'body', index, 'key']"
          :rules="{
            required: true,
            message: '参数名不能为空',
            trigger: ['blur', 'change'],
          }"
        >
          <Input placeholder="参数名" v-model:value="item.key" />
        </FormItem>
      </Col>
      <Col :span="5">
        <Select v-model:value="item.type">
          <SelectOption
            v-for="types in BPM_HTTP_REQUEST_PARAM_TYPES"
            :key="types.value"
            :label="types.label"
            :value="types.value"
          >
            {{ types.label }}
          </SelectOption>
        </Select>
      </Col>
      <Col :span="10">
        <FormItem
          :name="[bind, 'body', index, 'value']"
          :rules="{
            required: true,
            message: '参数值不能为空',
            trigger: ['blur', 'change'],
          }"
          v-if="item.type === BpmHttpRequestParamTypeEnum.FIXED_VALUE"
        >
          <Input placeholder="参数值" v-model:value="item.value" />
        </FormItem>
        <FormItem
          :name="[bind, 'body', index, 'value']"
          :rules="{
            required: true,
            message: '参数值不能为空',
            trigger: 'change',
          }"
          v-if="item.type === BpmHttpRequestParamTypeEnum.FROM_FORM"
        >
          <Select v-model:value="item.value" placeholder="请选择表单字段">
            <SelectOption
              v-for="(field, fIdx) in formFieldOptions"
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
      <Col :span="2">
        <div class="flex h-8 items-center">
          <IconifyIcon
            class="size-4 cursor-pointer text-red-500"
            icon="lucide:trash-2"
            @click="deleteHttpRequestParam(props.body, index)"
          />
        </div>
      </Col>
    </Row>
    <Button
      type="link"
      @click="addHttpRequestParam(props.body)"
      class="flex items-center"
    >
      <template #icon>
        <IconifyIcon class="size-4" icon="lucide:plus" />
      </template>
      添加一行
    </Button>
  </FormItem>
</template>
