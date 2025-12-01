<script setup lang="ts">
import type { FormRules } from 'element-plus';
// SelectValue type removed - use string | number | boolean directly

import type {
  FormTriggerSetting,
  SimpleFlowNode,
  TriggerSetting,
} from '../../consts';

import { computed, getCurrentInstance, onMounted, reactive, ref } from 'vue';

import { useVbenDrawer } from '@vben/common-ui';
import { BpmNodeTypeEnum } from '@vben/constants';
import { IconifyIcon } from '@vben/icons';
import { cloneDeep } from '@vben/utils';

import {
  ElButton,
  ElCard,
  ElCol,
  ElDivider,
  ElForm,
  ElFormItem,
  ElInput,
  ElMessage,
  ElOption,
  ElRow,
  ElSelect,
  ElTag,
} from 'element-plus';

import {
  DEFAULT_CONDITION_GROUP_VALUE,
  TRIGGER_TYPES,
  TriggerTypeEnum,
} from '../../consts';
import {
  getConditionShowText,
  useFormFields,
  useFormFieldsAndStartUser,
  useNodeName,
  useWatchNode,
} from '../../helpers';
import ConditionDialog from './modules/condition-dialog.vue';
import HttpRequestSetting from './modules/http-request-setting.vue';

defineOptions({
  name: 'TriggerNodeConfig',
});

const props = defineProps({
  flowNode: {
    type: Object as () => SimpleFlowNode,
    required: true,
  },
});

const { proxy } = getCurrentInstance() as any;

// 抽屉配置
const [Drawer, drawerApi] = useVbenDrawer({
  header: true,
  closable: true,
  title: '',
  onConfirm() {
    saveConfig();
  },
});

// 当前节点
const currentNode = useWatchNode(props);
// 节点名称
const { nodeName, showInput, clickIcon, changeNodeName, inputRef } =
  useNodeName(BpmNodeTypeEnum.TRIGGER_NODE);
// 触发器表单配置
const formRef = ref(); // 表单 Ref

// 表单校验规则
const formRules: FormRules = reactive({
  type: [{ required: true, message: '触发器类型不能为空', trigger: 'change' }],
  'httpRequestSetting.url': [
    { required: true, message: '请求地址不能为空', trigger: 'blur' },
  ],
});

// 触发器配置表单数据
const configForm = ref<TriggerSetting>({
  type: TriggerTypeEnum.HTTP_REQUEST,
  httpRequestSetting: {
    url: '',
    header: [],
    body: [],
    response: [],
  },
  formSettings: [
    {
      conditionGroups: cloneDeep(DEFAULT_CONDITION_GROUP_VALUE),
      updateFormFields: {},
      deleteFields: [],
    },
  ],
});
// 流程表单字段
const formFields = useFormFields();

// 可选的修改的表单字段
const optionalUpdateFormFields = computed(() => {
  return formFields.map((field) => ({
    title: field.title,
    field: field.field,
    disabled: false,
  }));
});

let originalSetting: TriggerSetting | undefined;

