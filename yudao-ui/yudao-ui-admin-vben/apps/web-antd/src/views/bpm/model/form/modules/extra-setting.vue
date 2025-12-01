<script setup lang="ts">
import { computed, provide, ref, watch } from 'vue';

import { useVbenModal } from '@vben/common-ui';
import {
  BpmAutoApproveType,
  BpmModelFormType,
  ProcessVariableEnum,
} from '@vben/constants';
import { IconifyIcon } from '@vben/icons';

import {
  Button,
  Checkbox,
  Col,
  Form,
  FormItem,
  Input,
  InputNumber,
  Mentions,
  Radio,
  RadioGroup,
  Row,
  Select,
  Switch,
  Tooltip,
  TypographyText,
} from 'ant-design-vue';
import dayjs from 'dayjs';

import { getForm } from '#/api/bpm/form';
import {
  HttpRequestSetting,
  parseFormFields,
} from '#/views/bpm/components/simple-process-design';

import PrintTemplate from './custom-print-template.vue';

const modelData = defineModel<any>();

/** 自定义 ID 流程编码 */
const timeOptions = ref([
  {
    value: '',
    label: '无',
  },
  {
    value: 'DAY',
    label: '精确到日',
  },
  {
    value: 'HOUR',
    label: '精确到时',
  },
  {
    value: 'MINUTE',
    label: '精确到分',
  },
  {
    value: 'SECOND',
    label: '精确到秒',
  },
]);
const numberExample = computed(() => {
  if (modelData.value.processIdRule.enable) {
    let infix = '';
    switch (modelData.value.processIdRule.infix) {
      case 'DAY': {
        infix = dayjs().format('YYYYMMDD');
        break;
      }
      case 'HOUR': {
        infix = dayjs().format('YYYYMMDDHH');
        break;
      }
      case 'MINUTE': {
        infix = dayjs().format('YYYYMMDDHHmm');
        break;
      }
      case 'SECOND': {
        infix = dayjs().format('YYYYMMDDHHmmss');
        break;
      }
      default: {
        break;
      }
    }
    return (
      modelData.value.processIdRule.prefix +
      infix +
      modelData.value.processIdRule.postfix +
      '1'.padStart(modelData.value.processIdRule.length - 1, '0')
    );
  } else {
    return '';
  }
});

/** 是否开启流程前置通知 */
const processBeforeTriggerEnable = ref(false);
function handleProcessBeforeTriggerEnableChange(
  val: boolean | number | string,
) {
  modelData.value.processBeforeTriggerSetting = val
    ? {
        url: '',
        header: [],
        body: [],
        response: [],
      }
    : null;
}

/** 是否开启流程后置通知 */
const processAfterTriggerEnable = ref(false);
function handleProcessAfterTriggerEnableChange(val: boolean | number | string) {
  modelData.value.processAfterTriggerSetting = val
    ? {
        url: '',
        header: [],
        body: [],
        response: [],
      }
    : null;
}

/** 是否开启任务前置通知 */
const taskBeforeTriggerEnable = ref(false);
function handleTaskBeforeTriggerEnableChange(val: boolean | number | string) {
  modelData.value.taskBeforeTriggerSetting = val
    ? {
        url: '',
        header: [],
        body: [],
        response: [],
      }
    : null;
}

/** 是否开启任务后置通知 */
const taskAfterTriggerEnable = ref(false);
function handleTaskAfterTriggerEnableChange(val: boolean | number | string) {
  modelData.value.taskAfterTriggerSetting = val
    ? {
        url: '',
        header: [],
        body: [],
        response: [],
      }
    : null;
}

