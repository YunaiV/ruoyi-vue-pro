<script setup lang="ts">
import type { FormRules } from 'element-plus';

import type { Ref } from 'vue';

import { computed, inject, reactive, ref } from 'vue';

import { BpmModelFormType } from '@vben/constants';
import { IconifyIcon, Plus, Trash2 } from '@vben/icons';
import { cloneDeep } from '@vben/utils';

import {
  ElCard,
  ElCol,
  ElForm,
  ElFormItem,
  ElInput,
  ElOption,
  ElRadio,
  ElRadioGroup,
  ElRow,
  ElSelect,
  ElSpace,
  ElSwitch,
  ElTooltip,
} from 'element-plus';

import {
  COMPARISON_OPERATORS,
  CONDITION_CONFIG_TYPES,
  ConditionType,
  DEFAULT_CONDITION_GROUP_VALUE,
} from '../../../consts';
import { useFormFieldsAndStartUser } from '../../../helpers';

defineOptions({
  name: 'Condition',
});

const props = defineProps({
  modelValue: {
    type: Object,
    default: () => ({}),
  },
});

const emit = defineEmits(['update:modelValue']);

const condition = computed({
  get() {
    return props.modelValue;
  },
  set(newValue) {
    emit('update:modelValue', newValue);
  },
});

const formType = inject<Ref<number>>('formType'); // 表单类型
const conditionConfigTypes = computed(() => {
  return CONDITION_CONFIG_TYPES.filter((item) => {
    // 业务表单暂时去掉条件规则选项
    return !(
      formType?.value === BpmModelFormType.CUSTOM &&
      item.value === ConditionType.RULE
    );
  });
});

/** 条件规则可选择的表单字段 */
const fieldOptions = useFormFieldsAndStartUser();

// 表单校验规则
const formRules: FormRules = reactive({
  conditionType: [
    { required: true, message: '配置方式不能为空', trigger: 'change' },
  ],
  conditionExpression: [
    {
      required: true,
      message: '条件表达式不能为空',
      trigger: ['blur', 'change'],
    },
  ],
});

const formRef = ref(); // 表单 Ref

/** 切换条件配置方式 */
function changeConditionType() {
  if (
    condition.value.conditionType === ConditionType.RULE &&
    !condition.value.conditionGroups
  ) {
    condition.value.conditionGroups = cloneDeep(DEFAULT_CONDITION_GROUP_VALUE);
  }
}

function deleteConditionGroup(conditions: any, index: number) {
  conditions.splice(index, 1);
}

function deleteConditionRule(condition: any, index: number) {
  condition.rules.splice(index, 1);
}

function addConditionRule(condition: any, index: number) {
  const rule = {
    opCode: '==',
    leftSide: undefined,
    rightSide: '',
  };
  condition.rules.splice(index + 1, 0, rule);
}

function addConditionGroup(conditions: any) {
  const condition = {
    and: true,
    rules: [
      {
        opCode: '==',
        leftSide: undefined,
        rightSide: '',
      },
    ],
  };
  conditions.push(condition);
}

async function validate() {
  if (!formRef.value) return false;
  return await formRef.value.validate();
}

