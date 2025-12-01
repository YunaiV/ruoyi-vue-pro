<script lang="ts" setup>
import type { Rule } from 'ant-design-vue/es/form';

import type { BpmFormApi } from '#/api/bpm/form';

import { ref, watch } from 'vue';

import { BpmModelFormType, DICT_TYPE } from '@vben/constants';
import { getDictOptions } from '@vben/hooks';
import { IconifyIcon } from '@vben/icons';

import FormCreate from '@form-create/ant-design-vue';
import {
  Form,
  FormItem,
  Input,
  Radio,
  RadioGroup,
  Select,
  SelectOption,
  Tooltip,
} from 'ant-design-vue';

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

const rules: Record<string, Rule[]> = {
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
  <Form
    ref="formRef"
    :model="modelData"
    :rules="rules"
    :label-col="{ span: 3 }"
    :wrapper-col="{ span: 21 }"
    class="mt-5"
  >
    <FormItem label="表单类型" name="formType" class="mb-5">
      <RadioGroup v-model:value="modelData.formType">
        <Radio
          v-for="(dict, index) in getDictOptions(
            DICT_TYPE.BPM_MODEL_FORM_TYPE,
            'number',
          )"
          :key="index"
          :value="dict.value"
        >
          {{ dict.label }}
        </Radio>
      </RadioGroup>
    </FormItem>
    <FormItem
      v-if="modelData.formType === BpmModelFormType.NORMAL"
      label="流程表单"
      name="formId"
      class="mb-5"
    >
      <Select v-model:value="modelData.formId" allow-clear>
        <SelectOption
          v-for="form in props.formList"
          :key="form.id"
          :value="form.id"
        >
          {{ form.name }}
        </SelectOption>
        >
      </Select>
    </FormItem>
    <FormItem
      v-if="modelData.formType === BpmModelFormType.CUSTOM"
      label="表单提交路由"
      name="formCustomCreatePath"
      class="mb-5"
    >
      <div class="flex items-center">
        <Input
          v-model:value="modelData.formCustomCreatePath"
          placeholder="请输入表单提交路由"
        />
        <Tooltip
          title="自定义表单的提交路径，使用 Vue 的路由地址, 例如说: bpm/oa/leave/create.vue"
          placement="top"
        >
          <IconifyIcon
            icon="lucide:circle-help"
            class="ml-1 size-5 text-gray-900"
          />
        </Tooltip>
      </div>
    </FormItem>
    <FormItem
      v-if="modelData.formType === BpmModelFormType.CUSTOM"
      label="表单查看地址"
      name="formCustomViewPath"
      class="mb-5"
    >
      <div class="flex items-center">
        <Input
          v-model:value="modelData.formCustomViewPath"
          placeholder="请输入表单查看的组件地址"
        />
        <Tooltip
          title="自定义表单的查看组件地址，使用 Vue 的组件地址，例如说：bpm/oa/leave/detail.vue"
          placement="top"
        >
          <IconifyIcon
            icon="lucide:circle-help"
            class="ml-1 size-5 text-gray-900"
          />
        </Tooltip>
      </div>
    </FormItem>
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
  </Form>
</template>
