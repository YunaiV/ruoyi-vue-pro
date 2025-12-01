<script lang="ts" setup>
import type { FormItemRule } from 'element-plus';

import type { BpmFormApi } from '#/api/bpm/form';

import { ref, watch } from 'vue';

import { BpmModelFormType, DICT_TYPE } from '@vben/constants';
import { getDictOptions } from '@vben/hooks';
import { IconifyIcon } from '@vben/icons';

import FormCreate from '@form-create/element-ui';
import {
  ElForm,
  ElFormItem,
  ElInput,
  ElOption,
  ElRadio,
  ElRadioGroup,
  ElSelect,
  ElTooltip,
} from 'element-plus';

import { getForm } from '#/api/bpm/form';
import { setConfAndFields2 } from '#/components/form-create';

const props = defineProps({
  formList: {
    type: Array<BpmFormApi.Form>,
    required: true,
  },
});

const formRef = ref();

const modelData = defineModel<any>(); // 创建本地数据副本
const formPreview = ref({
  formData: {} as any,
  rule: [],
  option: {
    submitBtn: false,
    resetBtn: false,
    formData: {},
  },
}); // 表单预览数据

const rules: Record<string, FormItemRule[]> = {
  formType: [{ required: true, message: '表单类型不能为空', trigger: 'blur' }],
  formId: [{ required: true, message: '流程表单不能为空', trigger: 'blur' }],
  formCustomCreatePath: [
    { required: true, message: '表单提交路由不能为空', trigger: 'blur' },
  ],
  formCustomViewPath: [
    { required: true, message: '表单查看地址不能为空', trigger: 'blur' },
  ],
};

/** 监听表单 ID 变化，加载表单数据 */
watch(
  () => modelData.value.formId,
  async (newFormId) => {
    if (newFormId && modelData.value.formType === BpmModelFormType.NORMAL) {
      const data = await getForm(newFormId);
      setConfAndFields2(formPreview.value, data.conf, data.fields);
      // 设置只读
      formPreview.value.rule.forEach((item: any) => {
        item.props = { ...item.props, disabled: true };
      });
    } else {
      formPreview.value.rule = [];
    }
  },
  { immediate: true },
);

/** 表单校验 */
async function validate() {
  await formRef.value?.validate();
}

defineExpose({ validate });
</script>
<template>
  <ElForm
    ref="formRef"
    :model="modelData"
    :rules="rules"
    label-width="120px"
    class="mt-5"
  >
    <ElFormItem label="表单类型" prop="formType" class="mb-5">
      <ElRadioGroup v-model="modelData.formType">
        <ElRadio
          v-for="(dict, index) in getDictOptions(
            DICT_TYPE.BPM_MODEL_FORM_TYPE,
            'number',
          )"
          :key="index"
          :value="dict.value"
        >
          {{ dict.label }}
        </ElRadio>
      </ElRadioGroup>
    </ElFormItem>
    <ElFormItem
      v-if="modelData.formType === BpmModelFormType.NORMAL"
      label="流程表单"
      prop="formId"
      class="mb-5"
    >
      <ElSelect v-model="modelData.formId" clearable>
        <ElOption
          v-for="form in props.formList"
          :key="form.id"
          :value="form.id!"
          :label="form.name"
        />
      </ElSelect>
    </ElFormItem>
    <ElFormItem
      v-if="modelData.formType === BpmModelFormType.CUSTOM"
      label="表单提交路由"
      prop="formCustomCreatePath"
      class="mb-5"
    >
      <div class="flex items-center">
        <ElInput
          v-model="modelData.formCustomCreatePath"
          placeholder="请输入表单提交路由"
        />
        <ElTooltip
          content="自定义表单的提交路径，使用 Vue 的路由地址, 例如说: bpm/oa/leave/create.vue"
          placement="top"
        >
          <IconifyIcon
            icon="lucide:circle-help"
            class="ml-1 size-5 text-gray-900"
          />
        </ElTooltip>
      </div>
    </ElFormItem>
    <ElFormItem
      v-if="modelData.formType === BpmModelFormType.CUSTOM"
      label="表单查看地址"
      prop="formCustomViewPath"
      class="mb-5"
    >
      <div class="flex items-center">
        <ElInput
          v-model="modelData.formCustomViewPath"
          placeholder="请输入表单查看的组件地址"
        />
        <ElTooltip
          content="自定义表单的查看组件地址，使用 Vue 的组件地址，例如说：bpm/oa/leave/detail.vue"
          placement="top"
        >
          <IconifyIcon
            icon="lucide:circle-help"
            class="ml-1 size-5 text-gray-900"
          />
        </ElTooltip>
      </div>
    </ElFormItem>
    <!-- 表单预览 -->
    <div
      v-if="
        modelData.formType === BpmModelFormType.NORMAL &&
        modelData.formId &&
        formPreview.rule.length > 0
      "
      class="mb-5 mt-7 rounded-sm border border-solid border-gray-200 p-5"
    >
      <div class="mb-4 flex items-center">
        <div class="mr-2 h-4 w-1 bg-blue-500"></div>
        <span class="text-base font-bold">表单预览</span>
      </div>
      <FormCreate
        v-model:api="formPreview.formData"
        :rule="formPreview.rule"
        :option="formPreview.option"
      />
    </div>
  </ElForm>
</template>
