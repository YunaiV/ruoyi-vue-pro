<!-- eslint-disable no-unused-vars -->
<script lang="ts" setup>
import { computed, inject, nextTick, onMounted, ref, toRaw, watch } from 'vue';

import { cloneDeep } from '@vben/utils';

import { Form, FormItem, Select } from 'ant-design-vue';

import { getFormSimpleList } from '#/api/bpm/form';

defineOptions({ name: 'ElementForm' });

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

const formKey = ref<number | string | undefined>(undefined);
const businessKey = ref('');
const optionModelTitle = ref('');
const fieldList = ref<any[]>([]);
const formFieldForm = ref<any>({});
const fieldType = ref({
  long: '长整型',
  string: '字符串',
  boolean: '布尔类',
  date: '日期类',
  enum: '枚举类',
  custom: '自定义类型',
});
const formFieldIndex = ref(-1); // 编辑中的字段， -1 为新增
const formFieldOptionIndex = ref(-1); // 编辑中的字段配置项， -1 为新增
const fieldModelVisible = ref(false);
const fieldOptionModelVisible = ref(false);
const fieldOptionForm = ref<any>({}); // 当前激活的字段配置项数据
const fieldOptionType = ref(''); // 当前激活的字段配置项弹窗 类型
const fieldEnumList = ref<any[]>([]); // 枚举值列表
const fieldConstraintsList = ref<any[]>([]); // 约束条件列表
const fieldPropertiesList = ref<any[]>([]); // 绑定属性列表
const bpmnELement = ref();
const elExtensionElements = ref();
const formData = ref();
const otherExtensions = ref();

const bpmnInstances = () => (window as any)?.bpmnInstances;
const resetFormList = () => {
  bpmnELement.value = bpmnInstances().bpmnElement;
  formKey.value = bpmnELement.value.businessObject.formKey;
  // if (formKey.value?.length > 0) {
  //   formKey.value = parseInt(formKey.value)
  // }
  // 获取元素扩展属性 或者 创建扩展属性
  elExtensionElements.value =
    bpmnELement.value.businessObject.get('extensionElements') ||
    bpmnInstances().moddle.create('bpmn:ExtensionElements', { values: [] });
  // 获取元素表单配置 或者 创建新的表单配置
  formData.value =
    elExtensionElements.value.values.find(
      (ex: any) => ex.$type === `${prefix}:FormData`,
    ) || bpmnInstances().moddle.create(`${prefix}:FormData`, { fields: [] });

  // 业务标识 businessKey， 绑定在 formData 中
  businessKey.value = formData.value.businessKey;

  // 保留剩余扩展元素，便于后面更新该元素对应属性
  otherExtensions.value = elExtensionElements.value.values.filter(
    (ex: any) => ex.$type !== `${prefix}:FormData`,
  );

  // 复制原始值，填充表格
  fieldList.value = cloneDeep(formData.value.fields || []);

  // 更新元素扩展属性，避免后续报错
  updateElementExtensions();
};
const updateElementFormKey = () => {
  bpmnInstances().modeling.updateProperties(toRaw(bpmnELement.value), {
    formKey: formKey.value,
  });
};
const _updateElementBusinessKey = () => {
  bpmnInstances().modeling.updateModdleProperties(
    toRaw(bpmnELement.value),
    formData.value,
    {
      businessKey: businessKey.value,
    },
  );
};
// 根据类型调整字段type
const _changeFieldTypeType = (type: any) => {
  formFieldForm.value.type = type === 'custom' ? '' : type;
};

