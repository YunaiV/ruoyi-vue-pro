<script lang="ts" setup>
import type { IPropTypes } from '@tinymce/tinymce-vue/lib/cjs/main/ts/components/EditorPropTypes';
import type { Editor as EditorType } from 'tinymce/tinymce';

import type { PropType } from 'vue';

import {
  computed,
  nextTick,
  onActivated,
  onBeforeUnmount,
  onDeactivated,
  onMounted,
  ref,
  unref,
  useAttrs,
  watch,
} from 'vue';

import { preferences, usePreferences } from '@vben/preferences';
import { buildShortUUID, isNumber } from '@vben/utils';

import Editor from '@tinymce/tinymce-vue';

import { useUpload } from '#/components/upload/use-upload';

import { bindHandlers } from './helper';
import ImgUpload from './img-upload.vue';
import {
  plugins as defaultPlugins,
  toolbar as defaultToolbar,
} from './tinymce';

type InitOptions = IPropTypes['init'];

defineOptions({ name: 'Tinymce', inheritAttrs: false });

const props = defineProps({
  options: {
    type: Object as PropType<Partial<InitOptions>>,
    default: () => ({}),
  },
  toolbar: {
    type: String,
    default: defaultToolbar,
  },
  plugins: {
    type: String,
    default: defaultPlugins,
  },
  height: {
    type: [Number, String] as PropType<number | string>,
    required: false,
    default: 400,
  },
  width: {
    type: [Number, String] as PropType<number | string>,
    required: false,
    default: 'auto',
  },
  showImageUpload: {
    type: Boolean,
    default: true,
  },
});

const emit = defineEmits(['change']);

/** 外部使用 v-model 绑定值 */
const modelValue = defineModel('modelValue', { default: '', type: String });

/** TinyMCE 自托管：https://www.jianshu.com/p/59a9c3802443 */
const tinymceScriptSrc = `${import.meta.env.VITE_BASE}tinymce/tinymce.min.js`;

const attrs = useAttrs();
const editorRef = ref<EditorType>();
const fullscreen = ref(false); // 图片上传，是否放到全屏的位置
const tinymceId = ref<string>(buildShortUUID('tiny-vue'));
const elRef = ref<HTMLElement | null>(null);

const containerWidth = computed(() => {
  const width = props.width;
  if (isNumber(width)) {
    return `${width}px`;
  }
  return width;
});

/** 主题皮肤 */
const { isDark } = usePreferences();
const skinName = computed(() => {
  return isDark.value ? 'oxide-dark' : 'oxide';
});

const contentCss = computed(() => {
  return isDark.value ? 'dark' : 'default';
});

/** 国际化：需要在 langs 目录下，放好语言包 */
const { locale } = usePreferences();
const langName = computed(() => {
  if (locale.value === 'en-US') {
    return 'en';
  }
  return 'zh_CN';
});

/** 监听 mode、locale 进行主题、语言切换 */
const init = ref(true);
watch(
  () => [preferences.theme.mode, preferences.app.locale],
  async () => {
    if (!editorRef.value) {
      return;
    }
    // 通过 init + v-if 来挂载/卸载组件
    destroy();
    init.value = false;
    await nextTick();
    init.value = true;
    // 等待加载完成
    await nextTick();
    setEditorMode();
  },
);

const initOptions = computed((): InitOptions => {
  const { height, options, plugins, toolbar } = props;
  return {
    height,
    toolbar,
    menubar: 'file edit view insert format tools table help',
    plugins,
    language: langName.value,
    branding: false, // 禁止显示，右下角的“使用 TinyMCE 构建”
    default_link_target: '_blank',
    link_title: false,
    object_resizing: true, // 和 vben2.0 不同，它默认是 false
    auto_focus: undefined, // 和 vben2.0 不同，它默认是 true
    skin: skinName.value,
    content_css: contentCss.value,
    content_style:
      'body { font-family:Helvetica,Arial,sans-serif; font-size:16px }',
    contextmenu: 'link image table',
    image_advtab: true, // 图片高级选项
    image_caption: true,
    importcss_append: true,
    noneditable_class: 'mceNonEditable',
    paste_data_images: true, // 允许粘贴图片，默认 base64 格式，images_upload_handler 启用时为上传
    quickbars_selection_toolbar:
      'bold italic | quicklink h2 h3 blockquote quickimage quicktable',
    toolbar_mode: 'sliding',
    ...options,
    images_upload_handler: (blobInfo) => {
      return new Promise((resolve, reject) => {
        const file = blobInfo.blob() as File;
        const { httpRequest } = useUpload();
        httpRequest(file)
          .then((url) => {
            resolve(url);
          })
          .catch((error) => {
            console.error('tinymce 上传图片失败:', error);
            reject(error.message);
          });
      });
    },
    setup: (editor) => {
      editorRef.value = editor;
      editor.on('init', (e) => initSetup(e));
    },
  };
});

