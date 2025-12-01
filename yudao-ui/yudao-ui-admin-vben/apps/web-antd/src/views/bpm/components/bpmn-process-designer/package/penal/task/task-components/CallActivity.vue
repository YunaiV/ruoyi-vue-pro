<script lang="ts" setup>
import { h, inject, nextTick, ref, toRaw, watch } from 'vue';

import { alert } from '@vben/common-ui';
import { PlusOutlined } from '@vben/icons';

import {
  Button,
  Divider,
  Form,
  FormItem,
  Input,
  Modal,
  Switch,
  Table,
  TableColumn,
} from 'ant-design-vue';

interface FormData {
  processInstanceName: string;
  calledElement: string;
  inheritVariables: boolean;
  businessKey: string;
  inheritBusinessKey: boolean;
  calledElementType: string;
}

defineOptions({ name: 'CallActivity' });
const props = defineProps({
  id: { type: String, default: '' },
  type: { type: String, default: '' },
});
const prefix = inject('prefix');

const formData = ref<FormData>({
  processInstanceName: '',
  calledElement: '',
  inheritVariables: false,
  businessKey: '',
  inheritBusinessKey: false,
  calledElementType: 'key',
});
const inVariableList = ref<any[]>([]);
const outVariableList = ref<any[]>([]);
const variableType = ref<string>(); // 参数类型
const editingVariableIndex = ref<number>(-1); // 编辑参数下标
const variableDialogVisible = ref<boolean>(false);
const varialbeFormRef = ref<any>();
const varialbeFormData = ref<{
  source: string;
  target: string;
}>({
  source: '',
  target: '',
});

const bpmnInstances = () => (window as any)?.bpmnInstances;
const bpmnElement = ref<any>();
const otherExtensionList = ref<any[]>([]);

const initCallActivity = () => {
  bpmnElement.value = bpmnInstances().bpmnElement;
  // console.log(bpmnElement.value.businessObject, 'callActivity');

  // 初始化所有配置项
  Object.keys(formData.value).forEach((key: string) => {
    // @ts-ignore
    formData.value[key] =
      bpmnElement.value.businessObject[key] ??
      formData.value[key as keyof FormData];
  });

  otherExtensionList.value = []; // 其他扩展配置
  inVariableList.value.length = 0;
  outVariableList.value.length = 0;
  // 初始化输入参数
  bpmnElement.value.businessObject?.extensionElements?.values?.forEach(
    (ex: any) => {
      if (ex.$type === `${prefix}:In`) {
        inVariableList.value.push(ex);
      } else if (ex.$type === `${prefix}:Out`) {
        outVariableList.value.push(ex);
      } else {
        otherExtensionList.value.push(ex);
      }
    },
  );

  // 默认添加
  // bpmnInstances().modeling.updateProperties(toRaw(bpmnElement.value), {
  //   calledElementType: 'key'
  // })
};

const updateCallActivityAttr = (attr: keyof FormData) => {
  bpmnInstances().modeling.updateProperties(toRaw(bpmnElement.value), {
    [attr]: formData.value[attr],
  });
};

const openVariableForm = (type: string, data: any, index: number) => {
  editingVariableIndex.value = index;
  variableType.value = type;
  varialbeFormData.value = index === -1 ? {} : { ...data };
  variableDialogVisible.value = true;
};

const removeVariable = async (type: string, index: number) => {
  try {
    await alert('是否确认删除？');
    if (type === 'in') {
      inVariableList.value.splice(index, 1);
    }
    if (type === 'out') {
      outVariableList.value.splice(index, 1);
    }
    updateElementExtensions();
  } catch {}
};

const saveVariable = () => {
  if (editingVariableIndex.value === -1) {
    if (variableType.value === 'in') {
      inVariableList.value.push(
        bpmnInstances().moddle.create(`${prefix}:In`, {
          ...varialbeFormData.value,
        }),
      );
    }
    if (variableType.value === 'out') {
      outVariableList.value.push(
        bpmnInstances().moddle.create(`${prefix}:Out`, {
          ...varialbeFormData.value,
        }),
      );
    }
    updateElementExtensions();
  } else {
    if (variableType.value === 'in') {
      inVariableList.value[editingVariableIndex.value].source =
        varialbeFormData.value.source;
      inVariableList.value[editingVariableIndex.value].target =
        varialbeFormData.value.target;
    }
    if (variableType.value === 'out') {
      outVariableList.value[editingVariableIndex.value].source =
        varialbeFormData.value.source;
      outVariableList.value[editingVariableIndex.value].target =
        varialbeFormData.value.target;
    }
  }
  variableDialogVisible.value = false;
};

