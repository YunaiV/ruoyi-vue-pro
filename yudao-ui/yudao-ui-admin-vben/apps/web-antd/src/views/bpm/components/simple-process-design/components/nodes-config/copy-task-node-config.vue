<script setup lang="ts">
import type { Rule } from 'ant-design-vue/es/form';

import type { Ref } from 'vue';

import type { SimpleFlowNode } from '../../consts';
import type { CopyTaskFormType } from '../../helpers';

import { computed, onMounted, reactive, ref } from 'vue';

import { useVbenDrawer } from '@vben/common-ui';
import { BpmModelFormType, BpmNodeTypeEnum } from '@vben/constants';
import { IconifyIcon } from '@vben/icons';

import {
  Col,
  Form,
  FormItem,
  Input,
  Radio,
  RadioGroup,
  Row,
  Select,
  SelectOption,
  TabPane,
  Tabs,
  Textarea,
  TreeSelect,
} from 'ant-design-vue';

import {
  CANDIDATE_STRATEGY,
  CandidateStrategy,
  FieldPermissionType,
  MULTI_LEVEL_DEPT,
} from '../../consts';
import {
  useFormFieldsPermission,
  useNodeForm,
  useNodeName,
  useWatchNode,
} from '../../helpers';

defineOptions({ name: 'CopyTaskNodeConfig' });

const props = defineProps({
  flowNode: {
    type: Object as () => SimpleFlowNode,
    required: true,
  },
});

const deptLevelLabel = computed(() => {
  let label = '部门负责人来源';
  label =
    configForm.value.candidateStrategy ===
    CandidateStrategy.MULTI_LEVEL_DEPT_LEADER
      ? `${label}(指定部门向上)`
      : `${label}(发起人部门向上)`;
  return label;
});

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
  useNodeName(BpmNodeTypeEnum.COPY_TASK_NODE);

// 激活的 Tab 标签页
const activeTabName = ref('user');

// 表单字段权限配置
const {
  formType,
  fieldsPermissionConfig,
  formFieldOptions,
  getNodeConfigFormFields,
} = useFormFieldsPermission(FieldPermissionType.READ);

// 表单内用户字段选项, 必须是必填和用户选择器
const userFieldOnFormOptions = computed(() => {
  return formFieldOptions.filter((item) => item.type === 'UserSelect');
});

// 表单内部门字段选项, 必须是必填和部门选择器
const deptFieldOnFormOptions = computed(() => {
  return formFieldOptions.filter((item) => item.type === 'DeptSelect');
});

// 抄送人表单配置
const formRef = ref(); // 表单 Ref

// 表单校验规则
const formRules: Record<string, Rule[]> = reactive({
  candidateStrategy: [
    { required: true, message: '抄送人设置不能为空', trigger: 'change' },
  ],
  userIds: [{ required: true, message: '用户不能为空', trigger: 'change' }],
  roleIds: [{ required: true, message: '角色不能为空', trigger: 'change' }],
  deptIds: [{ required: true, message: '部门不能为空', trigger: 'change' }],
  userGroups: [
    { required: true, message: '用户组不能为空', trigger: 'change' },
  ],
  postIds: [{ required: true, message: '岗位不能为空', trigger: 'change' }],
  formUser: [
    { required: true, message: '表单内用户字段不能为空', trigger: 'change' },
  ],
  formDept: [
    { required: true, message: '表单内部门字段不能为空', trigger: 'change' },
  ],
  expression: [
    { required: true, message: '流程表达式不能为空', trigger: 'blur' },
  ],
});

const {
  configForm: tempConfigForm,
  roleOptions,
  postOptions,
  userOptions,
  userGroupOptions,
  deptTreeOptions,
  getShowText,
  handleCandidateParam,
  parseCandidateParam,
} = useNodeForm(BpmNodeTypeEnum.COPY_TASK_NODE);

const configForm = tempConfigForm as Ref<CopyTaskFormType>;
// 抄送人策略， 去掉发起人自选 和 发起人自己
const copyUserStrategies = computed(() => {
  return CANDIDATE_STRATEGY.filter(
    (item) => item.value !== CandidateStrategy.START_USER,
  );
});

// 改变抄送人设置策略
function changeCandidateStrategy() {
  configForm.value.userIds = [];
  configForm.value.deptIds = [];
  configForm.value.roleIds = [];
  configForm.value.postIds = [];
  configForm.value.userGroups = [];
  configForm.value.deptLevel = 1;
  configForm.value.formUser = '';
  configForm.value.formDept = '';
}

