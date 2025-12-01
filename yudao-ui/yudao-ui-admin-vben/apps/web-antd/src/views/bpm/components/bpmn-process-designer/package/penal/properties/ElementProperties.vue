<script lang="ts" setup>
import { inject, nextTick, ref, toRaw, watch } from 'vue';

import { IconifyIcon } from '@vben/icons';
import { cloneDeep } from '@vben/utils';

import {
  Button,
  Divider,
  Form,
  FormItem,
  Input,
  Modal,
  Table,
  TableColumn,
} from 'ant-design-vue';

defineOptions({ name: 'ElementProperties' });

const props = defineProps({
  id: {
    type: String,
    default: '',
  },
  type: {
    type: String,
    default: '',
  },
});

const prefix = inject('prefix');
// const width = inject('width')

const elementPropertyList = ref<Array<{ name: string; value: string }>>([]);
const propertyForm = ref<{ name?: string; value?: string }>({});
const editingPropertyIndex = ref(-1);
const propertyFormModelVisible = ref(false);
const bpmnElement = ref<any>();
const otherExtensionList = ref<any[]>([]);
const bpmnElementProperties = ref<any[]>([]);
const bpmnElementPropertyList = ref<any[]>([]);
const attributeFormRef = ref<any>();
const bpmnInstances = () => (window as any)?.bpmnInstances;

const resetAttributesList = () => {
  bpmnElement.value = bpmnInstances().bpmnElement;
  otherExtensionList.value = []; // 其他扩展配置
  bpmnElementProperties.value =
    // bpmnElement.value.businessObject?.extensionElements?.filter((ex) => {
    bpmnElement.value.businessObject?.extensionElements?.values?.filter(
      (ex: any) => {
        if (ex.$type !== `${prefix}:Properties`) {
          otherExtensionList.value.push(ex);
        }
        return ex.$type === `${prefix}:Properties`;
      },
    ) ?? [];

  // 保存所有的 扩展属性字段
  bpmnElementPropertyList.value = bpmnElementProperties.value.flatMap(
    (current: any) => current.values,
  );
  // 复制 显示
  elementPropertyList.value = cloneDeep(bpmnElementPropertyList.value ?? []);
};

const openAttributesForm = (
  attr: null | { name: string; value: string },
  index: number,
) => {
  editingPropertyIndex.value = index;
  // @ts-ignore
  propertyForm.value = index === -1 ? {} : cloneDeep(attr);
  propertyFormModelVisible.value = true;
  nextTick(() => {
    if (attributeFormRef.value) attributeFormRef.value.clearValidate();
  });
};

const removeAttributes = (
  _attr: { name: string; value: string },
  index: number,
) => {
  Modal.confirm({
    title: '提示',
    content: '确认移除该属性吗？',
    okText: '确 认',
    cancelText: '取 消',
    onOk() {
      elementPropertyList.value.splice(index, 1);
      bpmnElementPropertyList.value.splice(index, 1);
      // 新建一个属性字段的保存列表
      const propertiesObject = bpmnInstances().moddle.create(
        `${prefix}:Properties`,
        {
          values: bpmnElementPropertyList.value,
        },
      );
      updateElementExtensions(propertiesObject);
      resetAttributesList();
    },
    onCancel() {
      // console.info('操作取消');
    },
  });
};

const saveAttribute = () => {
  // console.log(propertyForm.value, 'propertyForm.value');
  const { name, value } = propertyForm.value;
  if (editingPropertyIndex.value === -1) {
    // 新建属性字段
    const newPropertyObject = bpmnInstances().moddle.create(
      `${prefix}:Property`,
      {
        name,
        value,
      },
    );
    // 新建一个属性字段的保存列表
    const propertiesObject = bpmnInstances().moddle.create(
      `${prefix}:Properties`,
      {
        values: [...bpmnElementPropertyList.value, newPropertyObject],
      },
    );
    updateElementExtensions(propertiesObject);
  } else {
    bpmnInstances().modeling.updateModdleProperties(
      toRaw(bpmnElement.value),
      toRaw(bpmnElementPropertyList.value)[toRaw(editingPropertyIndex.value)],
      {
        name,
        value,
      },
    );
  }
  propertyFormModelVisible.value = false;
  resetAttributesList();
};

const updateElementExtensions = (properties: any) => {
  const extensions = bpmnInstances().moddle.create('bpmn:ExtensionElements', {
    values: [...otherExtensionList.value, properties],
  });
  bpmnInstances().modeling.updateProperties(toRaw(bpmnElement.value), {
    extensionElements: extensions,
  });
};

watch(
  () => props.id,
  (val) => {
    if (val) {
      val && val.length > 0 && resetAttributesList();
    }
  },
  { immediate: true },
);
</script>

<template>
  <div class="panel-tab__content">
    <Table :data="elementPropertyList" size="small" bordered>
      <TableColumn title="序号" width="50">
        <template #default="{ index }">
          {{ index + 1 }}
        </template>
      </TableColumn>
      <TableColumn title="属性名" data-index="name" />
      <TableColumn title="属性值" data-index="value" />
      <TableColumn title="操作">
        <template #default="{ record, index }">
          <Button
            type="link"
            @click="openAttributesForm(record, index)"
            size="small"
          >
            编辑
          </Button>
          <Divider type="vertical" />
          <Button
            type="link"
            size="small"
            danger
            @click="removeAttributes(record, index)"
          >
            移除
          </Button>
        </template>
      </TableColumn>
    </Table>
    <div class="element-drawer__button">
      <Button type="primary" @click="openAttributesForm(null, -1)">
        <template #icon>
          <IconifyIcon icon="ep:plus" />
        </template>
        添加属性
      </Button>
    </div>

    <Modal
      v-model:open="propertyFormModelVisible"
      title="属性配置"
      :width="600"
      :destroy-on-close="true"
    >
      <Form :model="propertyForm" ref="attributeFormRef">
        <FormItem label="属性名：" name="name">
          <Input v-model:value="propertyForm.name" allow-clear />
        </FormItem>
        <FormItem label="属性值：" name="value">
          <Input v-model:value="propertyForm.value" allow-clear />
        </FormItem>
      </Form>
      <template #footer>
        <Button @click="propertyFormModelVisible = false">取 消</Button>
        <Button type="primary" @click="saveAttribute">确 定</Button>
      </template>
    </Modal>
  </div>
</template>
