<script setup lang="ts">
import type { FormRules } from 'element-plus';

import type { IOParameter, SimpleFlowNode } from '../../consts';

import { computed, onMounted, reactive, ref } from 'vue';

import { useVbenDrawer } from '@vben/common-ui';
import { BpmNodeTypeEnum } from '@vben/constants';
import { IconifyIcon } from '@vben/icons';

import { getForm } from '#/api/bpm/form';
import { getModelList } from '#/api/bpm/model';

import {
  CHILD_PROCESS_MULTI_INSTANCE_SOURCE_TYPE,
  CHILD_PROCESS_START_USER_EMPTY_TYPE,
  CHILD_PROCESS_START_USER_TYPE,
  ChildProcessMultiInstanceSourceTypeEnum,
  ChildProcessStartUserEmptyTypeEnum,
  ChildProcessStartUserTypeEnum,
  DELAY_TYPE,
  DelayTypeEnum,
  TIME_UNIT_TYPES,
  TimeUnitType,
} from '../../consts';
import {
  parseFormFields,
  useFormFields,
  useNodeName,
  useWatchNode,
} from '../../helpers';
import { convertTimeUnit } from './utils';

defineOptions({ name: 'ChildProcessNodeConfig' });

const props = defineProps<{
  flowNode: SimpleFlowNode;
}>();

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
/** 节点名称配置 */
const { nodeName, showInput, clickIcon, changeNodeName, inputRef } =
  useNodeName(BpmNodeTypeEnum.CHILD_PROCESS_NODE);
// 激活的 Tab 标签页
const activeTabName = ref('child');
// 子流程表单配置
const formRef = ref(); // 表单 Ref
// 表单校验规则
const formRules: FormRules = reactive({
  async: [{ required: true, message: '是否异步不能为空', trigger: 'change' }],
  calledProcessDefinitionKey: [
    { required: true, message: '子流程不能为空', trigger: 'change' },
  ],
  skipStartUserNode: [
    {
      required: true,
      message: '是否自动跳过子流程发起节点不能为空',
      trigger: 'change',
    },
  ],
  startUserType: [
    { required: true, message: '子流程发起人不能为空', trigger: 'change' },
  ],
  startUserEmptyType: [
    {
      required: true,
      message: '当子流程发起人为空时不能为空',
      trigger: 'change',
    },
  ],
  startUserFormField: [
    { required: true, message: '子流程发起人字段不能为空', trigger: 'change' },
  ],
  timeoutEnable: [
    { required: true, message: '超时设置是否开启不能为空', trigger: 'change' },
  ],
  timeoutType: [
    { required: true, message: '超时设置时间不能为空', trigger: 'change' },
  ],
  timeDuration: [
    { required: true, message: '超时设置时间不能为空', trigger: 'change' },
  ],
  dateTime: [
    { required: true, message: '超时设置时间不能为空', trigger: 'change' },
  ],
  multiInstanceEnable: [
    { required: true, message: '多实例设置不能为空', trigger: 'change' },
  ],
  sequential: [
    { required: true, message: '是否串行不能为空', trigger: 'change' },
  ],
  multiInstanceSourceType: [
    { required: true, message: '实例数量不能为空', trigger: 'change' },
  ],
  approveRatio: [
    { required: true, message: '完成比例不能为空', trigger: 'change' },
  ],
});

type ChildProcessFormType = {
  approveRatio: number;
  async: boolean;
  calledProcessDefinitionKey: string;
  dateTime: string;
  inVariables?: IOParameter[];
  multiInstanceEnable: boolean;
  multiInstanceSource: string;
  multiInstanceSourceType: ChildProcessMultiInstanceSourceTypeEnum;
  outVariables?: IOParameter[];
  sequential: boolean;
  skipStartUserNode: boolean;
  startUserEmptyType: ChildProcessStartUserEmptyTypeEnum;
  startUserFormField: string;
  startUserType: ChildProcessStartUserTypeEnum;
  timeDuration: number;
  timeoutEnable: boolean;
  timeoutType: DelayTypeEnum;
  timeUnit: TimeUnitType;
};

