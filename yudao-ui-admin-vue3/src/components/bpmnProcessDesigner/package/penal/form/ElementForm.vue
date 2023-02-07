<template>
  <div class="panel-tab__content">
    <el-form label-width="80px">
      <el-form-item label="表单标识">
        <el-input v-model="formKey" clearable @change="updateElementFormKey" />
      </el-form-item>
      <el-form-item label="业务标识">
        <el-select v-model="businessKey" @change="updateElementBusinessKey">
          <el-option v-for="i in fieldList" :key="i.id" :value="i.id" :label="i.label" />
          <el-option label="无" value="" />
        </el-select>
      </el-form-item>
    </el-form>

    <!--字段列表-->
    <div class="element-property list-property">
      <el-divider><Icon icon="ep:coin" /> 表单字段</el-divider>
      <el-table :data="fieldList" max-height="240" border fit>
        <el-table-column label="序号" type="index" width="50px" />
        <el-table-column label="字段名称" prop="label" min-width="80px" show-overflow-tooltip />
        <el-table-column
          label="字段类型"
          prop="type"
          min-width="80px"
          :formatter="(row) => fieldType[row.type] || row.type"
          show-overflow-tooltip
        />
        <el-table-column
          label="默认值"
          prop="defaultValue"
          min-width="80px"
          show-overflow-tooltip
        />
        <el-table-column label="操作" width="90px">
          <template #default="scope">
            <el-button type="text" @click="openFieldForm(scope, scope.$index)">编辑</el-button>
            <el-divider direction="vertical" />
            <el-button type="text" style="color: #ff4d4f" @click="removeField(scope, scope.$index)"
              >移除</el-button
            >
          </template>
        </el-table-column>
      </el-table>
    </div>
    <div class="element-drawer__button">
      <XButton type="primary" proIcon="ep:plus" title="添加字段" @click="openFieldForm(null, -1)" />
    </div>

    <!--字段配置侧边栏-->
    <el-drawer
      v-model="fieldModelVisible"
      title="字段配置"
      :size="`${width}px`"
      append-to-body
      destroy-on-close
    >
      <el-form :model="formFieldForm" label-width="90px">
        <el-form-item label="字段ID">
          <el-input v-model="formFieldForm.id" clearable />
        </el-form-item>
        <el-form-item label="类型">
          <el-select
            v-model="formFieldForm.typeType"
            placeholder="请选择字段类型"
            clearable
            @change="changeFieldTypeType"
          >
            <el-option v-for="(value, key) of fieldType" :label="value" :value="key" :key="key" />
          </el-select>
        </el-form-item>
        <el-form-item label="类型名称" v-if="formFieldForm.typeType === 'custom'">
          <el-input v-model="formFieldForm.type" clearable />
        </el-form-item>
        <el-form-item label="名称">
          <el-input v-model="formFieldForm.label" clearable />
        </el-form-item>
        <el-form-item label="时间格式" v-if="formFieldForm.typeType === 'date'">
          <el-input v-model="formFieldForm.datePattern" clearable />
        </el-form-item>
        <el-form-item label="默认值">
          <el-input v-model="formFieldForm.defaultValue" clearable />
        </el-form-item>
      </el-form>

      <!-- 枚举值设置 -->
      <template v-if="formFieldForm.type === 'enum'">
        <el-divider key="enum-divider" />
        <p class="listener-filed__title" key="enum-title">
          <span><Icon icon="ep:menu" />枚举值列表：</span>
          <el-button type="primary" @click="openFieldOptionForm(null, -1, 'enum')"
            >添加枚举值</el-button
          >
        </p>
        <el-table :data="fieldEnumList" key="enum-table" max-height="240" border fit>
          <el-table-column label="序号" width="50px" type="index" />
          <el-table-column label="枚举值编号" prop="id" min-width="100px" show-overflow-tooltip />
          <el-table-column label="枚举值名称" prop="name" min-width="100px" show-overflow-tooltip />
          <el-table-column label="操作" width="90px">
            <template #default="scope">
              <el-button type="text" @click="openFieldOptionForm(scope, scope.$index, 'enum')"
                >编辑</el-button
              >
              <el-divider direction="vertical" />
              <el-button
                type="text"
                style="color: #ff4d4f"
                @click="removeFieldOptionItem(scope, scope.$index, 'enum')"
                >移除</el-button
              >
            </template>
          </el-table-column>
        </el-table>
      </template>

      <!-- 校验规则 -->
      <el-divider key="validation-divider" />
      <p class="listener-filed__title" key="validation-title">
        <span><Icon icon="ep:menu" />约束条件列表：</span>
        <el-button type="primary" @click="openFieldOptionForm(null, -1, 'constraint')"
          >添加约束</el-button
        >
      </p>
      <el-table :data="fieldConstraintsList" key="validation-table" max-height="240" border fit>
        <el-table-column label="序号" width="50px" type="index" />
        <el-table-column label="约束名称" prop="name" min-width="100px" show-overflow-tooltip />
        <el-table-column label="约束配置" prop="config" min-width="100px" show-overflow-tooltip />
        <el-table-column label="操作" width="90px">
          <template #default="scope">
            <el-button type="text" @click="openFieldOptionForm(scope, scope.$index, 'constraint')"
              >编辑</el-button
            >
            <el-divider direction="vertical" />
            <el-button
              type="text"
              style="color: #ff4d4f"
              @click="removeFieldOptionItem(scope, scope.$index, 'constraint')"
              >移除</el-button
            >
          </template>
        </el-table-column>
      </el-table>

      <!-- 表单属性 -->
      <el-divider key="property-divider" />
      <p class="listener-filed__title" key="property-title">
        <span><Icon icon="ep:menu" />字段属性列表：</span>
        <el-button type="primary" @click="openFieldOptionForm(null, -1, 'property')"
          >添加属性</el-button
        >
      </p>
      <el-table :data="fieldPropertiesList" key="property-table" max-height="240" border fit>
        <el-table-column label="序号" width="50px" type="index" />
        <el-table-column label="属性编号" prop="id" min-width="100px" show-overflow-tooltip />
        <el-table-column label="属性值" prop="value" min-width="100px" show-overflow-tooltip />
        <el-table-column label="操作" width="90px">
          <template #default="scope">
            <el-button type="text" @click="openFieldOptionForm(scope, scope.$index, 'property')"
              >编辑</el-button
            >
            <el-divider direction="vertical" />
            <el-button
              type="text"
              style="color: #ff4d4f"
              @click="removeFieldOptionItem(scope, scope.$index, 'property')"
              >移除</el-button
            >
          </template>
        </el-table-column>
      </el-table>

      <!-- 底部按钮 -->
      <div class="element-drawer__button">
        <el-button>取 消</el-button>
        <el-button type="primary" @click="saveField">保 存</el-button>
      </div>
    </el-drawer>

    <el-dialog
      v-model="fieldOptionModelVisible"
      :title="optionModelTitle"
      width="600px"
      append-to-body
      destroy-on-close
    >
      <el-form :model="fieldOptionForm" label-width="96px">
        <el-form-item label="编号/ID" v-if="fieldOptionType !== 'constraint'" key="option-id">
          <el-input v-model="fieldOptionForm.id" clearable />
        </el-form-item>
        <el-form-item label="名称" v-if="fieldOptionType !== 'property'" key="option-name">
          <el-input v-model="fieldOptionForm.name" clearable />
        </el-form-item>
        <el-form-item label="配置" v-if="fieldOptionType === 'constraint'" key="option-config">
          <el-input v-model="fieldOptionForm.config" clearable />
        </el-form-item>
        <el-form-item label="值" v-if="fieldOptionType === 'property'" key="option-value">
          <el-input v-model="fieldOptionForm.value" clearable />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="fieldOptionModelVisible = false">取 消</el-button>
        <el-button type="primary" @click="saveFieldOption">确 定</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts" name="ElementForm">