// 打开字段详情侧边栏
const _openFieldForm = (field: any, index: any) => {
  formFieldIndex.value = index;
  if (index === -1) {
    formFieldForm.value = {};
    // 初始化枚举值列表
    fieldEnumList.value = [];
    // 初始化约束条件列表
    fieldConstraintsList.value = [];
    // 初始化自定义属性列表
    fieldPropertiesList.value = [];
  } else {
    const FieldObject = formData.value.fields[index];
    formFieldForm.value = cloneDeep(field);
    // 设置自定义类型
    // this.$set(this.formFieldForm, "typeType", !this.fieldType[field.type] ? "custom" : field.type);
    formFieldForm.value.typeType = fieldType.value[
      field.type as keyof typeof fieldType.value
    ]
      ? field.type
      : 'custom';
    // 初始化枚举值列表
    field.type === 'enum' &&
      (fieldEnumList.value = cloneDeep(FieldObject?.values || []));
    // 初始化约束条件列表
    fieldConstraintsList.value = cloneDeep(
      FieldObject?.validation?.constraints || [],
    );
    // 初始化自定义属性列表
    fieldPropertiesList.value = cloneDeep(
      FieldObject?.properties?.values || [],
    );
  }
  fieldModelVisible.value = true;
};
// 打开字段 某个 配置项 弹窗
const _openFieldOptionForm = (option: any, index: any, type: any) => {
  fieldOptionModelVisible.value = true;
  fieldOptionType.value = type;
  formFieldOptionIndex.value = index;
  if (type === 'property') {
    fieldOptionForm.value = option ? cloneDeep(option) : {};
    return (optionModelTitle.value = '属性配置');
  }
  if (type === 'enum') {
    fieldOptionForm.value = option ? cloneDeep(option) : {};
    return (optionModelTitle.value = '枚举值配置');
  }
  fieldOptionForm.value = option ? cloneDeep(option) : {};
  return (optionModelTitle.value = '约束条件配置');
};

// 保存字段 某个 配置项
const _saveFieldOption = () => {
  if (formFieldOptionIndex.value === -1) {
    if (fieldOptionType.value === 'property') {
      fieldPropertiesList.value.push(fieldOptionForm.value);
    }
    if (fieldOptionType.value === 'constraint') {
      fieldConstraintsList.value.push(fieldOptionForm.value);
    }
    if (fieldOptionType.value === 'enum') {
      fieldEnumList.value.push(fieldOptionForm.value);
    }
  } else {
    fieldOptionType.value === 'property' &&
      fieldPropertiesList.value.splice(
        formFieldOptionIndex.value,
        1,
        fieldOptionForm.value,
      );
    fieldOptionType.value === 'constraint' &&
      fieldConstraintsList.value.splice(
        formFieldOptionIndex.value,
        1,
        fieldOptionForm.value,
      );
    fieldOptionType.value === 'enum' &&
      fieldEnumList.value.splice(
        formFieldOptionIndex.value,
        1,
        fieldOptionForm.value,
      );
  }
  fieldOptionModelVisible.value = false;
  fieldOptionForm.value = {};
};
// 保存字段配置
const _saveField = () => {
  const { id, type, label, defaultValue, datePattern } = formFieldForm.value;
  const Field = bpmnInstances().moddle.create(`${prefix}:FormField`, {
    id,
    type,
    label,
  });
  defaultValue && (Field.defaultValue = defaultValue);
  datePattern && (Field.datePattern = datePattern);
  // 构建属性
  if (fieldPropertiesList.value && fieldPropertiesList.value.length > 0) {
    const fieldPropertyList = fieldPropertiesList.value.map((fp: any) => {
      return bpmnInstances().moddle.create(`${prefix}:Property`, {
        id: fp.id,
        value: fp.value,
      });
    });
    Field.properties = bpmnInstances().moddle.create(`${prefix}:Properties`, {
      values: fieldPropertyList,
    });
  }
  // 构建校验规则
  if (fieldConstraintsList.value && fieldConstraintsList.value.length > 0) {
    const fieldConstraintList = fieldConstraintsList.value.map((fc: any) => {
      return bpmnInstances().moddle.create(`${prefix}:Constraint`, {
        name: fc.name,
        config: fc.config,
      });
    });
    Field.validation = bpmnInstances().moddle.create(`${prefix}:Validation`, {
      constraints: fieldConstraintList,
    });
  }
  // 构建枚举值
  if (fieldEnumList.value && fieldEnumList.value.length > 0) {
    Field.values = fieldEnumList.value.map((fe: any) => {
      return bpmnInstances().moddle.create(`${prefix}:Value`, {
        name: fe.name,
        id: fe.id,
      });
    });
  }
  // 更新数组 与 表单配置实例
  if (formFieldIndex.value === -1) {
    fieldList.value.push(formFieldForm.value);
    formData.value.fields.push(Field);
  } else {
    fieldList.value.splice(formFieldIndex.value, 1, formFieldForm.value);
    formData.value.fields.splice(formFieldIndex.value, 1, Field);
  }
  updateElementExtensions();
  fieldModelVisible.value = false;
};