const configForm = ref<ChildProcessFormType>({
  async: false,
  calledProcessDefinitionKey: '',
  skipStartUserNode: false,
  inVariables: [],
  outVariables: [],
  startUserType: ChildProcessStartUserTypeEnum.MAIN_PROCESS_START_USER,
  startUserEmptyType:
    ChildProcessStartUserEmptyTypeEnum.MAIN_PROCESS_START_USER,
  startUserFormField: '',
  timeoutEnable: false,
  timeoutType: DelayTypeEnum.FIXED_TIME_DURATION,
  timeDuration: 1,
  timeUnit: TimeUnitType.HOUR,
  dateTime: '',
  multiInstanceEnable: false,
  sequential: false,
  approveRatio: 100,
  multiInstanceSourceType:
    ChildProcessMultiInstanceSourceTypeEnum.FIXED_QUANTITY,
  multiInstanceSource: '',
});

const childProcessOptions = ref<any[]>([]);
// 主流程表单字段选项
const formFieldOptions = useFormFields();
/** 子流程发起人表单可选项 : 只有用户选择组件字段才能被选择 */
const startUserFormFieldOptions = computed(() => {
  return formFieldOptions.filter((item) => item.type === 'UserSelect');
});
// 数字表单字段选项
const digitalFormFieldOptions = computed(() => {
  return formFieldOptions.filter((item) => item.type === 'inputNumber');
});
// 多选表单字段选项
const multiFormFieldOptions = computed(() => {
  return formFieldOptions.filter(
    (item) => item.type === 'select' || item.type === 'checkbox',
  );
});
const childFormFieldOptions = ref<any[]>([]);

/** 保存配置 */
const saveConfig = async () => {
  activeTabName.value = 'child';
  if (!formRef.value) return false;
  const valid = await formRef.value.validate().catch(() => false);
  if (!valid) return false;

  const childInfo = childProcessOptions.value.find(
    (option) => option.key === configForm.value.calledProcessDefinitionKey,
  );

  currentNode.value.name = nodeName.value!;
  if (currentNode.value.childProcessSetting) {
    // 1. 是否异步
    currentNode.value.childProcessSetting.async = configForm.value.async;
    // 2. 调用流程
    currentNode.value.childProcessSetting.calledProcessDefinitionKey =
      childInfo.key;
    currentNode.value.childProcessSetting.calledProcessDefinitionName =
      childInfo.name;
    // 3. 是否跳过发起人
    currentNode.value.childProcessSetting.skipStartUserNode =
      configForm.value.skipStartUserNode;
    // 4. 主→子变量
    currentNode.value.childProcessSetting.inVariables =
      configForm.value.inVariables;
    // 5. 子→主变量
    currentNode.value.childProcessSetting.outVariables =
      configForm.value.outVariables;
    // 6. 发起人设置
    currentNode.value.childProcessSetting.startUserSetting.type =
      configForm.value.startUserType;
    currentNode.value.childProcessSetting.startUserSetting.emptyType =
      configForm.value.startUserEmptyType;
    currentNode.value.childProcessSetting.startUserSetting.formField =
      configForm.value.startUserFormField;
    // 7. 超时设置
    currentNode.value.childProcessSetting.timeoutSetting = {
      enable: configForm.value.timeoutEnable,
    };
    if (configForm.value.timeoutEnable) {
      currentNode.value.childProcessSetting.timeoutSetting.type =
        configForm.value.timeoutType;
      if (configForm.value.timeoutType === DelayTypeEnum.FIXED_TIME_DURATION) {
        currentNode.value.childProcessSetting.timeoutSetting.timeExpression =
          getIsoTimeDuration();
      }
      if (configForm.value.timeoutType === DelayTypeEnum.FIXED_DATE_TIME) {
        currentNode.value.childProcessSetting.timeoutSetting.timeExpression =
          configForm.value.dateTime;
      }
    }
    // 8. 多实例设置
    currentNode.value.childProcessSetting.multiInstanceSetting = {
      enable: configForm.value.multiInstanceEnable,
    };
    if (configForm.value.multiInstanceEnable) {
      currentNode.value.childProcessSetting.multiInstanceSetting.sequential =
        configForm.value.sequential;
      currentNode.value.childProcessSetting.multiInstanceSetting.approveRatio =
        configForm.value.approveRatio;
      currentNode.value.childProcessSetting.multiInstanceSetting.sourceType =
        configForm.value.multiInstanceSourceType;
      currentNode.value.childProcessSetting.multiInstanceSetting.source =
        configForm.value.multiInstanceSource;
    }
  }

  currentNode.value.showText = `调用子流程：${childInfo.name}`;
  drawerApi.close();
  return true;
};