const props = defineProps({
  id: String,
  type: String
})
const prefix = inject('prefix')
const width = inject('width')

const formKey = ref('')
const businessKey = ref('')
const optionModelTitle = ref('')
const fieldList = ref<any[]>([])
const formFieldForm = ref<any>({})
const fieldType = ref({
  long: '长整型',
  string: '字符串',
  boolean: '布尔类',
  date: '日期类',
  enum: '枚举类',
  custom: '自定义类型'
})
const formFieldIndex = ref(-1) // 编辑中的字段， -1 为新增
const formFieldOptionIndex = ref(-1) // 编辑中的字段配置项， -1 为新增
const fieldModelVisible = ref(false)
const fieldOptionModelVisible = ref(false)
const fieldOptionForm = ref<any>({}) // 当前激活的字段配置项数据
const fieldOptionType = ref('') // 当前激活的字段配置项弹窗 类型
const fieldEnumList = ref<any[]>([]) // 枚举值列表
const fieldConstraintsList = ref<any[]>([]) // 约束条件列表
const fieldPropertiesList = ref<any[]>([]) // 绑定属性列表
const bpmnELement = ref()
const elExtensionElements = ref()
const formData = ref()
const otherExtensions = ref()

const resetFormList = () => {
  bpmnELement.value = window.bpmnInstances.bpmnElement
  formKey.value = bpmnELement.value.businessObject.formKey
  // 获取元素扩展属性 或者 创建扩展属性
  elExtensionElements.value =
    bpmnELement.value.businessObject.get('extensionElements') ||
    window.bpmnInstances.moddle.create('bpmn:ExtensionElements', { values: [] })
  // 获取元素表单配置 或者 创建新的表单配置
  formData.value =
    elExtensionElements.value.values.filter((ex) => ex.$type === `${prefix}:FormData`)?.[0] ||
    window.bpmnInstances.moddle.create(`${prefix}:FormData`, { fields: [] })

  // 业务标识 businessKey， 绑定在 formData 中
  businessKey.value = formData.value.businessKey

  // 保留剩余扩展元素，便于后面更新该元素对应属性
  otherExtensions.value = elExtensionElements.value.values.filter(
    (ex) => ex.$type !== `${prefix}:FormData`
  )

  // 复制原始值，填充表格
  fieldList.value = JSON.parse(JSON.stringify(formData.value.fields || []))

  // 更新元素扩展属性，避免后续报错
  updateElementExtensions()
}
const updateElementFormKey = () => {
  window.bpmnInstances.modeling.updateProperties(toRaw(bpmnELement.value), {
    formKey: formKey.value
  })
}
const updateElementBusinessKey = () => {
  window.bpmnInstances.modeling.updateModdleProperties(toRaw(bpmnELement.value), formData.value, {
    businessKey: businessKey.value
  })
}
// 根据类型调整字段type
const changeFieldTypeType = (type) => {
  // this.$set(this.formFieldForm, "type", type === "custom" ? "" : type);
  formFieldForm.value['type'] = type === 'custom' ? '' : type
}