// 保存配置
async function saveConfig() {
  activeTabName.value = 'user';
  if (!formRef.value) return false;
  const valid = await formRef.value.validate();
  if (!valid) return false;
  const showText = getShowText();
  if (!showText) return false;
  currentNode.value.name = nodeName.value!;
  currentNode.value.candidateParam = handleCandidateParam();
  currentNode.value.candidateStrategy = configForm.value.candidateStrategy;
  currentNode.value.showText = showText;
  currentNode.value.fieldsPermission = fieldsPermissionConfig.value;
  drawerApi.close();
  return true;
}

// 显示抄送节点配置， 由父组件传过来
function showCopyTaskNodeConfig(node: SimpleFlowNode) {
  nodeName.value = node.name;
  // 抄送人设置
  configForm.value.candidateStrategy = node.candidateStrategy!;
  parseCandidateParam(node.candidateStrategy!, node?.candidateParam);
  // 表单字段权限
  getNodeConfigFormFields(node.fieldsPermission);

  drawerApi.open();
}

/** 批量更新权限 */
function updatePermission(type: string) {
  fieldsPermissionConfig.value.forEach((field) => {
    if (type === 'READ') {
      field.permission = FieldPermissionType.READ;
    } else if (type === 'WRITE') {
      field.permission = FieldPermissionType.WRITE;
    } else {
      field.permission = FieldPermissionType.NONE;
    }
  });
}

// 在组件初始化时对表单字段进行处理
onMounted(() => {
  // 可以在这里进行初始化操作
});

