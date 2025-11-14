<script lang="ts" setup>
import { computed, onMounted, ref } from 'vue';

import { Page, useVbenModal } from '@vben/common-ui';
import { IconifyIcon } from '@vben/icons';

import FcDesigner from '@form-create/antd-designer';
import { Button, message } from 'ant-design-vue';

import { getFormDetail } from '#/api/bpm/form';
import {
  setConfAndFields,
  useFormCreateDesigner,
} from '#/components/form-create';
import { router } from '#/router';
import Form from '#/views/bpm/form/modules/form.vue';

defineOptions({ name: 'BpmFormEditor' });

const props = defineProps<{
  copyId?: number | string;
  id?: number | string;
  type: 'copy' | 'create' | 'edit';
}>();

/** 流程表单详情 */
const flowFormConfig = ref();

const [FormModal, formModalApi] = useVbenModal({
  connectedComponent: Form,
  destroyOnClose: true,
});

const designerRef = ref<InstanceType<typeof FcDesigner>>();

/** 表单设计器配置 */
const designerConfig = ref({
  switchType: [], // 是否可以切换组件类型,或者可以相互切换的字段
  autoActive: true, // 是否自动选中拖入的组件
  useTemplate: false, // 是否生成vue2语法的模板组件
  formOptions: {
    form: {
      labelWidth: '100px', // 设置默认的 label 宽度为 100px
    },
  }, // 定义表单配置默认值
  fieldReadonly: false, // 配置field是否可以编辑
  hiddenDragMenu: false, // 隐藏拖拽操作按钮
  hiddenDragBtn: false, // 隐藏拖拽按钮
  hiddenMenu: [], // 隐藏部分菜单
  hiddenItem: [], // 隐藏部分组件
  hiddenItemConfig: {}, // 隐藏组件的部分配置项
  disabledItemConfig: {}, // 禁用组件的部分配置项
  showSaveBtn: false, // 是否显示保存按钮
  showConfig: true, // 是否显示右侧的配置界面
  showBaseForm: true, // 是否显示组件的基础配置表单
  showControl: true, // 是否显示组件联动
  showPropsForm: true, // 是否显示组件的属性配置表单
  showEventForm: true, // 是否显示组件的事件配置表单
  showValidateForm: true, // 是否显示组件的验证配置表单
  showFormConfig: true, // 是否显示表单配置
  showInputData: true, // 是否显示录入按钮
  showDevice: true, // 是否显示多端适配选项
  appendConfigData: [], // 定义渲染规则所需的formData
});

useFormCreateDesigner(designerRef); // 表单设计器增强

// 计算属性：获取当前需要加载的表单ID
const currentFormId = computed(() => {
  switch (props.type) {
    case 'copy': {
      return props.copyId;
    }
    case 'create':
    case 'edit': {
      return props.id;
    }
    default: {
      return undefined;
    }
  }
});

// 加载表单配置
async function loadFormConfig(id: number) {
  try {
    const formDetail = await getFormDetail(id);
    flowFormConfig.value = formDetail;
    if (designerRef.value) {
      setConfAndFields(designerRef, formDetail.conf, formDetail.fields);
    }
  } catch {
    message.error('加载表单配置失败');
  }
}

// 初始化设计器
async function initializeDesigner() {
  const id = currentFormId.value;

  if (props.type === 'copy' && !id) {
    message.error('复制ID不能为空');
    return;
  }

  if (id) {
    await loadFormConfig(Number(id));
  }
}

/** 保存表单 */
function handleSave() {
  formModalApi
    .setData({
      designer: designerRef.value,
      formConfig: flowFormConfig.value,
      action: props.type,
    })
    .open();
}

/** 返回列表页 */
function onBack() {
  router.push({
    path: '/bpm/manager/form',
    query: {
      refresh: '1',
    },
  });
}

onMounted(() => {
  initializeDesigner();
});
</script>

<template>
  <Page auto-content-height>
    <FormModal @success="onBack" />

    <FcDesigner
      class="h-full min-h-[500px]"
      ref="designerRef"
      :config="designerConfig"
    >
      <template #handle>
        <Button size="small" type="primary" @click="handleSave">
          <IconifyIcon icon="mdi:content-save" />
          保存
        </Button>
      </template>
    </FcDesigner>
  </Page>
</template>
