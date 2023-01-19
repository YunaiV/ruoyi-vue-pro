<template>
  <ContentWrap>
    <el-row>
      <el-col>
        <div class="mb-2 float-right">
          <el-button size="small" @click="setJson"> 导入JSON</el-button>
          <el-button size="small" @click="setOption"> 导入Options</el-button>
          <el-button size="small" type="primary" @click="showJson">生成JSON</el-button>
          <el-button size="small" type="success" @click="showOption">生成Options</el-button>
          <el-button size="small" type="danger" @click="showTemplate">生成组件</el-button>
          <el-button size="small" @click="changeLocale">中英切换</el-button>
        </div>
      </el-col>
      <el-col>
        <fc-designer ref="designer" height="780px" />
      </el-col>
    </el-row>
    <Dialog :title="dialogTitle" v-model="dialogVisible" maxHeight="600">
      <div ref="editor" v-if="dialogVisible">
        <XTextButton style="float: right" :title="t('common.copy')" @click="copy(formValue)" />
        <el-scrollbar height="580">
          <pre>
            {{ formValue }}
          </pre>
        </el-scrollbar>
      </div>
      <span style="color: red" v-if="err">输入内容格式有误!</span>
    </Dialog>
  </ContentWrap>
</template>
<script setup lang="ts" name="Build">
import formCreate from '@form-create/element-ui'
import { useClipboard } from '@vueuse/core'

const { t } = useI18n()
const message = useMessage()

const designer = ref()

const dialogVisible = ref(false)
const dialogTitle = ref('')
const err = ref(false)
const type = ref(-1)
const formValue = ref('')

const openModel = (title: string) => {
  dialogVisible.value = true
  dialogTitle.value = title
}

const setJson = () => {
  openModel('导入JSON--未实现')
}
const setOption = () => {
  openModel('导入Options--未实现')
}
const showJson = () => {
  openModel('生成JSON')
  type.value = 0
  formValue.value = designer.value.getRule()
}
const showOption = () => {
  openModel('生成Options')
  type.value = 1
  formValue.value = designer.value.getOption()
}
const showTemplate = () => {
  openModel('生成组件')
  type.value = 2
  formValue.value = makeTemplate()
}
const changeLocale = () => {
  console.info('changeLocale')
}

/** 复制 **/
const copy = async (text: string) => {
  const { copy, copied, isSupported } = useClipboard({ source: text })
  if (!isSupported) {
    message.error(t('common.copyError'))
  } else {
    await copy()
    if (unref(copied)) {
      message.success(t('common.copySuccess'))
    }
  }
}

const makeTemplate = () => {
  const rule = designer.value.getRule()
  const opt = designer.value.getOption()
  return `<template>
  <form-create
    v-model="fapi"
    :rule="rule"
    :option="option"
    @submit="onSubmit"
  ></form-create>
</template>
<script setup lang=ts>
  import formCreate from "@form-create/element-ui";
  const faps = ref(null)
  const rule = ref('')
  const option = ref('')
  const init = () => {
    rule.value = formCreate.parseJson('${formCreate.toJson(rule).replaceAll('\\', '\\\\')}')
    option.value = formCreate.parseJson('${JSON.stringify(opt)}')
  }
  const onSubmit = (formData) => {
    //todo 提交表单
  }
  init()
<\/script>`
}
</script>