const updateElementExtensions = () => {
  const extensions = bpmnInstances().moddle.create('bpmn:ExtensionElements', {
    values: [
      ...inVariableList.value,
      ...outVariableList.value,
      ...otherExtensionList.value,
    ],
  });
  bpmnInstances().modeling.updateProperties(toRaw(bpmnElement.value), {
    extensionElements: extensions,
  });
};

watch(
  () => props.id,
  (val) => {
    val &&
      val.length > 0 &&
      nextTick(() => {
        initCallActivity();
      });
  },
  { immediate: true },
);
</script>

<template>
  <div>
    <Form>
      <FormItem label="实例名称">
        <Input
          v-model:value="formData.processInstanceName"
          allow-clear
          placeholder="请输入实例名称"
          @change="updateCallActivityAttr('processInstanceName')"
        />
      </FormItem>

      <!-- TODO 需要可选择已存在的流程 -->
      <FormItem label="被调用流程">
        <Input
          v-model:value="formData.calledElement"
          allow-clear
          placeholder="请输入被调用流程"
          @change="updateCallActivityAttr('calledElement')"
        />
      </FormItem>

      <FormItem label="继承变量">
        <Switch
          v-model:checked="formData.inheritVariables"
          @change="updateCallActivityAttr('inheritVariables')"
        />
      </FormItem>

      <FormItem label="继承业务键">
        <Switch
          v-model:checked="formData.inheritBusinessKey"
          @change="updateCallActivityAttr('inheritBusinessKey')"
        />
      </FormItem>

      <FormItem v-if="!formData.inheritBusinessKey" label="业务键表达式">
        <Input
          v-model:value="formData.businessKey"
          allow-clear
          placeholder="请输入业务键表达式"
          @change="updateCallActivityAttr('businessKey')"
        />
      </FormItem>

      <Divider />
      <div>
        <div class="mb-10px flex">
          <span>输入参数</span>
          <Button
            class="ml-auto"
            type="primary"
            :icon="h(PlusOutlined)"
            title="添加参数"
            size="small"
            @click="openVariableForm('in', null, -1)"
          />
        </div>
        <Table
          :data-source="inVariableList"
          :scroll="{ y: 240 }"
          bordered
          :pagination="false"
        >
          <TableColumn
            title="源"
            data-index="source"
            :min-width="100"
            :ellipsis="true"
          />
          <TableColumn
            title="目标"
            data-index="target"
            :min-width="100"
            :ellipsis="true"
          />
          <TableColumn title="操作" :width="110">
            <template #default="{ record, index }">
              <Button
                type="link"
                @click="openVariableForm('in', record, index)"
                size="small"
              >
                编辑
              </Button>
              <Divider type="vertical" />
              <Button
                type="link"
                size="small"
                danger
                @click="removeVariable('in', index)"
              >
                移除
              </Button>
            </template>
          </TableColumn>
        </Table>
      </div>

      <Divider />
      <div>
        <div class="mb-10px flex">
          <span>输出参数</span>
          <Button
            class="ml-auto"
            type="primary"
            :icon="h(PlusOutlined)"
            title="添加参数"
            size="small"
            @click="openVariableForm('out', null, -1)"
          />
        </div>
        <Table
          :data-source="outVariableList"
          :scroll="{ y: 240 }"
          bordered
          :pagination="false"
        >
          <TableColumn
            title="源"
            data-index="source"
            :min-width="100"
            :ellipsis="true"
          />
          <TableColumn
            title="目标"
            data-index="target"
            :min-width="100"
            :ellipsis="true"
          />
          <TableColumn title="操作" :width="110">
            <template #default="{ record, index }">
              <Button
                type="link"
                @click="openVariableForm('out', record, index)"
                size="small"
              >
                编辑
              </Button>
              <Divider type="vertical" />
              <Button
                type="link"
                size="small"
                danger
                @click="removeVariable('out', index)"
              >
                移除
              </Button>
            </template>
          </TableColumn>
        </Table>
      </div>
    </Form>

    <!-- 添加或修改参数 -->
    <Modal
      v-model:open="variableDialogVisible"
      title="参数配置"
      :width="600"
      :destroy-on-close="true"
      @ok="saveVariable"
      @cancel="variableDialogVisible = false"
    >
      <Form :model="varialbeFormData" ref="varialbeFormRef">
        <FormItem label="源：" name="source">
          <Input v-model:value="varialbeFormData.source" allow-clear />
        </FormItem>
        <FormItem label="目标：" name="target">
          <Input v-model:value="varialbeFormData.target" allow-clear />
        </FormItem>
      </Form>
    </Modal>
  </div>
</template>

<style lang="scss" scoped></style>