/** 触发器类型改变了 */
function changeTriggerType() {
  if (configForm.value.type === TriggerTypeEnum.HTTP_REQUEST) {
    configForm.value.httpRequestSetting =
      originalSetting?.type === TriggerTypeEnum.HTTP_REQUEST &&
      originalSetting.httpRequestSetting
        ? originalSetting.httpRequestSetting
        : {
            url: '',
            header: [],
            body: [],
            response: [],
          };
    configForm.value.formSettings = undefined;
    return;
  }

  if (configForm.value.type === TriggerTypeEnum.HTTP_CALLBACK) {
    configForm.value.httpRequestSetting =
      originalSetting?.type === TriggerTypeEnum.HTTP_CALLBACK &&
      originalSetting.httpRequestSetting
        ? originalSetting.httpRequestSetting
        : {
            url: '',
            header: [],
            body: [],
            response: [],
          };
    configForm.value.formSettings = undefined;
    return;
  }

  if (configForm.value.type === TriggerTypeEnum.FORM_UPDATE) {
    configForm.value.formSettings =
      originalSetting?.type === TriggerTypeEnum.FORM_UPDATE &&
      originalSetting.formSettings
        ? originalSetting.formSettings
        : [
            {
              conditionGroups: cloneDeep(DEFAULT_CONDITION_GROUP_VALUE),
              updateFormFields: {},
              deleteFields: [],
            },
          ];
    configForm.value.httpRequestSetting = undefined;
    return;
  }

  if (configForm.value.type === TriggerTypeEnum.FORM_DELETE) {
    configForm.value.formSettings =
      originalSetting?.type === TriggerTypeEnum.FORM_DELETE &&
      originalSetting.formSettings
        ? originalSetting.formSettings
        : [
            {
              conditionGroups: cloneDeep(DEFAULT_CONDITION_GROUP_VALUE),
              updateFormFields: undefined,
              deleteFields: [],
            },
          ];
    configForm.value.httpRequestSetting = undefined;
  }
}

/** 添加新的修改表单设置 */
function addFormSetting() {
  configForm.value.formSettings!.push({
    conditionGroups: cloneDeep(DEFAULT_CONDITION_GROUP_VALUE),
    updateFormFields: {},
    deleteFields: [],
  });
}

/** 删除修改表单设置 */
function deleteFormSetting(index: number) {
  configForm.value.formSettings!.splice(index, 1);
}

/** 添加条件配置 */
function addFormSettingCondition(
  index: number,
  formSetting: FormTriggerSetting,
) {
  const conditionDialog = proxy.$refs[`condition-${index}`][0];
  // 打开模态框并传递数据
  conditionDialog.openModal(formSetting);
}

/** 删除条件配置 */
function deleteFormSettingCondition(formSetting: FormTriggerSetting) {
  formSetting.conditionType = undefined;
}

/** 打开条件配置弹窗 */
function openFormSettingCondition(
  index: number,
  formSetting: FormTriggerSetting,
) {
  const conditionDialog = proxy.$refs[`condition-${index}`][0];
  // 打开模态框并传递数据
  conditionDialog.openModal(formSetting);
}

/** 处理条件配置保存 */
function handleConditionUpdate(index: number, condition: any) {
  if (configForm.value.formSettings![index]) {
    configForm.value.formSettings![index].conditionType =
      condition.conditionType;
    configForm.value.formSettings![index].conditionExpression =
      condition.conditionExpression;
    configForm.value.formSettings![index].conditionGroups =
      condition.conditionGroups;
  }
}
// 包含发起人字段的表单字段
const includeStartUserFormFields = useFormFieldsAndStartUser();
/** 条件配置展示 */
function showConditionText(formSetting: FormTriggerSetting) {
  return getConditionShowText(
    formSetting.conditionType,
    formSetting.conditionExpression,
    formSetting.conditionGroups,
    includeStartUserFormFields,
  );
}

/** 添加修改字段设置项 */
function addFormFieldSetting(formSetting: FormTriggerSetting) {
  if (!formSetting) return;
  if (!formSetting.updateFormFields) {
    formSetting.updateFormFields = {};
  }
  formSetting.updateFormFields[''] = undefined;
}

/** 更新字段 KEY */
function updateFormFieldKey(
  formSetting: FormTriggerSetting,
  oldKey: string,
  newKey: number | string,
) {
  if (!formSetting?.updateFormFields || !newKey) return;
  const value = formSetting.updateFormFields[oldKey];
  delete formSetting.updateFormFields[oldKey];
  formSetting.updateFormFields[String(newKey)] = value;
}

/** 删除修改字段设置 */
function deleteFormFieldSetting(formSetting: FormTriggerSetting, key: string) {
  if (!formSetting?.updateFormFields) return;
  delete formSetting.updateFormFields[key];
}