// 移除某个 字段的 配置项
const _removeFieldOptionItem = (_option: any, index: any, type: any) => {
  // console.log(option, 'option')
  if (type === 'property') {
    fieldPropertiesList.value.splice(index, 1);
    return;
  }
  if (type === 'enum') {
    fieldEnumList.value.splice(index, 1);
    return;
  }
  fieldConstraintsList.value.splice(index, 1);
};
// 移除 字段
const _removeField = (field: any, index: any) => {
  console.warn(field, 'field');
  fieldList.value.splice(index, 1);
  formData.value.fields.splice(index, 1);
  updateElementExtensions();
};

const updateElementExtensions = () => {
  // 更新回扩展元素
  const newElExtensionElements = bpmnInstances().moddle.create(
    `bpmn:ExtensionElements`,
    {
      values: [...otherExtensions.value, formData.value],
    },
  );
  // 更新到元素上
  bpmnInstances().modeling.updateProperties(toRaw(bpmnELement.value), {
    extensionElements: newElExtensionElements,
  });
};

const formList = ref<any[]>([]); // 流程表单的下拉框的数据
const formOptions = computed(() => {
  return formList.value.map((form: any) => ({
    value: form.id,
    label: form.name,
  }));
});

onMounted(async () => {
  formList.value = await getFormSimpleList();
  formKey.value = formKey.value
    ? Number.parseInt(formKey.value as string)
    : undefined;
});

watch(
  () => props.id,
  (val: any) => {
    val &&
      val.length > 0 &&
      nextTick(() => {
        resetFormList();
      });
  },
  { immediate: true },
);
</script>