// 打开字段详情侧边栏
const openFieldForm = (field, index) => {
  formFieldIndex.value = index
  if (index !== -1) {
    const FieldObject = formData.value.fields[index]
    formFieldForm.value = JSON.parse(JSON.stringify(field))
    // 设置自定义类型
    // this.$set(this.formFieldForm, "typeType", !this.fieldType[field.type] ? "custom" : field.type);
    formFieldForm.value['typeType'] = !fieldType.value[field.type] ? 'custom' : field.type
    // 初始化枚举值列表
    field.type === 'enum' &&
      (fieldEnumList.value = JSON.parse(JSON.stringify(FieldObject?.values || [])))
    // 初始化约束条件列表
    fieldConstraintsList.value = JSON.parse(
      JSON.stringify(FieldObject?.validation?.constraints || [])
    )
    // 初始化自定义属性列表
    fieldPropertiesList.value = JSON.parse(JSON.stringify(FieldObject?.properties?.values || []))
  } else {
    formFieldForm.value = {}
    // 初始化枚举值列表
    fieldEnumList.value = []
    // 初始化约束条件列表
    fieldConstraintsList.value = []
    // 初始化自定义属性列表
    fieldPropertiesList.value = []
  }
  fieldModelVisible.value = true
}
// 打开字段 某个 配置项 弹窗
const openFieldOptionForm = (option, index, type) => {
  fieldOptionModelVisible.value = true
  fieldOptionType.value = type
  formFieldOptionIndex.value = index
  if (type === 'property') {
    fieldOptionForm.value = option ? JSON.parse(JSON.stringify(option)) : {}
    return (optionModelTitle.value = '属性配置')
  }
  if (type === 'enum') {
    fieldOptionForm.value = option ? JSON.parse(JSON.stringify(option)) : {}
    return (optionModelTitle.value = '枚举值配置')
  }
  fieldOptionForm.value = option ? JSON.parse(JSON.stringify(option)) : {}
  return (optionModelTitle.value = '约束条件配置')
}