/** 保存配置 */
async function saveConfig() {
  if (!formRef.value) return false;
  const valid = await formRef.value.validate();
  if (!valid) return false;
  const showText = getShowText();
  if (!showText) return false;
  currentNode.value.name = nodeName.value!;
  currentNode.value.showText = showText;
  switch (configForm.value.type) {
    case TriggerTypeEnum.FORM_DELETE: {
      configForm.value.httpRequestSetting = undefined;
      // 清理修改字段相关的数据
      configForm.value.formSettings?.forEach((setting) => {
        setting.updateFormFields = undefined;
      });

      break;
    }
    case TriggerTypeEnum.FORM_UPDATE: {
      configForm.value.httpRequestSetting = undefined;
      // 清理删除字段相关的数据
      configForm.value.formSettings?.forEach((setting) => {
        setting.deleteFields = undefined;
      });

      break;
    }
    case TriggerTypeEnum.HTTP_REQUEST: {
      configForm.value.formSettings = undefined;

      break;
    }
    // No default
  }
  currentNode.value.triggerSetting = configForm.value;
  drawerApi.close();
  return true;
}

/** 获取节点展示内容 */
function getShowText(): string {
  let showText = '';
  switch (configForm.value.type) {
    case TriggerTypeEnum.FORM_DELETE: {
      for (const [index, setting] of configForm.value.formSettings!.entries()) {
        if (!setting.deleteFields || setting.deleteFields.length === 0) {
          ElMessage.warning(`请选择表单设置${index + 1}要删除的字段`);
          return '';
        }
      }
      showText = '删除表单数据';

      break;
    }
    case TriggerTypeEnum.FORM_UPDATE: {
      for (const [index, setting] of configForm.value.formSettings!.entries()) {
        if (
          !setting.updateFormFields ||
          Object.keys(setting.updateFormFields).length === 0
        ) {
          ElMessage.warning(`请添加表单设置${index + 1}的修改字段`);
          return '';
        }
      }
      showText = '修改表单数据';

      break;
    }
    case TriggerTypeEnum.HTTP_CALLBACK:
    case TriggerTypeEnum.HTTP_REQUEST: {
      showText = `${configForm.value.httpRequestSetting?.url}`;

      break;
    }
    // No default
  }
  return showText;
}

/** 显示触发器节点配置，由父组件传过来 */
function showTriggerNodeConfig(node: SimpleFlowNode) {
  nodeName.value = node.name;
  originalSetting = node.triggerSetting
    ? cloneDeep(node.triggerSetting)
    : undefined;
  if (node.triggerSetting) {
    configForm.value = {
      type: node.triggerSetting.type,
      httpRequestSetting: node.triggerSetting.httpRequestSetting || {
        url: '',
        header: [],
        body: [],
        response: [],
      },
      formSettings: node.triggerSetting.formSettings || [
        {
          conditionGroups: cloneDeep(DEFAULT_CONDITION_GROUP_VALUE),
          updateFormFields: {},
          deleteFields: [],
        },
      ],
    };
  }
  drawerApi.open();
}

// 暴露方法给父组件
defineExpose({ showTriggerNodeConfig });