/** 监听 options.readonly 是否只读 */
const disabled = computed(() => props.options.readonly ?? false);
watch(
  () => props.options,
  (options) => {
    const getDisabled = options && Reflect.get(options, 'readonly');
    const editor = unref(editorRef);
    if (editor) {
      editor.mode.set(getDisabled ? 'readonly' : 'design');
    }
  },
);

onMounted(() => {
  if (!initOptions.value.inline) {
    tinymceId.value = buildShortUUID('tiny-vue');
  }
  nextTick(() => {
    setTimeout(() => {
      initEditor();
      setEditorMode();
    }, 30);
  });
});

onBeforeUnmount(() => {
  destroy();
});

onDeactivated(() => {
  destroy();
});

onActivated(() => {
  setEditorMode();
});

function setEditorMode() {
  const editor = unref(editorRef);
  if (editor) {
    const mode = props.options.readonly ? 'readonly' : 'design';
    editor.mode.set(mode);
  }
}

function destroy() {
  const editor = unref(editorRef);
  editor?.destroy();
}

function initEditor() {
  const el = unref(elRef);
  if (el) {
    el.style.visibility = '';
  }
}

function initSetup(e: any) {
  const editor = unref(editorRef);
  if (!editor) {
    return;
  }
  const value = modelValue.value || '';

  editor.setContent(value);
  bindModelHandlers(editor);
  bindHandlers(e, attrs, unref(editorRef));
}

function setValue(editor: Record<string, any>, val?: string, prevVal?: string) {
  if (
    editor &&
    typeof val === 'string' &&
    val !== prevVal &&
    val !== editor.getContent({ format: attrs.outputFormat })
  ) {
    editor.setContent(val);
  }
}

function bindModelHandlers(editor: any) {
  const modelEvents = attrs.modelEvents ?? null;
  const normalizedEvents = Array.isArray(modelEvents)
    ? modelEvents.join(' ')
    : modelEvents;

  watch(
    () => modelValue.value,
    (val, prevVal) => {
      setValue(editor, val, prevVal);
    },
  );

  editor.on(normalizedEvents || 'change keyup undo redo', () => {
    const content = editor.getContent({ format: attrs.outputFormat });
    emit('change', content);
  });

  editor.on('FullscreenStateChanged', (e: any) => {
    fullscreen.value = e.state;
  });
}

function getUploadingImgName(name: string) {
  return `[uploading:${name}]`;
}

function handleImageUploading(name: string) {
  const editor = unref(editorRef);
  if (!editor) {
    return;
  }
  editor.execCommand('mceInsertContent', false, getUploadingImgName(name));
  const content = editor?.getContent() ?? '';
  setValue(editor, content);
}

function handleDone(name: string, url: string) {
  const editor = unref(editorRef);
  if (!editor) {
    return;
  }
  const content = editor?.getContent() ?? '';
  const val =
    content?.replace(getUploadingImgName(name), `<img src="${url}"/>`) ?? '';
  setValue(editor, val);
}

function handleError(name: string) {
  const editor = unref(editorRef);
  if (!editor) {
    return;
  }
  const content = editor?.getContent() ?? '';
  const val = content?.replace(getUploadingImgName(name), '') ?? '';
  setValue(editor, val);
}
</script>

<template>
  <div :style="{ width: containerWidth }" class="app-tinymce">
    <ImgUpload
      v-if="showImageUpload"
      v-show="editorRef"
      :disabled="disabled"
      :fullscreen="fullscreen"
      @done="handleDone"
      @error="handleError"
      @uploading="handleImageUploading"
    />
    <Editor
      v-if="!initOptions.inline && init"
      v-model="modelValue"
      :init="initOptions"
      :style="{ visibility: 'hidden', zIndex: 3000 }"
      :tinymce-script-src="tinymceScriptSrc"
      license-key="gpl"
    />
    <slot v-else></slot>
  </div>
</template>
<style lang="scss">
.tox.tox-silver-sink.tox-tinymce-aux {
  z-index: 2025; /* 由于 vben modal/drawer 的 zIndex 为 2000，需要调整 z-index（默认 1300）超过它，避免遮挡 */
}
</style>

<style lang="scss" scoped>
.app-tinymce {
  position: relative;
  line-height: normal;

  :deep(.textarea) {
    z-index: -1;
    visibility: hidden;
  }
}

/* 隐藏右上角 tinymce upgrade 按钮 */
:deep(.tox-promotion) {
  display: none !important;
}
</style>