// 保存字段 某个 配置项
const saveFieldOption = () => {
  if (formFieldOptionIndex.value === -1) {
    if (fieldOptionType.value === 'property') {
      fieldPropertiesList.value.push(fieldOptionForm.value)
    }
    if (fieldOptionType.value === 'constraint') {
      fieldConstraintsList.value.push(fieldOptionForm.value)
    }
    if (fieldOptionType.value === 'enum') {
      fieldEnumList.value.push(fieldOptionForm.value)
    }
  } else {
    fieldOptionType.value === 'property' &&
      fieldPropertiesList.value.splice(formFieldOptionIndex.value, 1, fieldOptionForm.value)
    fieldOptionType.value === 'constraint' &&
      fieldConstraintsList.value.splice(formFieldOptionIndex.value, 1, fieldOptionForm.value)
    fieldOptionType.value === 'enum' &&
      fieldEnumList.value.splice(formFieldOptionIndex.value, 1, fieldOptionForm.value)
  }
  fieldOptionModelVisible.value = false
  fieldOptionForm.value = {}
}
// 保存字段配置
const saveField = () => {
  const { id, type, label, defaultValue, datePattern } = formFieldForm.value
  const Field = window.bpmnInstances.moddle.create(`${prefix}:FormField`, { id, type, label })
  defaultValue && (Field.defaultValue = defaultValue)
  datePattern && (Field.datePattern = datePattern)
  // 构建属性
  if (fieldPropertiesList.value && fieldPropertiesList.value.length) {
    const fieldPropertyList = fieldPropertiesList.value.map((fp) => {
      return window.bpmnInstances.moddle.create(`${prefix}:Property`, {
        id: fp.id,
        value: fp.value
      })
    })
    Field.properties = window.bpmnInstances.moddle.create(`${this.prefix}:Properties`, {
      values: fieldPropertyList
    })
  }
  // 构建校验规则
  if (fieldConstraintsList.value && fieldConstraintsList.value.length) {
    const fieldConstraintList = fieldConstraintsList.value.map((fc) => {
      return window.bpmnInstances.moddle.create(`${prefix}:Constraint`, {
        name: fc.name,
        config: fc.config
      })
    })
    Field.validation = window.bpmnInstances.moddle.create(`${prefix}:Validation`, {
      constraints: fieldConstraintList
    })
  }
  // 构建枚举值
  if (fieldEnumList.value && fieldEnumList.value.length) {
    Field.values = fieldEnumList.value.map((fe) => {
      return window.bpmnInstances.moddle.create(`${prefix}:Value`, { name: fe.name, id: fe.id })
    })
  }
  // 更新数组 与 表单配置实例
  if (formFieldIndex.value === -1) {
    fieldList.value.push(formFieldForm.value)
    formData.value.fields.push(Field)
  } else {
    fieldList.value.splice(formFieldIndex.value, 1, formFieldForm.value)
    formData.value.fields.splice(formFieldIndex.value, 1, Field)
  }
  updateElementExtensions()
  fieldModelVisible.value = false
}

// 移除某个 字段的 配置项
const removeFieldOptionItem = (option, index, type) => {
  console.log(option, 'option')
  if (type === 'property') {
    fieldPropertiesList.value.splice(index, 1)
    return
  }
  if (type === 'enum') {
    fieldEnumList.value.splice(index, 1)
    return
  }
  fieldConstraintsList.value.splice(index, 1)
}
// 移除 字段
const removeField = (field, index) => {
  console.log(field, 'field')
  fieldList.value.splice(index, 1)
  formData.value.fields.splice(index, 1)
  updateElementExtensions()
}

const updateElementExtensions = () => {
  // 更新回扩展元素
  const newElExtensionElements = window.bpmnInstances.moddle.create(`bpmn:ExtensionElements`, {
    values: otherExtensions.value.concat(formData.value)
  })
  // 更新到元素上
  window.bpmnInstances.modeling.updateProperties(toRaw(bpmnELement.value), {
    extensionElements: newElExtensionElements
  })
}

watch(
  () => props.id,
  (val) => {
    val &&
      val.length &&
      nextTick(() => {
        resetFormList()
      })
  },
  { immediate: true }
)
</script>