/** 表单字段 */
const formFields = ref<Array<{ field: string; title: string }>>([]);
const formFieldOptions4Title = computed(() => {
  const cloneFormField = formFields.value.map((item) => {
    return {
      label: item.title,
      value: item.field,
    };
  });
  // 固定添加发起人 ID 字段
  cloneFormField.unshift({
    label: '流程名称',
    value: ProcessVariableEnum.PROCESS_DEFINITION_NAME,
  });
  cloneFormField.unshift({
    label: '发起时间',
    value: ProcessVariableEnum.START_TIME,
  });
  cloneFormField.unshift({
    label: '发起人',
    value: ProcessVariableEnum.START_USER_ID,
  });
  return cloneFormField;
});
const formFieldOptions4Summary = computed(() => {
  return formFields.value.map((item) => {
    return {
      label: item.title,
      value: item.field,
    };
  });
});
const unParsedFormFields = ref<string[]>([]); // 未解析的表单字段
provide('formFields', unParsedFormFields); // 暴露给子组件 HttpRequestSetting 使用

/** 兼容以前未配置更多设置的流程 */
function initData() {
  if (!modelData.value.processIdRule) {
    modelData.value.processIdRule = {
      enable: false,
      prefix: '',
      infix: '',
      postfix: '',
      length: 5,
    };
  }
  if (!modelData.value.printTemplateSetting) {
    modelData.value.printTemplateSetting = {
      enable: false,
      template: '',
    };
  }
  if (!modelData.value.autoApprovalType) {
    modelData.value.autoApprovalType = BpmAutoApproveType.NONE;
  }
  if (!modelData.value.titleSetting) {
    modelData.value.titleSetting = {
      enable: false,
      title: '',
    };
  }
  if (!modelData.value.summarySetting) {
    modelData.value.summarySetting = {
      enable: false,
      summary: [],
    };
  }
  if (modelData.value.processBeforeTriggerSetting) {
    processBeforeTriggerEnable.value = true;
  }
  if (modelData.value.processAfterTriggerSetting) {
    processAfterTriggerEnable.value = true;
  }
  if (modelData.value.taskBeforeTriggerSetting) {
    taskBeforeTriggerEnable.value = true;
  }
  if (modelData.value.taskAfterTriggerSetting) {
    taskAfterTriggerEnable.value = true;
  }
  if (modelData.value.allowWithdrawTask === undefined) {
    modelData.value.allowWithdrawTask = false;
  }
}

/** 监听表单 ID 变化，加载表单数据 */
watch(
  () => modelData.value.formId,
  async (newFormId) => {
    if (newFormId && modelData.value.formType === BpmModelFormType.NORMAL) {
      const data = await getForm(newFormId);
      const result: Array<{ field: string; title: string }> = [];
      if (data.fields) {
        unParsedFormFields.value = data.fields;
        data.fields.forEach((fieldStr: string) => {
          parseFormFields(JSON.parse(fieldStr), result);
        });
      }
      formFields.value = result;
    } else {
      formFields.value = [];
      unParsedFormFields.value = [];
    }
  },
  { immediate: true },
);
const formRef = ref(); // 表单引用

/** 表单校验 */
async function validate() {
  await formRef.value?.validate();
}

/** 自定义打印模板模态框 */
const [PrintTemplateModal, printTemplateModalApi] = useVbenModal({
  connectedComponent: PrintTemplate,
  destroyOnClose: true,
});

/** 弹出自定义打印模板弹窗 */
function openPrintTemplateModal() {
  printTemplateModalApi
    .setData({ template: modelData.value.printTemplateSetting.template })
    .open();
}