// 显示子流程节点配置， 由父组件传过来
const showChildProcessNodeConfig = (node: SimpleFlowNode) => {
  nodeName.value = node.name;
  if (node.childProcessSetting) {
    // 1. 是否异步
    configForm.value.async = node.childProcessSetting.async;
    // 2. 调用流程
    configForm.value.calledProcessDefinitionKey =
      node.childProcessSetting?.calledProcessDefinitionKey;
    // 3. 是否跳过发起人
    configForm.value.skipStartUserNode =
      node.childProcessSetting.skipStartUserNode;
    // 4. 主流程→子流程变量
    configForm.value.inVariables = node.childProcessSetting.inVariables ?? [];
    // 5. 子流程→主流程变量
    configForm.value.outVariables = node.childProcessSetting.outVariables ?? [];
    // 6. 发起人设置
    configForm.value.startUserType =
      node.childProcessSetting.startUserSetting.type;
    configForm.value.startUserEmptyType =
      node.childProcessSetting.startUserSetting.emptyType ??
      ChildProcessStartUserEmptyTypeEnum.MAIN_PROCESS_START_USER;
    configForm.value.startUserFormField =
      node.childProcessSetting.startUserSetting.formField ?? '';
    // 7. 超时设置
    configForm.value.timeoutEnable =
      node.childProcessSetting.timeoutSetting.enable ?? false;
    if (configForm.value.timeoutEnable) {
      configForm.value.timeoutType =
        node.childProcessSetting.timeoutSetting.type ??
        DelayTypeEnum.FIXED_TIME_DURATION;
      // 固定时长
      if (configForm.value.timeoutType === DelayTypeEnum.FIXED_TIME_DURATION) {
        const strTimeDuration =
          node.childProcessSetting.timeoutSetting.timeExpression ?? '';
        const parseTime = strTimeDuration.slice(2, -1);
        const parseTimeUnit = strTimeDuration.slice(-1);
        configForm.value.timeDuration = Number.parseInt(parseTime);
        configForm.value.timeUnit = convertTimeUnit(parseTimeUnit);
      }
      // 固定日期时间
      if (configForm.value.timeoutType === DelayTypeEnum.FIXED_DATE_TIME) {
        configForm.value.dateTime =
          node.childProcessSetting.timeoutSetting.timeExpression ?? '';
      }
    }
    // 8. 多实例设置
    configForm.value.multiInstanceEnable =
      node.childProcessSetting.multiInstanceSetting.enable ?? false;
    if (configForm.value.multiInstanceEnable) {
      configForm.value.sequential =
        node.childProcessSetting.multiInstanceSetting.sequential ?? false;
      configForm.value.approveRatio =
        node.childProcessSetting.multiInstanceSetting.approveRatio ?? 100;
      configForm.value.multiInstanceSourceType =
        node.childProcessSetting.multiInstanceSetting.sourceType ??
        ChildProcessMultiInstanceSourceTypeEnum.FIXED_QUANTITY;
      configForm.value.multiInstanceSource =
        node.childProcessSetting.multiInstanceSetting.source ?? '';
    }
  }
  loadFormInfo();
  drawerApi.open();
};

/** 暴露方法给父组件 */
defineExpose({ showChildProcessNodeConfig });

const addVariable = (arr?: IOParameter[]) => {
  arr?.push({
    source: '',
    target: '',
  });
};

const deleteVariable = (index: number, arr?: IOParameter[]) => {
  arr?.splice(index, 1);
};

const handleCalledElementChange = () => {
  configForm.value.inVariables = [];
  configForm.value.outVariables = [];
  loadFormInfo();
};

