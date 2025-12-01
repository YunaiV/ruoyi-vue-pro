<script setup lang="ts">
import type { Rule } from 'ant-design-vue/es/form';
import type { SelectValue } from 'ant-design-vue/es/select';

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
  Button,
  Card,
  Col,
  Divider,
  Form,
  FormItem,
  Input,
  message,
  Row,
  Select,
  SelectOption,
  Tag,
} from 'ant-design-vue';

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
const formRules: Record<string, Rule[]> = reactive({
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
  newKey: SelectValue,
) {
  if (!formSetting?.updateFormFields || !newKey) return;
  const value = formSetting.updateFormFields[oldKey];
  delete formSetting.updateFormFields[oldKey];
  formSetting.updateFormFields[String(newKey)] = value;
}

/** 删除修改字段设置项 */
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
          message.warning(`请选择表单设置${index + 1}要删除的字段`);
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
          message.warning(`请添加表单设置${index + 1}的修改字段`);
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

/** 显示触发器节点配置， 由父组件传过来 */
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
  <Drawer class="w-1/3">
    <template #title>
      <div class="config-header">
        <Input
          ref="inputRef"
          v-if="showInput"
          type="text"
          class="focus:border-blue-500 focus:shadow-[0_0_0_2px_rgba(24,144,255,0.2)] focus:outline-none"
          @blur="changeNodeName()"
          @press-enter="changeNodeName()"
          v-model:value="nodeName"
          :placeholder="nodeName"
        />
        <div v-else class="node-name">
          {{ nodeName }}
          <IconifyIcon class="ml-1" icon="lucide:edit-3" @click="clickIcon()" />
        </div>
      </div>
    </template>
    <div>
      <Form
        ref="formRef"
        :model="configForm"
        :label-col="{ span: 24 }"
        :wrapper-col="{ span: 24 }"
        :rules="formRules"
      >
        <FormItem label="触发器类型" name="type">
          <Select v-model:value="configForm.type" @change="changeTriggerType">
            <SelectOption
              v-for="(item, index) in TRIGGER_TYPES"
              :key="index"
              :value="item.value"
              :label="item.label"
            >
              {{ item.label }}
            </SelectOption>
          </Select>
        </FormItem>
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
            <Card class="mt-4">
              <template #title>
                <div class="flex w-full items-center justify-between">
                  <span>修改表单设置 {{ index + 1 }}</span>
                  <Button
                    v-if="configForm.formSettings!.length > 1"
                    shape="circle"
                    class="flex items-center justify-center"
                    @click="deleteFormSetting(index)"
                  >
                    <template #icon>
                      <IconifyIcon icon="lucide:x" />
                    </template>
                  </Button>
                </div>
              </template>

              <ConditionDialog
                :ref="`condition-${index}`"
                @update-condition="(val) => handleConditionUpdate(index, val)"
              />
              <Row>
                <Col :span="24">
                  <div class="cursor-pointer" v-if="formSetting.conditionType">
                    <Tag
                      color="success"
                      closable
                      class="text-sm"
                      @close="deleteFormSettingCondition(formSetting)"
                      @click="openFormSettingCondition(index, formSetting)"
                    >
                      {{ showConditionText(formSetting) }}
                    </Tag>
                  </div>
                  <Button
                    v-else
                    type="link"
                    class="flex items-center p-0"
                    @click="addFormSettingCondition(index, formSetting)"
                  >
                    <template #icon>
                      <IconifyIcon icon="lucide:link" />
                    </template>
                    添加条件
                  </Button>
                </Col>
              </Row>
              <Divider>修改表单字段设置</Divider>
              <!-- 表单字段修改设置 -->
              <Row
                :gutter="8"
                v-for="key in Object.keys(formSetting.updateFormFields || {})"
                :key="key"
              >
                <Col :span="8">
                  <FormItem>
                    <Select
                      :value="key || undefined"
                      @change="
                        (newKey) => updateFormFieldKey(formSetting, key, newKey)
                      "
                      placeholder="请选择表单字段"
                      :disabled="key !== ''"
                      allow-clear
                    >
                      <SelectOption
                        v-for="(field, fIdx) in optionalUpdateFormFields"
                        :key="fIdx"
                        :label="field.title"
                        :value="field.field"
                        :disabled="field.disabled"
                      >
                        {{ field.title }}
                      </SelectOption>
                    </Select>
                  </FormItem>
                </Col>
                <Col :span="4">
                  <FormItem>的值设置为</FormItem>
                </Col>
                <Col :span="10">
                  <FormItem
                    :name="['formSettings', index, 'updateFormFields', key]"
                    :rules="{
                      required: true,
                      message: '值不能为空',
                      trigger: 'blur',
                    }"
                  >
                    <Input
                      v-model:value="formSetting.updateFormFields![key]"
                      placeholder="请输入值"
                      allow-clear
                      :disabled="!key"
                    />
                  </FormItem>
                </Col>
                <Col :span="2">
                  <div class="flex h-8 items-center">
                    <IconifyIcon
                      class="size-4 cursor-pointer text-red-500"
                      icon="lucide:trash-2"
                      @click="deleteFormFieldSetting(formSetting, key)"
                    />
                  </div>
                </Col>
              </Row>

              <!-- 添加表单字段按钮 -->
              <Row>
                <Col :span="24">
                  <Button
                    type="link"
                    class="flex items-center p-0"
                    @click="addFormFieldSetting(formSetting)"
                  >
                    <template #icon>
                      <IconifyIcon icon="lucide:file-cog" />
                    </template>
                    添加修改字段
                  </Button>
                </Col>
              </Row>
            </Card>
          </div>

          <!-- 添加新的设置 -->
          <Row class="mt-6">
            <Col :span="24">
              <Button
                class="flex items-center p-0"
                type="link"
                @click="addFormSetting"
              >
                <template #icon>
                  <IconifyIcon icon="lucide:settings" />
                </template>
                添加设置
              </Button>
            </Col>
          </Row>
        </div>

        <!-- 表单数据删除触发器 -->
        <div v-if="configForm.type === TriggerTypeEnum.FORM_DELETE">
          <div
            v-for="(formSetting, index) in configForm.formSettings"
            :key="index"
          >
            <Card class="mt-4">
              <template #title>
                <div class="flex w-full items-center justify-between">
                  <span>删除表单设置 {{ index + 1 }}</span>
                  <Button
                    v-if="configForm.formSettings!.length > 1"
                    shape="circle"
                    class="flex items-center justify-center"
                    @click="deleteFormSetting(index)"
                  >
                    <template #icon>
                      <IconifyIcon icon="lucide:x" />
                    </template>
                  </Button>
                </div>
              </template>

              <!-- 条件设置 -->
              <ConditionDialog
                :ref="`condition-${index}`"
                @update-condition="(val) => handleConditionUpdate(index, val)"
              />
              <Row>
                <Col :span="24">
                  <div class="cursor-pointer" v-if="formSetting.conditionType">
                    <Tag
                      color="success"
                      closable
                      class="text-sm"
                      @close="deleteFormSettingCondition(formSetting)"
                      @click="openFormSettingCondition(index, formSetting)"
                    >
                      {{ showConditionText(formSetting) }}
                    </Tag>
                  </div>
                  <Button
                    v-else
                    type="link"
                    class="flex items-center p-0"
                    @click="addFormSettingCondition(index, formSetting)"
                  >
                    <template #icon>
                      <IconifyIcon icon="lucide:link" />
                    </template>
                    添加条件
                  </Button>
                </Col>
              </Row>

              <Divider>删除表单字段设置</Divider>
              <!-- 表单字段删除设置 -->
              <div class="flex flex-wrap gap-2">
                <Select
                  v-model:value="formSetting.deleteFields"
                  mode="multiple"
                  placeholder="请选择要删除的字段"
                  class="w-full"
                >
                  <SelectOption
                    v-for="field in formFields"
                    :key="field.field"
                    :label="field.title"
                    :value="field.field"
                  >
                    {{ field.title }}
                  </SelectOption>
                </Select>
              </div>
            </Card>
          </div>

          <!-- 添加新的设置 -->
          <Row class="mt-6">
            <Col :span="24">
              <Button
                class="flex items-center p-0"
                type="link"
                @click="addFormSetting"
              >
                <template #icon>
                  <IconifyIcon icon="lucide:settings" />
                </template>
                添加设置
              </Button>
            </Col>
          </Row>
        </div>
      </Form>
    </div>
  </Drawer>
</template>