/** 默认的打印模板， 目前自定义模板没有引入自定义样式。 看后续是否需要 */
const defaultTemplate = `<p style="text-align: center;font-size: 1.25rem;"><strong><span data-w-e-type="mention" data-value="流程名称" data-info="%7B%22id%22%3A%22processName%22%7D">@流程名称</span></strong></p>
<p style="text-align: right;">打印人员：<span data-w-e-type="mention" data-info="%7B%22id%22%3A%22printUser%22%7D">@打印人</span></p>
<p style="text-align: left;">流程编号：<span data-w-e-type="mention" data-value="流程编号" data-info="%7B%22id%22%3A%22processNum%22%7D">@流程编号</span></p>
<p>&nbsp;</p>
<table style="width: 100%; height: 72.2159px;">
<tbody>
<tr style="height: 36.108px;">
<td style="width: 21.7532%; border: 1px solid;" colspan="1" rowspan="1" width="auto">发起人</td>
<td style="width: 30.5551%; border: 1px solid;" colspan="1" rowspan="1" width="auto"><span data-w-e-type="mention" data-value="发起人" data-info="%7B%22id%22%3A%22startUser%22%7D">@发起人</span></td>
<td style="width: 21.7532%; border: 1px solid;" colspan="1" rowspan="1" width="auto">发起时间</td>
<td style="width: 26.0284%; border: 1px solid;" colspan="1" rowspan="1" width="auto"><span data-w-e-type="mention" data-value="发起时间" data-info="%7B%22id%22%3A%22startTime%22%7D">@发起时间</span></td>
</tr>
<tr style="height: 36.108px;">
<td style="width: 21.7532%; border: 1px solid;" colspan="1" rowspan="1" width="auto">所属部门</td>
<td style="width: 30.5551%; border: 1px solid;" colspan="1" rowspan="1" width="auto"><span data-w-e-type="mention" data-w-e-is-void="" data-w-e-is-inline="" data-value="发起人部门" data-info="%7B%22id%22%3A%22startUserDept%22%7D">@发起人部门</span></td>
<td style="width: 21.7532%; border: 1px solid;" colspan="1" rowspan="1" width="auto">流程状态</td>
<td style="width: 26.0284%; border: 1px solid;" colspan="1" rowspan="1" width="auto"><span data-w-e-type="mention" data-value="流程状态" data-info="%7B%22id%22%3A%22processStatus%22%7D">@流程状态</span></td>
</tr>
</tbody>
</table>
<p>&nbsp;</p>
<div contenteditable="false" data-w-e-type="process-record" data-w-e-is-void="">
<table class="process-record-table" style="width: 100%; border-collapse: collapse; border: 1px solid;">
<tr>
	<td style="width: 100%; border: 1px solid; text-align: center;" colspan="2">流程记录</td>
</tr>
<tr>
	<td style="width: 25%; border: 1px solid;">节点</td>
	<td style="width: 75%; border: 1px solid;">操作</td>
</tr>
</table>
</div>
<p>&nbsp;</p>`;

const printTemplateEnable = computed<boolean>({
  get() {
    return !!modelData.value?.printTemplateSetting?.enable;
  },
  set(val: boolean) {
    if (!modelData.value.printTemplateSetting) {
      modelData.value.printTemplateSetting = {
        enable: false,
        template: '',
      };
    }
    modelData.value.printTemplateSetting.enable = val;
  },
}); // 自定义打印模板开关

function handlePrintTemplateEnableChange(checked: any) {
  const val = !!checked;
  if (val && !modelData.value.printTemplateSetting.template) {
    modelData.value.printTemplateSetting.template = defaultTemplate;
  }
}

function confirmPrintTemplate(template: string) {
  modelData.value.printTemplateSetting.template = template;
}