const loadFormInfo = async () => {
  const childInfo = childProcessOptions.value.find(
    (option) => option.key === configForm.value.calledProcessDefinitionKey,
  );
  if (!childInfo) return;

  const formInfo = await getForm(childInfo.formId);
  childFormFieldOptions.value = [];
  if (formInfo.fields) {
    formInfo.fields.forEach((fieldStr: string) => {
      parseFormFields(JSON.parse(fieldStr), childFormFieldOptions.value);
    });
  }
};

const getIsoTimeDuration = () => {
  let strTimeDuration = 'PT';
  if (configForm.value.timeUnit === TimeUnitType.MINUTE) {
    strTimeDuration += `${configForm.value.timeDuration}M`;
  }
  if (configForm.value.timeUnit === TimeUnitType.HOUR) {
    strTimeDuration += `${configForm.value.timeDuration}H`;
  }
  if (configForm.value.timeUnit === TimeUnitType.DAY) {
    strTimeDuration += `${configForm.value.timeDuration}D`;
  }
  return strTimeDuration;
};

const handleMultiInstanceSourceTypeChange = () => {
  configForm.value.multiInstanceSource = '';
};

onMounted(async () => {
  try {
    childProcessOptions.value = await getModelList(undefined);
  } catch (error) {
    console.error('获取模型列表失败', error);
  }
});
</script>