defineExpose({ showCopyTaskNodeConfig }); // 暴露方法给父组件
</script>
<template>
  <Drawer class="w-2/5">
    <template #title>
      <div class="config-header">
        <Input
          v-if="showInput"
          ref="inputRef"
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
    <Tabs v-model:active-key="activeTabName">
      <TabPane tab="抄送人" key="user">
        <div>
          <Form
            ref="formRef"
            :model="configForm"
            :label-col="{ span: 24 }"
            :wrapper-col="{ span: 24 }"
            :rules="formRules"
          >
            <FormItem label="抄送人设置" name="candidateStrategy">
              <RadioGroup
                v-model:value="configForm.candidateStrategy"
                @change="changeCandidateStrategy"
              >
                <Row :gutter="[0, 8]">
                  <Col
                    v-for="(dict, index) in copyUserStrategies"
                    :key="index"
                    :span="8"
                  >
                    <Radio :value="dict.value" :label="dict.value">
                      {{ dict.label }}
                    </Radio>
                  </Col>
                </Row>
              </RadioGroup>
            </FormItem>

            <FormItem
              v-if="configForm.candidateStrategy === CandidateStrategy.ROLE"
              label="指定角色"
              name="roleIds"
            >
              <Select
                v-model:value="configForm.roleIds"
                clearable
                mode="multiple"
              >
                <SelectOption
                  v-for="item in roleOptions"
                  :key="item.id"
                  :label="item.name"
                  :value="item.id"
                >
                  {{ item.name }}
                </SelectOption>
              </Select>
            </FormItem>
            <FormItem
              v-if="
                configForm.candidateStrategy ===
                  CandidateStrategy.DEPT_MEMBER ||
                configForm.candidateStrategy ===
                  CandidateStrategy.DEPT_LEADER ||
                configForm.candidateStrategy ===
                  CandidateStrategy.MULTI_LEVEL_DEPT_LEADER
              "
              label="指定部门"
              name="deptIds"
            >
              <TreeSelect
                v-model:value="configForm.deptIds"
                :tree-data="deptTreeOptions"
                :field-names="{
                  label: 'name',
                  value: 'id',
                  children: 'children',
                }"
                empty-text="加载中，请稍后"
                multiple
                :check-strictly="true"
                allow-clear
                tree-checkable
              />
            </FormItem>
            <FormItem
              v-if="configForm.candidateStrategy === CandidateStrategy.POST"
              label="指定岗位"
              name="postIds"
            >
              <Select
                v-model:value="configForm.postIds"
                clearable
                mode="multiple"
              >
                <SelectOption
                  v-for="item in postOptions"
                  :key="item.id"
                  :label="item.name"
                  :value="item.id!"
                >
                  {{ item.name }}
                </SelectOption>
              </Select>
            </FormItem>
            <FormItem
              v-if="configForm.candidateStrategy === CandidateStrategy.USER"
              label="指定用户"
              name="userIds"
            >
              <Select
                v-model:value="configForm.userIds"
                clearable
                mode="multiple"
              >
                <SelectOption
                  v-for="item in userOptions"
                  :key="item.id"
                  :label="item.nickname"
                  :value="item.id"
                >
                  {{ item.nickname }}
                </SelectOption>
              </Select>
            </FormItem>
            <FormItem
              v-if="
                configForm.candidateStrategy === CandidateStrategy.USER_GROUP
              "
              label="指定用户组"
              name="userGroups"
            >
              <Select
                v-model:value="configForm.userGroups"
                clearable
                mode="multiple"
              >
                <SelectOption
                  v-for="item in userGroupOptions"
                  :key="item.id"
                  :label="item.name"
                  :value="item.id"
                >
                  {{ item.name }}
                </SelectOption>
              </Select>
            </FormItem>
            <FormItem
              v-if="
                configForm.candidateStrategy === CandidateStrategy.FORM_USER
              "
              label="表单内用户字段"
              name="formUser"
            >
              <Select v-model:value="configForm.formUser" clearable>
                <SelectOption
                  v-for="(item, idx) in userFieldOnFormOptions"
                  :key="idx"
                  :label="item.title"
                  :value="item.field"
                  :disabled="!item.required"
                >
                  {{ item.title }}
                </SelectOption>
              </Select>
            </FormItem>
            <FormItem
              v-if="
                configForm.candidateStrategy ===
                CandidateStrategy.FORM_DEPT_LEADER
              "
              label="表单内部门字段"
              name="formDept"
            >
              <Select v-model:value="configForm.formDept" clearable>
                <SelectOption
                  v-for="(item, idx) in deptFieldOnFormOptions"
                  :key="idx"
                  :label="item.title"
                  :value="item.field"
                  :disabled="!item.required"
                >
                  {{ item.title }}
                </SelectOption>
              </Select>
            </FormItem>
            <FormItem
              v-if="
                configForm.candidateStrategy ===
                  CandidateStrategy.MULTI_LEVEL_DEPT_LEADER ||
                configForm.candidateStrategy ===
                  CandidateStrategy.START_USER_DEPT_LEADER ||
                configForm.candidateStrategy ===
                  CandidateStrategy.START_USER_MULTI_LEVEL_DEPT_LEADER ||
                configForm.candidateStrategy ===
                  CandidateStrategy.FORM_DEPT_LEADER
              "
              :label="deptLevelLabel!"
              name="deptLevel"
            >
              <Select v-model:value="configForm.deptLevel" clearable>
                <SelectOption
                  v-for="(item, index) in MULTI_LEVEL_DEPT"
                  :key="index"
                  :label="item.label"
                  :value="item.value"
                >
                  {{ item.label }}
                </SelectOption>
              </Select>
            </FormItem>
            <FormItem
              v-if="
                configForm.candidateStrategy === CandidateStrategy.EXPRESSION
              "
              label="流程表达式"
              name="expression"
            >
              <Textarea v-model:value="configForm.expression" clearable />
            </FormItem>
          </Form>
        </div>
      </TabPane>
      <TabPane
        tab="表单字段权限"
        key="fields"
        v-if="formType === BpmModelFormType.NORMAL"
      >
        <div class="p-1">
          <div class="mb-4 text-base font-bold">字段权限</div>

          <!-- 表头 -->
          <Row class="border border-gray-200 px-4 py-3">
            <Col :span="8" class="font-bold">字段名称</Col>
            <Col :span="16">
              <Row>
                <Col :span="8" class="flex items-center justify-center">
                  <span
                    class="cursor-pointer font-bold"
                    @click="updatePermission('READ')"
                  >
                    只读
                  </span>
                </Col>
                <Col :span="8" class="flex items-center justify-center">
                  <span
                    class="cursor-pointer font-bold"
                    @click="updatePermission('WRITE')"
                  >
                    可编辑
                  </span>
                </Col>
                <Col :span="8" class="flex items-center justify-center">
                  <span
                    class="cursor-pointer font-bold"
                    @click="updatePermission('NONE')"
                  >
                    隐藏
                  </span>
                </Col>
              </Row>
            </Col>
          </Row>

          <!-- 表格内容 -->
          <div v-for="(item, index) in fieldsPermissionConfig" :key="index">
            <Row class="border border-t-0 border-gray-200 px-4 py-2">
              <Col :span="8" class="flex items-center truncate">
                {{ item.title }}
              </Col>
              <Col :span="16">
                <RadioGroup v-model:value="item.permission" class="w-full">
                  <Row>
                    <Col :span="8" class="flex items-center justify-center">
                      <Radio
                        :value="FieldPermissionType.READ"
                        size="large"
                        :label="FieldPermissionType.READ"
                      />
                    </Col>
                    <Col :span="8" class="flex items-center justify-center">
                      <Radio
                        :value="FieldPermissionType.WRITE"
                        size="large"
                        :label="FieldPermissionType.WRITE"
                        disabled
                      />
                    </Col>
                    <Col :span="8" class="flex items-center justify-center">
                      <Radio
                        :value="FieldPermissionType.NONE"
                        size="large"
                        :label="FieldPermissionType.NONE"
                      />
                    </Col>
                  </Row>
                </RadioGroup>
              </Col>
            </Row>
          </div>
        </div>
      </TabPane>
    </Tabs>
  </Drawer>
</template>