defineExpose({ initData, validate });
</script>
<template>
  <Form
    ref="formRef"
    :model="modelData"
    :label-col="{ span: 4 }"
    :wrapper-col="{ span: 20 }"
    class="mt-5 px-5"
  >
    <FormItem class="mb-5" label="提交人权限">
      <div class="mt-1 flex flex-col">
        <Checkbox v-model:checked="modelData.allowCancelRunningProcess">
          允许撤销审批中的申请
        </Checkbox>
      </div>
    </FormItem>
    <FormItem class="mb-5" label="审批人权限">
      <div class="mt-1 flex flex-col">
        <Checkbox v-model:checked="modelData.allowWithdrawTask">
          允许审批人撤回任务
        </Checkbox>
        <div class="ml-6">
          <TypographyText type="secondary">
            审批人可撤回正在审批节点的前一节点
          </TypographyText>
        </div>
      </div>
    </FormItem>
    <FormItem v-if="modelData.processIdRule" class="mb-5" label="流程编码">
      <Row :gutter="8" align="middle">
        <Col :span="1">
          <Checkbox v-model:checked="modelData.processIdRule.enable" />
        </Col>
        <Col :span="5">
          <Input
            v-model:value="modelData.processIdRule.prefix"
            placeholder="前缀"
            :disabled="!modelData.processIdRule.enable"
          />
        </Col>
        <Col :span="6">
          <Select
            v-model:value="modelData.processIdRule.infix"
            allow-clear
            placeholder="中缀"
            :disabled="!modelData.processIdRule.enable"
            :options="timeOptions"
          />
        </Col>
        <Col :span="4">
          <Input
            v-model:value="modelData.processIdRule.postfix"
            placeholder="后缀"
            :disabled="!modelData.processIdRule.enable"
          />
        </Col>
        <Col :span="4">
          <InputNumber
            v-model:value="modelData.processIdRule.length"
            :min="5"
            :disabled="!modelData.processIdRule.enable"
          />
        </Col>
      </Row>
      <div class="ml-6 mt-2" v-if="modelData.processIdRule.enable">
        <TypographyText type="success">
          编码示例：{{ numberExample }}
        </TypographyText>
      </div>
    </FormItem>
    <FormItem class="mb-5" label="自动去重">
      <div class="mt-1">
        <TypographyText class="mb-2 block">
          同一审批人在流程中重复出现时：
        </TypographyText>
        <RadioGroup v-model:value="modelData.autoApprovalType">
          <Row :gutter="[0, 8]">
            <Col :span="24">
              <Radio :value="0">不自动通过</Radio>
            </Col>
            <Col :span="24">
              <Radio :value="1">
                仅审批一次，后续重复的审批节点均自动通过
              </Radio>
            </Col>
            <Col :span="24">
              <Radio :value="2">仅针对连续审批的节点自动通过</Radio>
            </Col>
          </Row>
        </RadioGroup>
      </div>
    </FormItem>
    <FormItem v-if="modelData.titleSetting" class="mb-5" label="标题设置">
      <div class="mt-1">
        <RadioGroup v-model:value="modelData.titleSetting.enable">
          <Row :gutter="[0, 8]">
            <Col :span="24">
              <Radio :value="false">
                系统默认
                <TypographyText type="success"> 展示流程名称 </TypographyText>
              </Radio>
            </Col>
            <Col :span="24">
              <Radio :value="true">
                <div class="inline-flex items-center">
                  自定义标题
                  <Tooltip
                    title="输入字符 '{' 即可插入表单字段"
                    placement="top"
                  >
                    <IconifyIcon
                      icon="lucide:circle-help"
                      class="ml-1 size-4 text-gray-500"
                    />
                  </Tooltip>
                </div>
              </Radio>
            </Col>
          </Row>
        </RadioGroup>
        <div class="mt-2">
          <Mentions
            v-if="modelData.titleSetting.enable"
            v-model:value="modelData.titleSetting.title"
            style="width: 100%; max-width: 600px"
            type="textarea"
            prefix="{"
            split="}"
            :options="formFieldOptions4Title"
            placeholder="请插入表单字段（输入 '{' 可以选择表单字段）或输入文本"
          />
        </div>
      </div>
    </FormItem>
    <FormItem
      v-if="
        modelData.summarySetting &&
        modelData.formType === BpmModelFormType.NORMAL
      "
      class="mb-5"
      label="摘要设置"
    >
      <div class="mt-1">
        <RadioGroup v-model:value="modelData.summarySetting.enable">
          <Row :gutter="[0, 8]">
            <Col :span="24">
              <Radio :value="false">
                系统默认
                <TypographyText type="secondary">
                  展示表单前 3 个字段
                </TypographyText>
              </Radio>
            </Col>
            <Col :span="24">
              <Radio :value="true"> 自定义摘要 </Radio>
            </Col>
          </Row>
        </RadioGroup>
        <div class="mt-2">
          <Select
            v-if="modelData.summarySetting.enable"
            v-model:value="modelData.summarySetting.summary"
            mode="multiple"
            placeholder="请选择要展示的表单字段"
            :options="formFieldOptions4Summary"
          />
        </div>
      </div>
    </FormItem>
    <FormItem class="mb-5" label="流程前置通知">
      <Row class="mt-1">
        <Col :span="24">
          <div class="flex items-center">
            <Switch
              v-model:checked="processBeforeTriggerEnable"
              @change="handleProcessBeforeTriggerEnableChange"
            />
            <span class="ml-4">流程启动后通知</span>
          </div>
        </Col>
      </Row>
      <Row v-if="processBeforeTriggerEnable">
        <Col :span="24" class="mt-6">
          <HttpRequestSetting
            v-model:setting="modelData.processBeforeTriggerSetting"
            :response-enable="true"
            form-item-prefix="processBeforeTriggerSetting"
          />
        </Col>
      </Row>
    </FormItem>
    <FormItem class="mb-5" label="流程后置通知">
      <Row class="mt-1">
        <Col :span="24">
          <div class="flex items-center">
            <Switch
              v-model:checked="processAfterTriggerEnable"
              @change="handleProcessAfterTriggerEnableChange"
            />
            <span class="ml-4">流程结束后通知</span>
          </div>
        </Col>
      </Row>
      <Row v-if="processAfterTriggerEnable" class="mt-2">
        <Col :span="24">
          <HttpRequestSetting
            v-model:setting="modelData.processAfterTriggerSetting"
            :response-enable="true"
            form-item-prefix="processAfterTriggerSetting"
          />
        </Col>
      </Row>
    </FormItem>
    <FormItem class="mb-5" label="任务前置通知">
      <Row class="mt-1">
        <Col :span="24">
          <div class="flex items-center">
            <Switch
              v-model:checked="taskBeforeTriggerEnable"
              @change="handleTaskBeforeTriggerEnableChange"
            />
            <span class="ml-4">任务执行时通知</span>
          </div>
        </Col>
      </Row>
      <Row v-if="taskBeforeTriggerEnable" class="mt-2">
        <Col :span="24">
          <HttpRequestSetting
            v-model:setting="modelData.taskBeforeTriggerSetting"
            :response-enable="true"
            form-item-prefix="taskBeforeTriggerSetting"
          />
        </Col>
      </Row>
    </FormItem>
    <FormItem class="mb-5" label="任务后置通知">
      <Row class="mt-1">
        <Col :span="24">
          <div class="flex items-center">
            <Switch
              v-model:checked="taskAfterTriggerEnable"
              @change="handleTaskAfterTriggerEnableChange"
            />
            <span class="ml-4">任务结束后通知</span>
          </div>
        </Col>
      </Row>
      <Row v-if="taskAfterTriggerEnable" class="mt-2">
        <Col :span="24">
          <HttpRequestSetting
            v-model:setting="modelData.taskAfterTriggerSetting"
            :response-enable="true"
            form-item-prefix="taskAfterTriggerSetting"
          />
        </Col>
      </Row>
    </FormItem>
    <FormItem class="mb-5" label="自定义打印模板">
      <div class="flex w-full flex-col">
        <div class="flex items-center">
          <Switch
            v-model:checked="printTemplateEnable"
            @change="handlePrintTemplateEnableChange"
          />
          <Button
            v-if="printTemplateEnable"
            class="ml-2 flex items-center"
            type="link"
            @click="openPrintTemplateModal"
          >
            <template #icon>
              <IconifyIcon icon="lucide:pencil" />
            </template>
            编辑模板
          </Button>
        </div>
      </div>
    </FormItem>
    <PrintTemplateModal
      :form-fields="formFields"
      @confirm="confirmPrintTemplate"
    />
  </Form>
</template>