<template>
  <Drawer class="w-1/3">
    <template #title>
      <div class="config-header">
        <ElInput
          v-if="showInput"
          ref="inputRef"
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
        <ElFormItem label="是否异步执行" prop="async">
          <ElSwitch
            v-model="configForm.async"
            active-text="是"
            inactive-text="否"
          />
        </ElFormItem>
        <ElFormItem label="选择子流程" prop="calledProcessDefinitionKey">
          <ElSelect
            v-model="configForm.calledProcessDefinitionKey"
            clearable
            @change="handleCalledElementChange"
          >
            <ElOption
              v-for="(item, index) in childProcessOptions"
              :key="index"
              :value="item.key"
              :label="item.name"
            />
          </ElSelect>
        </ElFormItem>
        <ElFormItem label="是否自动跳过子流程发起节点" prop="skipStartUserNode">
          <ElSwitch
            v-model="configForm.skipStartUserNode"
            active-text="跳过"
            inactive-text="不跳过"
          />
        </ElFormItem>
        <ElFormItem label="主→子变量传递" prop="inVariables">
          <div
            class="flex"
            v-for="(item, index) in configForm.inVariables"
            :key="index"
          >
            <div class="mr-2 mt-1">
              <ElFormItem
                :prop="['inVariables', index, 'source']"
                :rules="{
                  required: true,
                  message: '变量不能为空',
                  trigger: 'blur',
                }"
              >
                <ElSelect class="!w-40" v-model="item.source">
                  <ElOption
                    v-for="(field, fIdx) in formFieldOptions"
                    :key="fIdx"
                    :value="field.field"
                    :label="field.title"
                  />
                </ElSelect>
              </ElFormItem>
            </div>
            <div class="mr-2 mt-1">
              <ElFormItem
                :prop="['inVariables', index, 'target']"
                :rules="{
                  required: true,
                  message: '变量不能为空',
                  trigger: 'blur',
                }"
              >
                <ElSelect class="!w-40" v-model="item.target">
                  <ElOption
                    v-for="(field, fIdx) in childFormFieldOptions"
                    :key="fIdx"
                    :value="field.field"
                    :label="field.title"
                  />
                </ElSelect>
              </ElFormItem>
            </div>
            <div class="mr-1 mt-1 flex h-8 items-center">
              <IconifyIcon
                icon="lucide:trash-2"
                :size="18"
                class="cursor-pointer text-red-500"
                @click="deleteVariable(index, configForm.inVariables)"
              />
            </div>
          </div>
          <ElButton
            link
            @click="addVariable(configForm.inVariables)"
            class="mt-1 flex items-center"
          >
            <template #icon>
              <IconifyIcon class="size-4" icon="lucide:plus" />
            </template>
            添加一个
          </ElButton>
        </ElFormItem>
        <ElFormItem
          v-if="configForm.async === false"
          label="子→主变量传递"
          prop="outVariables"
        >
          <div
            class="flex"
            v-for="(item, index) in configForm.outVariables"
            :key="index"
          >
            <div class="mr-2 mt-1">
              <ElFormItem
                :prop="['outVariables', index, 'source']"
                :rules="{
                  required: true,
                  message: '变量不能为空',
                  trigger: 'blur',
                }"
              >
                <ElSelect class="!w-40" v-model="item.source">
                  <ElOption
                    v-for="(field, fIdx) in childFormFieldOptions"
                    :key="fIdx"
                    :value="field.field"
                    :label="field.title"
                  />
                </ElSelect>
              </ElFormItem>
            </div>
            <div class="mr-2 mt-1">
              <ElFormItem
                :prop="['outVariables', index, 'target']"
                :rules="{
                  required: true,
                  message: '变量不能为空',
                  trigger: 'blur',
                }"
              >
                <ElSelect class="!w-40" v-model="item.target">
                  <ElOption
                    v-for="(field, fIdx) in formFieldOptions"
                    :key="fIdx"
                    :value="field.field"
                    :label="field.title"
                  />
                </ElSelect>
              </ElFormItem>
            </div>
            <div class="mr-1 mt-1 flex h-8 items-center">
              <IconifyIcon
                icon="lucide:trash-2"
                :size="18"
                class="cursor-pointer text-red-500"
                @click="deleteVariable(index, configForm.outVariables)"
              />
            </div>
          </div>
          <ElButton
            link
            @click="addVariable(configForm.outVariables)"
            class="mt-1 flex items-center"
          >
            <template #icon>
              <IconifyIcon class="size-4" icon="lucide:plus" />
            </template>
            添加一个
          </ElButton>
        </ElFormItem>
        <ElFormItem label="子流程发起人" prop="startUserType">
          <ElRadioGroup v-model="configForm.startUserType">
            <ElRadio
              v-for="item in CHILD_PROCESS_START_USER_TYPE"
              :key="item.value"
              :value="item.value"
            >
              {{ item.label }}
            </ElRadio>
          </ElRadioGroup>
        </ElFormItem>
        <ElFormItem
          v-if="
            configForm.startUserType === ChildProcessStartUserTypeEnum.FROM_FORM
          "
          label="子流程发起人字段"
          prop="startUserFormField"
        >
          <ElSelect v-model="configForm.startUserFormField" clearable>
            <ElOption
              v-for="(field, fIdx) in startUserFormFieldOptions"
              :key="fIdx"
              :label="field.title"
              :value="field.field"
            />
          </ElSelect>
        </ElFormItem>
        <ElFormItem
          v-if="
            configForm.startUserType === ChildProcessStartUserTypeEnum.FROM_FORM
          "
          label="当子流程发起人为空时"
          prop="startUserEmptyType"
        >
          <ElRadioGroup v-model="configForm.startUserEmptyType">
            <ElRadio
              v-for="item in CHILD_PROCESS_START_USER_EMPTY_TYPE"
              :key="item.value"
              :value="item.value"
            >
              {{ item.label }}
            </ElRadio>
          </ElRadioGroup>
        </ElFormItem>

        <ElDivider>超时设置</ElDivider>
        <ElFormItem label="启用开关" prop="timeoutEnable">
          <ElSwitch
            v-model="configForm.timeoutEnable"
            active-text="开启"
            inactive-text="关闭"
          />
        </ElFormItem>
        <div v-if="configForm.timeoutEnable">
          <ElFormItem prop="timeoutType">
            <ElRadioGroup v-model="configForm.timeoutType">
              <ElRadioButton
                v-for="item in DELAY_TYPE"
                :key="item.value"
                :value="item.value"
              >
                {{ item.label }}
              </ElRadioButton>
            </ElRadioGroup>
          </ElFormItem>
          <ElFormItem
            v-if="configForm.timeoutType === DelayTypeEnum.FIXED_TIME_DURATION"
          >
            <div class="flex items-center gap-2">
              <span>当超过</span>
              <ElFormItem prop="timeDuration" class="!mb-0">
                <ElInputNumber
                  class="!w-24"
                  v-model="configForm.timeDuration"
                  :min="1"
                  controls-position="right"
                />
              </ElFormItem>
              <ElFormItem class="!mb-0">
                <ElSelect v-model="configForm.timeUnit" class="!w-24">
                  <ElOption
                    v-for="item in TIME_UNIT_TYPES"
                    :key="item.value"
                    :label="item.label"
                    :value="item.value"
                  >
                    {{ item.label }}
                  </ElOption>
                </ElSelect>
              </ElFormItem>
              <span>后进入下一节点</span>
            </div>
          </ElFormItem>
          <ElFormItem
            v-if="configForm.timeoutType === DelayTypeEnum.FIXED_DATE_TIME"
            prop="dateTime"
          >
            <div class="flex items-center gap-2">
              <ElDatePicker
                v-model="configForm.dateTime"
                type="datetime"
                placeholder="请选择日期和时间"
                value-format="YYYY-MM-DDTHH:mm:ss"
              />
              <span>后进入下一节点</span>
            </div>
          </ElFormItem>
        </div>

        <ElDivider>多实例设置</ElDivider>
        <ElFormItem label="启用开关" prop="multiInstanceEnable">
          <ElSwitch
            v-model="configForm.multiInstanceEnable"
            active-text="开启"
            inactive-text="关闭"
          />
        </ElFormItem>
        <div v-if="configForm.multiInstanceEnable">
          <ElFormItem prop="sequential" label="是否串行">
            <ElSwitch
              v-model="configForm.sequential"
              active-text="是"
              inactive-text="否"
            />
          </ElFormItem>
          <ElFormItem prop="approveRatio" label="完成比例(%)">
            <ElInputNumber
              v-model="configForm.approveRatio"
              :min="10"
              :max="100"
              :step="10"
            />
          </ElFormItem>
          <ElFormItem prop="multiInstanceSourceType" label="实例数量">
            <ElSelect
              v-model="configForm.multiInstanceSourceType"
              @change="handleMultiInstanceSourceTypeChange"
            >
              <ElOption
                v-for="item in CHILD_PROCESS_MULTI_INSTANCE_SOURCE_TYPE"
                :key="item.value"
                :label="item.label"
                :value="item.value"
              >
                {{ item.label }}
              </ElOption>
            </ElSelect>
          </ElFormItem>
          <ElFormItem
            v-if="
              configForm.multiInstanceSourceType ===
              ChildProcessMultiInstanceSourceTypeEnum.FIXED_QUANTITY
            "
            prop="multiInstanceSource"
            label="固定数量"
            :rules="{
              required: true,
              message: '固定数量不能为空',
              trigger: 'change',
            }"
          >
            <ElInputNumber v-model="configForm.multiInstanceSource" :min="1" />
          </ElFormItem>
          <ElFormItem
            v-if="
              configForm.multiInstanceSourceType ===
              ChildProcessMultiInstanceSourceTypeEnum.NUMBER_FORM
            "
            prop="multiInstanceSource"
            label="数字表单"
            :rules="{
              required: true,
              message: '数字表单字段不能为空',
              trigger: 'change',
            }"
          >
            <ElSelect v-model="configForm.multiInstanceSource">
              <ElOption
                v-for="(field, fIdx) in digitalFormFieldOptions"
                :key="fIdx"
                :label="field.title"
                :value="field.field"
              >
                {{ field.title }}
              </ElOption>
            </ElSelect>
          </ElFormItem>
          <ElFormItem
            v-if="
              configForm.multiInstanceSourceType ===
              ChildProcessMultiInstanceSourceTypeEnum.MULTIPLE_FORM
            "
            prop="multiInstanceSource"
            label="多选表单"
            :rules="{
              required: true,
              message: '多选表单字段不能为空',
              trigger: 'change',
            }"
          >
            <ElSelect v-model="configForm.multiInstanceSource">
              <ElOption
                v-for="(field, fIdx) in multiFormFieldOptions"
                :key="fIdx"
                :label="field.title"
                :value="field.field"
              >
                {{ field.title }}
              </ElOption>
            </ElSelect>
          </ElFormItem>
        </div>
      </ElForm>
    </div>
  </Drawer>
</template>

<style scoped></style>