<template>
  <div class="panel-tab__content">
    <Form>
      <FormItem label="流程表单">
        <!--        <Input v-model:value="formKey" @change="updateElementFormKey" />-->
        <Select
          v-model:value="formKey"
          allow-clear
          @change="updateElementFormKey"
          :options="formOptions"
        />
      </FormItem>
      <FormItem label="业务标识">
        <Select
          v-model:value="businessKey"
          @change="_updateElementBusinessKey"
          allow-clear
        >
          <Select.Option v-for="i in fieldList" :key="i.id" :value="i.id">
            {{ i.label }}
          </Select.Option>
          <Select.Option value="">无</Select.Option>
        </Select>
      </FormItem>
    </Form>

    <!--字段列表-->
    <!--    <div class="element-property list-property">-->
    <!--      <Divider><Icon icon="ep:coin" /> 表单字段</Divider>-->
    <!--      <Table :data-source="fieldList" :scroll="{ y: 240 }" bordered>-->
    <!--        <TableColumn title="序号" type="index" width="50px" />-->
    <!--        <TableColumn title="字段名称" dataIndex="label" width="80px" :ellipsis="true" />-->
    <!--        <TableColumn-->
    <!--          title="字段类型"-->
    <!--          dataIndex="type"-->
    <!--          width="80px"-->
    <!--          :customRender="({ text }) => fieldType[text] || text"-->
    <!--          :ellipsis="true"-->
    <!--        />-->
    <!--        <TableColumn-->
    <!--          title="默认值"-->
    <!--          dataIndex="defaultValue"-->
    <!--          width="80px"-->
    <!--          :ellipsis="true"-->
    <!--        />-->
    <!--        <TableColumn title="操作" width="90px">-->
    <!--          <template #default="scope">-->
    <!--            <Button type="link" @click="openFieldForm(scope, scope.$index)">-->
    <!--              编辑-->
    <!--            </Button>-->
    <!--            <Divider type="vertical" />-->
    <!--            <Button-->
    <!--              type="link"-->
    <!--              danger-->
    <!--              @click="removeField(scope, scope.$index)"-->
    <!--            >-->
    <!--              移除-->
    <!--            </Button>-->
    <!--          </template>-->
    <!--        </TableColumn>-->
    <!--      </Table>-->
    <!--    </div>-->
    <!--    <div class="element-drawer__button">-->
    <!--      <Button type="primary" @click="openFieldForm(null, -1)">添加字段</Button>-->
    <!--    </div>-->

    <!--字段配置侧边栏-->
    <!--    <Drawer-->
    <!--      v-model:open="fieldModelVisible"-->
    <!--      title="字段配置"-->
    <!--      :width="`${width}px`"-->
    <!--      destroyOnClose-->
    <!--    >-->
    <!--      <Form :model="formFieldForm" :label-col="{ style: { width: '90px' } }">-->
    <!--        <FormItem label="字段ID">-->
    <!--          <Input v-model:value="formFieldForm.id" allowClear />-->
    <!--        </FormItem>-->
    <!--        <FormItem label="类型">-->
    <!--          <Select-->
    <!--            v-model:value="formFieldForm.typeType"-->
    <!--            placeholder="请选择字段类型"-->
    <!--            allowClear-->
    <!--            @change="changeFieldTypeType"-->
    <!--          >-->
    <!--            <SelectOption v-for="(value, key) of fieldType" :key="key" :value="key">{{ value }}</SelectOption>-->
    <!--          </Select>-->
    <!--        </FormItem>-->
    <!--        <FormItem label="类型名称" v-if="formFieldForm.typeType === 'custom'">-->
    <!--          <Input v-model:value="formFieldForm.type" allowClear />-->
    <!--        </FormItem>-->
    <!--        <FormItem label="名称">-->
    <!--          <Input v-model:value="formFieldForm.label" allowClear />-->
    <!--        </FormItem>-->
    <!--        <FormItem label="时间格式" v-if="formFieldForm.typeType === 'date'">-->
    <!--          <Input v-model:value="formFieldForm.datePattern" allowClear />-->
    <!--        </FormItem>-->
    <!--        <FormItem label="默认值">-->
    <!--          <Input v-model:value="formFieldForm.defaultValue" allowClear />-->
    <!--        </FormItem>-->
    <!--      </Form>-->

    <!--      &lt;!&ndash; 枚举值设置 &ndash;&gt;-->
    <!--      <template v-if="formFieldForm.type === 'enum'">-->
    <!--        <Divider key="enum-divider" />-->
    <!--        <p class="listener-filed__title" key="enum-title">-->
    <!--          <span><Icon icon="ep:menu" />枚举值列表：</span>-->
    <!--          <Button type="primary" @click="openFieldOptionForm(null, -1, 'enum')"-->
    <!--            >添加枚举值</Button-->
    <!--          >-->
    <!--        </p>-->
    <!--        <Table :data-source="fieldEnumList" key="enum-table" :scroll="{ y: 240 }" bordered>-->
    <!--          <TableColumn title="序号" width="50px" type="index" />-->
    <!--          <TableColumn title="枚举值编号" dataIndex="id" width="100px" :ellipsis="true" />-->
    <!--          <TableColumn title="枚举值名称" dataIndex="name" width="100px" :ellipsis="true" />-->
    <!--          <TableColumn title="操作" width="90px">-->
    <!--            <template #default="scope">-->
    <!--              <Button-->
    <!--                type="link"-->
    <!--                @click="openFieldOptionForm(scope, scope.$index, 'enum')"-->
    <!--              >-->
    <!--                编辑-->
    <!--              </Button>-->
    <!--              <Divider type="vertical" />-->
    <!--              <Button-->
    <!--                type="link"-->
    <!--                danger-->
    <!--                @click="removeFieldOptionItem(scope, scope.$index, 'enum')"-->
    <!--              >-->
    <!--                移除-->
    <!--              </Button>-->
    <!--            </template>-->
    <!--          </TableColumn>-->
    <!--        </Table>-->
    <!--      </template>-->

    <!--      &lt;!&ndash; 校验规则 &ndash;&gt;-->
    <!--      <Divider key="validation-divider" />-->
    <!--      <p class="listener-filed__title" key="validation-title">-->
    <!--        <span><Icon icon="ep:menu" />约束条件列表：</span>-->
    <!--        <Button type="primary" @click="openFieldOptionForm(null, -1, 'constraint')"-->
    <!--          >添加约束</Button-->
    <!--        >-->
    <!--      </p>-->
    <!--      <Table :data-source="fieldConstraintsList" key="validation-table" :scroll="{ y: 240 }" bordered>-->
    <!--        <TableColumn title="序号" width="50px" type="index" />-->
    <!--        <TableColumn title="约束名称" dataIndex="name" width="100px" :ellipsis="true" />-->
    <!--        <TableColumn title="约束配置" dataIndex="config" width="100px" :ellipsis="true" />-->
    <!--        <TableColumn title="操作" width="90px">-->
    <!--          <template #default="scope">-->
    <!--            <Button-->
    <!--              type="link"-->
    <!--              @click="openFieldOptionForm(scope, scope.$index, 'constraint')"-->
    <!--            >-->
    <!--              编辑-->
    <!--            </Button>-->
    <!--            <Divider type="vertical" />-->
    <!--            <Button-->
    <!--              type="link"-->
    <!--              danger-->
    <!--              @click="removeFieldOptionItem(scope, scope.$index, 'constraint')"-->
    <!--            >-->
    <!--              移除-->
    <!--            </Button>-->
    <!--          </template>-->
    <!--        </TableColumn>-->
    <!--      </Table>-->

    <!--      &lt;!&ndash; 表单属性 &ndash;&gt;-->
    <!--      <Divider key="property-divider" />-->
    <!--      <p class="listener-filed__title" key="property-title">-->
    <!--        <span><Icon icon="ep:menu" />字段属性列表：</span>-->
    <!--        <Button type="primary" @click="openFieldOptionForm(null, -1, 'property')"-->
    <!--          >添加属性</Button-->
    <!--        >-->
    <!--      </p>-->
    <!--      <Table :data-source="fieldPropertiesList" key="property-table" :scroll="{ y: 240 }" bordered>-->
    <!--        <TableColumn title="序号" width="50px" type="index" />-->
    <!--        <TableColumn title="属性编号" dataIndex="id" width="100px" :ellipsis="true" />-->
    <!--        <TableColumn title="属性值" dataIndex="value" width="100px" :ellipsis="true" />-->
    <!--        <TableColumn title="操作" width="90px">-->
    <!--          <template #default="scope">-->
    <!--            <Button-->
    <!--              type="link"-->
    <!--              @click="openFieldOptionForm(scope, scope.$index, 'property')"-->
    <!--            >-->
    <!--              编辑-->
    <!--            </Button>-->
    <!--            <Divider type="vertical" />-->
    <!--            <Button-->
    <!--              type="link"-->
    <!--              danger-->
    <!--              @click="removeFieldOptionItem(scope, scope.$index, 'property')"-->
    <!--            >-->
    <!--              移除-->
    <!--            </Button>-->
    <!--          </template>-->
    <!--        </TableColumn>-->
    <!--      </Table>-->

    <!--      &lt;!&ndash; 底部按钮 &ndash;&gt;-->
    <!--      <div class="element-drawer__button">-->
    <!--        <Button>取 消</Button>-->
    <!--        <Button type="primary" @click="saveField">保 存</Button>-->
    <!--      </div>-->
    <!--    </Drawer>-->

    <!--    <Modal-->
    <!--      v-model:open="fieldOptionModelVisible"-->
    <!--      :title="optionModelTitle"-->
    <!--      width="600px"-->
    <!--      destroyOnClose-->
    <!--    >-->
    <!--      <Form :model="fieldOptionForm" :label-col="{ style: { width: '96px' } }">-->
    <!--        <FormItem label="编号/ID" v-if="fieldOptionType !== 'constraint'" key="option-id">-->
    <!--          <Input v-model:value="fieldOptionForm.id" allowClear />-->
    <!--        </FormItem>-->
    <!--        <FormItem label="名称" v-if="fieldOptionType !== 'property'" key="option-name">-->
    <!--          <Input v-model:value="fieldOptionForm.name" allowClear />-->
    <!--        </FormItem>-->
    <!--        <FormItem label="配置" v-if="fieldOptionType === 'constraint'" key="option-config">-->
    <!--          <Input v-model:value="fieldOptionForm.config" allowClear />-->
    <!--        </FormItem>-->
    <!--        <FormItem label="值" v-if="fieldOptionType === 'property'" key="option-value">-->
    <!--          <Input v-model:value="fieldOptionForm.value" allowClear />-->
    <!--        </FormItem>-->
    <!--      </Form>-->
    <!--      <template #footer>-->
    <!--        <Button @click="fieldOptionModelVisible = false">取 消</Button>-->
    <!--        <Button type="primary" @click="saveFieldOption">确 定</Button>-->
    <!--      </template>-->
    <!--    </Modal>-->
  </div>
</template>