onMounted(() => {
  // 初始化可能需要的操作
});
</script>
<template>
  <Drawer class="w-2/5">
    <template #title>
      <div class="config-header">
        <ElInput
          ref="inputRef"
          v-if="showInput"
          type="text"
          class="focus:border-blue-500 focus:shadow-[0_0_0_2px_rgba(24,144,255,0.2)] focus:outline-none"
          @blur="changeNodeName()"
          @keyup.enter="changeNodeName()"
          v-model="nodeName"
          :placeholder="nodeName"
        />
        <div v-else class="node-name">
          {{ nodeName }}
          <IconifyIcon class="ml-1" icon="lucide:edit-3" @click="clickIcon()" />
        </div>
      </div>
    </template>
    <div>
      <ElForm
        ref="formRef"
        :model="configForm"
        label-position="top"
        :rules="formRules"
      >
        <ElFormItem label="触发器类型" prop="type">
          <ElSelect v-model="configForm.type" @change="changeTriggerType">
            <ElOption
              v-for="(item, index) in TRIGGER_TYPES"
              :key="index"
              :value="item.value"
              :label="item.label"
            />
          </ElSelect>
        </ElFormItem>
        <!-- HTTP 请求触发器 -->
        <div
          v-if="
            [
              TriggerTypeEnum.HTTP_REQUEST,
              TriggerTypeEnum.HTTP_CALLBACK,
            ].includes(configForm.type) && configForm.httpRequestSetting
          "
        >
          <HttpRequestSetting
            v-model:setting="configForm.httpRequestSetting"
            :response-enable="configForm.type === TriggerTypeEnum.HTTP_REQUEST"
            form-item-prefix="httpRequestSetting"
          />
        </div>

        <!-- 表单数据修改触发器 -->
        <div v-if="configForm.type === TriggerTypeEnum.FORM_UPDATE">
          <div
            v-for="(formSetting, index) in configForm.formSettings"
            :key="index"
          >
            <ElCard class="mt-4">
              <template #header>
                <div class="flex w-full items-center justify-between">
                  <span>修改表单设置 {{ index + 1 }}</span>
                  <ElButton
                    v-if="configForm.formSettings!.length > 1"
                    circle
                    @click="deleteFormSetting(index)"
                  >
                    <template #icon>
                      <IconifyIcon icon="lucide:x" />
                    </template>
                  </ElButton>
                </div>
              </template>

              <ConditionDialog
                :ref="`condition-${index}`"
                @update-condition="(val) => handleConditionUpdate(index, val)"
              />
              <ElRow>
                <ElCol :span="24">
                  <div class="cursor-pointer" v-if="formSetting.conditionType">
                    <ElTag
                      color="success"
                      closable
                      class="text-sm"
                      @close="deleteFormSettingCondition(formSetting)"
                      @click="openFormSettingCondition(index, formSetting)"
                    >
                      {{ showConditionText(formSetting) }}
                    </ElTag>
                  </div>
                  <ElButton
                    v-else
                    type="primary"
                    link
                    class="flex items-center p-0"
                    @click="addFormSettingCondition(index, formSetting)"
                  >
                    <template #icon>
                      <IconifyIcon icon="lucide:link" />
                    </template>
                    添加条件
                  </ElButton>
                </ElCol>
              </ElRow>
              <ElDivider>修改表单字段设置</ElDivider>
              <!-- 表单字段修改设置 -->
              <ElRow
                :gutter="8"
                v-for="key in Object.keys(formSetting.updateFormFields || {})"
                :key="key"
              >
                <ElCol :span="8">
                  <ElFormItem>
                    <ElSelect
                      :value="key || undefined"
                      @change="
                        (newKey) => updateFormFieldKey(formSetting, key, newKey)
                      "
                      placeholder="请选择表单字段"
                      :disabled="key !== ''"
                      allow-clear
                    >
                      <ElOption
                        v-for="(field, fIdx) in optionalUpdateFormFields"
                        :key="fIdx"
                        :label="field.title"
                        :value="field.field"
                        :disabled="field.disabled"
                      >
                        {{ field.title }}
                      </ElOption>
                    </ElSelect>
                  </ElFormItem>
                </ElCol>
                <ElCol :span="4">
                  <ElFormItem>的值设置为</ElFormItem>
                </ElCol>
                <ElCol :span="10">
                  <ElFormItem
                    :name="['formSettings', index, 'updateFormFields', key]"
                    :rules="{
                      required: true,
                      message: '值不能为空',
                      trigger: 'blur',
                    }"
                  >
                    <ElInput
                      v-model="formSetting.updateFormFields![key]"
                      placeholder="请输入值"
                      clearable
                      :disabled="!key"
                    />
                  </ElFormItem>
                </ElCol>
                <ElCol :span="2">
                  <div class="flex h-8 items-center">
                    <IconifyIcon
                      class="size-4 cursor-pointer text-red-500"
                      icon="lucide:trash-2"
                      @click="deleteFormFieldSetting(formSetting, key)"
                    />
                  </div>
                </ElCol>
              </ElRow>

              <!-- 添加表单字段按钮 -->
              <ElRow>
                <ElCol :span="24">
                  <ElButton
                    type="primary"
                    link
                    class="flex items-center p-0"
                    @click="addFormFieldSetting(formSetting)"
                  >
                    <template #icon>
                      <IconifyIcon icon="lucide:file-cog" />
                    </template>
                    添加修改字段
                  </ElButton>
                </ElCol>
              </ElRow>
            </ElCard>
          </div>

          <!-- 添加新的设置 -->
          <ElRow class="mt-6">
            <ElCol :span="24">
              <ElButton
                class="flex items-center p-0"
                link
                @click="addFormSetting"
              >
                <template #icon>
                  <IconifyIcon icon="lucide:settings" />
                </template>
                添加设置
              </ElButton>
            </ElCol>
          </ElRow>
        </div>

        <!-- 表单数据删除触发器 -->
        <div v-if="configForm.type === TriggerTypeEnum.FORM_DELETE">
          <div
            v-for="(formSetting, index) in configForm.formSettings"
            :key="index"
          >
            <ElCard class="mt-4">
              <template #header>
                <div class="flex w-full items-center justify-between">
                  <span>删除表单设置 {{ index + 1 }}</span>
                  <ElButton
                    v-if="configForm.formSettings!.length > 1"
                    circle
                    @click="deleteFormSetting(index)"
                  >
                    <template #icon>
                      <IconifyIcon icon="lucide:x" />
                    </template>
                  </ElButton>
                </div>
              </template>

              <!-- 条件设置 -->
              <ConditionDialog
                :ref="`condition-${index}`"
                @update-condition="(val) => handleConditionUpdate(index, val)"
              />
              <ElRow>
                <ElCol :span="24">
                  <div class="cursor-pointer" v-if="formSetting.conditionType">
                    <ElTag
                      color="success"
                      closable
                      class="text-sm"
                      @close="deleteFormSettingCondition(formSetting)"
                      @click="openFormSettingCondition(index, formSetting)"
                    >
                      {{ showConditionText(formSetting) }}
                    </ElTag>
                  </div>
                  <ElButton
                    v-else
                    type="primary"
                    link
                    class="flex items-center p-0"
                    @click="addFormSettingCondition(index, formSetting)"
                  >
                    <template #icon>
                      <IconifyIcon icon="lucide:link" />
                    </template>
                    添加条件
                  </ElButton>
                </ElCol>
              </ElRow>

              <ElDivider>删除表单字段设置</ElDivider>
              <!-- 表单字段删除设置 -->
              <div class="flex flex-wrap gap-2">
                <ElSelect
                  v-model:value="formSetting.deleteFields"
                  mode="multiple"
                  placeholder="请选择要删除的字段"
                  class="w-full"
                >
                  <ElOption
                    v-for="field in formFields"
                    :key="field.field"
                    :label="field.title"
                    :value="field.field"
                  >
                    {{ field.title }}
                  </ElOption>
                </ElSelect>
              </div>
            </ElCard>
          </div>

          <!-- 添加新的设置 -->
          <ElRow class="mt-6">
            <ElCol :span="24">
              <ElButton
                class="flex items-center p-0"
                link
                @click="addFormSetting"
              >
                <template #icon>
                  <IconifyIcon icon="lucide:settings" />
                </template>
                添加设置
              </ElButton>
            </ElCol>
          </ElRow>
        </div>
      </ElForm>
    </div>
  </Drawer>
</template>