defineExpose({ validate });
</script>
<template>
  <ElForm ref="formRef" :model="condition" :rules="formRules">
    <ElFormItem label="配置方式" prop="conditionType">
      <ElRadioGroup
        v-model="condition.conditionType"
        @change="changeConditionType"
      >
        <ElRadio
          v-for="(dict, indexConditionType) in conditionConfigTypes"
          :key="indexConditionType"
          :value="dict.value"
        >
          {{ dict.label }}
        </ElRadio>
      </ElRadioGroup>
    </ElFormItem>
    <ElFormItem
      v-if="
        condition.conditionType === ConditionType.RULE &&
        condition.conditionGroups
      "
    >
      <div class="mb-5 flex w-full justify-between">
        <div class="flex items-center">
          <div class="mr-4">条件组关系</div>
          <ElSwitch
            v-model="condition.conditionGroups.and"
            active-text="且"
            inactive-text="或"
          />
        </div>
      </div>
      <ElSpace
        direction="vertical"
        size="small"
        :spacer="condition.conditionGroups.and ? '且' : '或'"
        class="w-full"
        fill
        :fill-ratio="100"
      >
        <ElCard
          class="group relative w-full hover:border-blue-500"
          v-for="(equation, cIdx) in condition.conditionGroups.conditions"
          :key="cIdx"
        >
          <div
            class="absolute left-0 top-0 z-[1] flex cursor-pointer opacity-0 group-hover:opacity-100"
            v-if="condition.conditionGroups.conditions.length > 1"
          >
            <IconifyIcon
              color="blue"
              icon="lucide:circle-x"
              class="size-4"
              @click="
                deleteConditionGroup(condition.conditionGroups.conditions, cIdx)
              "
            />
          </div>
          <template #extra>
            <div class="flex items-center justify-between">
              <div>条件组</div>
              <div class="flex">
                <div class="mr-4">规则关系</div>
                <ElSwitch
                  v-model="equation.and"
                  active-text="且"
                  inactive-text="或"
                />
              </div>
            </div>
          </template>

          <ElRow
            :gutter="8"
            align="middle"
            class="mb-2"
            v-for="(rule, rIdx) in equation.rules"
            :key="rIdx"
          >
            <ElCol :span="7">
              <ElFormItem
                :prop="`conditionGroups.conditions.${cIdx}.rules.${rIdx}.leftSide`"
                :rules="{
                  required: true,
                  message: '左值不能为空',
                  trigger: 'change',
                }"
              >
                <ElSelect
                  v-model="rule.leftSide"
                  clearable
                  placeholder="请选择表单字段"
                >
                  <ElOption
                    v-for="(field, fIdx) in fieldOptions"
                    :key="fIdx"
                    :label="field.title"
                    :value="field.field"
                    :disabled="!field.required"
                  >
                    <ElTooltip
                      content="表单字段非必填时不能作为流程分支条件"
                      placement="right"
                      v-if="!field.required"
                    >
                      <span>{{ field.title }}</span>
                    </ElTooltip>
                    <template v-else>{{ field.title }}</template>
                  </ElOption>
                </ElSelect>
              </ElFormItem>
            </ElCol>
            <ElCol :span="6">
              <ElFormItem>
                <ElSelect v-model="rule.opCode" placeholder="请选择操作符">
                  <ElOption
                    v-for="operator in COMPARISON_OPERATORS"
                    :key="operator.value"
                    :label="operator.label"
                    :value="operator.value"
                  >
                    {{ operator.label }}
                  </ElOption>
                </ElSelect>
              </ElFormItem>
            </ElCol>
            <ElCol :span="8">
              <ElFormItem
                :prop="`conditionGroups.conditions.${cIdx}.rules.${rIdx}.rightSide`"
                :rules="{
                  required: true,
                  message: '右值不能为空',
                  trigger: ['blur', 'change'],
                }"
              >
                <ElInput v-model="rule.rightSide" placeholder="请输入右值" />
              </ElFormItem>
            </ElCol>
            <ElCol :span="3">
              <div class="flex items-center">
                <Plus
                  class="mr-2 size-4 cursor-pointer text-blue-500"
                  @click="addConditionRule(equation, rIdx)"
                />
                <Trash2
                  v-show="equation.rules.length > 1"
                  class="size-4 cursor-pointer text-red-500"
                  @click="deleteConditionRule(equation, rIdx)"
                />
              </div>
            </ElCol>
          </ElRow>
        </ElCard>
      </ElSpace>
      <div
        class="mt-4 flex cursor-pointer items-center text-blue-500 hover:text-blue-600"
        @click="addConditionGroup(condition.conditionGroups?.conditions)"
      >
        <Plus class="mr-1 size-5" />
        <span>添加条件组</span>
      </div>
    </ElFormItem>
    <ElFormItem
      v-if="condition.conditionType === ConditionType.EXPRESSION"
      label="条件表达式"
      prop="conditionExpression"
    >
      <ElInput
        v-model="condition.conditionExpression"
        type="textarea"
        placeholder="请输入条件表达式"
        clearable
        :autosize="{ minRows: 3, maxRows: 6 }"
      />
    </ElFormItem>
  </ElForm>
</template>
