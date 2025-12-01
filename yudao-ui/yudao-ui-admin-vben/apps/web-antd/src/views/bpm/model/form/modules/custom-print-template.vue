<script setup lang="ts">
import type { MentionItem } from '../modules/tinymce-plugin';

import { computed, onBeforeUnmount, ref, shallowRef } from 'vue';

import { useVbenModal } from '@vben/common-ui';

import Editor from '@tinymce/tinymce-vue';
import { Alert } from 'ant-design-vue';

import { setupTinyPlugins } from './tinymce-plugin';

const props = withDefaults(
  defineProps<{
    formFields?: Array<{ field: string; title: string }>;
  }>(),
  {
    formFields: () => [],
  },
);

const emits = defineEmits<{
  (e: 'confirm', value: string): void;
}>();

/** TinyMCE 自托管：https://www.jianshu.com/p/59a9c3802443 */
const tinymceScriptSrc = `${import.meta.env.VITE_BASE}tinymce/tinymce.min.js`;

const [Modal, modalApi] = useVbenModal({
  async onOpenChange(isOpen: boolean) {
    if (!isOpen) {
      return;
    }
    modalApi.lock();
    try {
      const { template } = modalApi.getData<{
        template: string;
      }>();
      if (template !== undefined) {
        valueHtml.value = template;
      }
    } finally {
      modalApi.unlock();
    }
  },
  onConfirm() {
    emits('confirm', valueHtml.value);
    modalApi.close();
  },
});

const mentionList = computed<MentionItem[]>(() => {
  const base: MentionItem[] = [
    { id: 'startUser', name: '发起人' },
    { id: 'startUserDept', name: '发起人部门' },
    { id: 'processName', name: '流程名称' },
    { id: 'processNum', name: '流程编号' },
    { id: 'startTime', name: '发起时间' },
    { id: 'endTime', name: '结束时间' },
    { id: 'processStatus', name: '流程状态' },
    { id: 'printUser', name: '打印人' },
    { id: 'printTime', name: '打印时间' },
  ];

  const extras: MentionItem[] = (props.formFields || []).map((it: any) => ({
    id: it.field,
    name: `[表单]${it.title}`,
  }));
  return [...base, ...extras];
}); // 提供给 @ 自动补全的字段（默认 + 表单字段）

const valueHtml = ref<string>('');
const editorRef = shallowRef<any>(); // 编辑器

const tinyInit = {
  height: 400,
  width: 'auto',
  menubar: false,
  plugins: 'link importcss table code preview autoresize lists ',
  toolbar:
    'undo redo | styles fontsize | bold italic underline | alignleft aligncenter alignright | link table | processrecord code preview',
  language: 'zh_CN',
  branding: false,
  statusbar: true,
  content_style:
    'body { font-family:Helvetica,Arial,sans-serif; font-size:16px }',
  setup(editor: any) {
    editorRef.value = editor;
    // 在编辑器 setup 时注册自定义插件
    setupTinyPlugins(editor, () => mentionList.value);
  },
};

onBeforeUnmount(() => {
  if (editorRef.value) {
    editorRef.value.destroy?.();
  }
});
</script>

<template>
  <Modal class="w-3/4" title="自定义模板">
    <div class="mb-3">
      <Alert
        message="输入 @ 可选择插入流程选项和表单选项"
        type="info"
        show-icon
      />
    </div>
    <Editor
      v-model="valueHtml"
      :init="tinyInit"
      :tinymce-script-src="tinymceScriptSrc"
      license-key="gpl"
    />
  </Modal>
</template>
