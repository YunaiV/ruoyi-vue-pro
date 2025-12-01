<script setup lang="ts">
import type { HttpRequestParam } from '../../../consts';

import { IconifyIcon } from '@vben/icons';

import {
  ElButton,
  ElFormItem,
  ElInput,
  ElOption,
  ElSelect,
} from 'element-plus';

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
  <ElFormItem label="请求头">
    <div
      v-for="(item, index) in props.header"
      :key="index"
      class="mb-2 flex items-center gap-2"
    >
      <!-- 参数名 -->
      <div class="w-[26%] min-w-32 shrink-0">
        <ElFormItem
          :prop="`${bind}.header.${index}.key`"
          :rules="{
            required: true,
            message: '参数名不能为空',
            trigger: ['blur', 'change'],
          }"
        >
          <ElInput placeholder="参数名不能为空" v-model="item.key" />
        </ElFormItem>
      </div>

      <!-- 类型选择 -->
      <div class="w-[24%] min-w-11 shrink-0">
        <ElFormItem class="w-full">
          <ElSelect v-model="item.type">
            <ElOption
              v-for="types in BPM_HTTP_REQUEST_PARAM_TYPES"
              :key="types.value"
              :label="types.label"
              :value="types.value"
            />
          </ElSelect>
        </ElFormItem>
      </div>

      <!-- 参数值 -->
      <div class="w-[42%] flex-1">
        <ElFormItem
          :prop="`${bind}.header.${index}.value`"
          :rules="{
            required: true,
            message: '参数值不能为空',
            trigger: ['blur', 'change'],
          }"
          v-if="item.type === BpmHttpRequestParamTypeEnum.FIXED_VALUE"
        >
          <ElInput placeholder="请求头" v-model="item.value" />
        </ElFormItem>
        <ElFormItem
          :prop="`${bind}.header.${index}.value`"
          :rules="{
            required: true,
            message: '参数值不能为空',
            trigger: 'change',
          }"
          v-if="item.type === BpmHttpRequestParamTypeEnum.FROM_FORM"
        >
          <ElSelect
            class="min-w-36"
            v-model="item.value"
            placeholder="请选择表单字段"
          >
            <ElOption
              v-for="(field, fIdx) in formFieldOptions"
              :key="fIdx"
              :label="field.title"
              :value="field.field"
              :disabled="!field.required"
            />
          </ElSelect>
        </ElFormItem>
      </div>

      <!-- 删除按钮 -->
      <div class="flex w-[8%] shrink-0 items-center">
        <IconifyIcon
          class="size-4 cursor-pointer text-red-500"
          icon="lucide:trash-2"
          @click="deleteHttpRequestParam(props.header, index)"
        />
      </div>
    </div>
  </ElFormItem>
  <div class="-mt-2.5 mb-2">
    <ElButton link @click="addHttpRequestParam(props.header)">
      <template #icon>
        <IconifyIcon class="size-4" icon="lucide:plus" />
      </template>
      添加一行
    </ElButton>
  </div>
  <ElFormItem label="请求体">
    <div
      v-for="(item, index) in props.body"
      :key="index"
      class="mb-2 flex items-center gap-2"
    >
      <!-- 参数名 -->
      <div class="w-[26%] min-w-32 shrink-0">
        <ElFormItem
          :prop="`${bind}.body.${index}.key`"
          :rules="{
            required: true,
            message: '参数名不能为空',
            trigger: ['blur', 'change'],
          }"
        >
          <ElInput placeholder="参数名" v-model="item.key" />
        </ElFormItem>
      </div>

      <!-- 类型选择 -->
      <div class="w-[24%] min-w-11 shrink-0">
        <ElFormItem>
          <ElSelect v-model="item.type">
            <ElOption
              v-for="types in BPM_HTTP_REQUEST_PARAM_TYPES"
              :key="types.value"
              :label="types.label"
              :value="types.value"
            />
          </ElSelect>
        </ElFormItem>
      </div>

      <!-- 参数值 -->
      <div class="w-[42%] flex-1">
        <ElFormItem
          :prop="`${bind}.body.${index}.value`"
          :rules="{
            required: true,
            message: '参数值不能为空',
            trigger: ['blur', 'change'],
          }"
          v-show="item.type === BpmHttpRequestParamTypeEnum.FIXED_VALUE"
        >
          <ElInput placeholder="参数值" v-model="item.value" />
        </ElFormItem>
        <ElFormItem
          :prop="`${bind}.body.${index}.value`"
          :rules="{
            required: true,
            message: '参数值不能为空',
            trigger: 'change',
          }"
          v-show="item.type === BpmHttpRequestParamTypeEnum.FROM_FORM"
        >
          <ElSelect
            class="min-w-36"
            v-model="item.value"
            placeholder="请选择表单字段"
          >
            <ElOption
              v-for="(field, fIdx) in formFieldOptions"
              :key="fIdx"
              :label="field.title"
              :value="field.field"
              :disabled="!field.required"
            />
          </ElSelect>
        </ElFormItem>
      </div>

      <!-- 删除按钮 -->
      <div class="flex w-[8%] shrink-0 items-center">
        <IconifyIcon
          class="size-4 cursor-pointer text-red-500"
          icon="lucide:trash-2"
          @click="deleteHttpRequestParam(props.body, index)"
        />
      </div>
    </div>
  </ElFormItem>
  <div class="-mt-2.5 mb-2">
    <ElButton link @click="addHttpRequestParam(props.body)">
      <template #icon>
        <IconifyIcon class="size-4" icon="lucide:plus" />
      </template>
      添加一行
    </ElButton>
  </div>
</template>
