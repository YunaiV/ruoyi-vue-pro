<script setup lang="ts">
import { onBeforeUnmount, computed, PropType, unref, nextTick, ref, watch, shallowRef } from 'vue'
import { Editor, Toolbar } from '@wangeditor/editor-for-vue'
import { IDomEditor, IEditorConfig, i18nChangeLanguage } from '@wangeditor/editor'
import { propTypes } from '@/utils/propTypes'
import { isNumber } from '@/utils/is'
import { ElMessage } from 'element-plus'
import { useLocaleStore } from '@/store/modules/locale'
import { getAccessToken, getTenantId } from '@/utils/auth'

type InsertFnType = (url: string, alt: string, href: string) => void

const localeStore = useLocaleStore()

const currentLocale = computed(() => localeStore.getCurrentLocale)

i18nChangeLanguage(unref(currentLocale).lang)

const props = defineProps({
  editorId: propTypes.string.def('wangeEditor-1'),
  height: propTypes.oneOfType([Number, String]).def('500px'),
  editorConfig: {
    type: Object as PropType<IEditorConfig>,
    default: () => undefined
  },
  modelValue: propTypes.string.def('')
})

const emit = defineEmits(['change', 'update:modelValue'])

// 编辑器实例，必须用 shallowRef
const editorRef = shallowRef<IDomEditor>()

const valueHtml = ref('')

watch(
  () => props.modelValue,
  (val: string) => {
    if (val === unref(valueHtml)) return
    valueHtml.value = val
  },
  {
    immediate: true
  }
)

// 监听
watch(
  () => valueHtml.value,
  (val: string) => {
    emit('update:modelValue', val)
  }
)

const handleCreated = (editor: IDomEditor) => {
  editorRef.value = editor
}

// 编辑器配置
const editorConfig = computed((): IEditorConfig => {
  return Object.assign(
    {
      placeholder: '请输入内容...',
      readOnly: false,
      customAlert: (s: string, t: string) => {
        switch (t) {
          case 'success':
            ElMessage.success(s)
            break
          case 'info':
            ElMessage.info(s)
            break
          case 'warning':
            ElMessage.warning(s)
            break
          case 'error':
            ElMessage.error(s)
            break
          default:
            ElMessage.info(s)
            break
        }
      },
      autoFocus: false,
      scroll: true,
      MENU_CONF: {
        ['uploadImage']: {
          server: import.meta.env.VITE_UPLOAD_URL,
          // 单个文件的最大体积限制，默认为 2M
          maxFileSize: 5 * 1024 * 1024,
          // 最多可上传几个文件，默认为 100
          maxNumberOfFiles: 10,
          // 选择文件时的类型限制，默认为 ['image/*'] 。如不想限制，则设置为 []
          allowedFileTypes: ['image/*'],

          // 自定义上传参数，例如传递验证的 token 等。参数会被添加到 formData 中，一起上传到服务端。
          meta: { updateSupport: 0 },
          // 将 meta 拼接到 url 参数中，默认 false
          metaWithUrl: true,

          // 自定义增加 http  header
          headers: {
            Accept: '*',
            Authorization: 'Bearer ' + getAccessToken(),
            'tenant-id': getTenantId()
          },

          // 跨域是否传递 cookie ，默认为 false
          withCredentials: true,

          // 超时时间，默认为 10 秒
          timeout: 5 * 1000, // 5 秒

          // form-data fieldName，后端接口参数名称，默认值wangeditor-uploaded-image
          fieldName: 'file',

          // 上传之前触发
          onBeforeUpload(file: File) {
            console.log(file)
            return file
          },
          // 上传进度的回调函数
          onProgress(progress: number) {
            // progress 是 0-100 的数字
            console.log('progress', progress)
          },
          onSuccess(file: File, res: any) {
            console.log('onSuccess', file, res)
          },
          onFailed(file: File, res: any) {
            alert(res.message)
            console.log('onFailed', file, res)
          },
          onError(file: File, err: any, res: any) {
            alert(err.message)
            console.error('onError', file, err, res)
          },
          // 自定义插入图片
          customInsert(res: any, insertFn: InsertFnType) {
            insertFn(res.data, 'image', res.data)
          }
        }
      },
      uploadImgShowBase64: true
    },
    props.editorConfig || {}
  )
})
const editorStyle = computed(() => {
  return {
    height: isNumber(props.height) ? `${props.height}px` : props.height
  }
})

// 回调函数
const handleChange = (editor: IDomEditor) => {
  emit('change', editor)
}

// 组件销毁时，及时销毁编辑器
onBeforeUnmount(() => {
  const editor = unref(editorRef.value)
  if (editor === null) return

  // 销毁，并移除 editor
  editor?.destroy()
})

const getEditorRef = async (): Promise<IDomEditor> => {
  await nextTick()
  return unref(editorRef.value) as IDomEditor
}

defineExpose({
  getEditorRef
})
</script>

<template>
  <div class="border-1 border-solid border-[var(--tags-view-border-color)] z-3000">
    <!-- 工具栏 -->
    <Toolbar
      :editor="editorRef"
      :editorId="editorId"
      class="border-bottom-1 border-solid border-[var(--tags-view-border-color)]"
    />
    <!-- 编辑器 -->
    <Editor
      v-model="valueHtml"
      :editorId="editorId"
      :defaultConfig="editorConfig"
      :style="editorStyle"
      @on-change="handleChange"
      @on-created="handleCreated"
    />
  </div>
</template>

<style src="@wangeditor/editor/dist/css/style.css"></style>
