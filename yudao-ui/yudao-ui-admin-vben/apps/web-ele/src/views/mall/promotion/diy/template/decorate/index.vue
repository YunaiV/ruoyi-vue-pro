<script lang="ts" setup>
import type { MallDiyPageApi } from '#/api/mall/promotion/diy/page';
import type { MallDiyTemplateApi } from '#/api/mall/promotion/diy/template';
import type { DiyComponentLibrary } from '#/views/mall/promotion/components'; // 商城的 DIY 组件，在 DiyEditor 目录下

import { onMounted, reactive, ref } from 'vue';
import { useRoute } from 'vue-router';

import { useTabs } from '@vben/hooks';
import { IconifyIcon } from '@vben/icons';
import { useAccessStore } from '@vben/stores';
import { isEmpty } from '@vben/utils';

import {
  ElLoading,
  ElMessage,
  ElRadioButton,
  ElRadioGroup,
  ElTooltip,
} from 'element-plus';

import { updateDiyPageProperty } from '#/api/mall/promotion/diy/page';
import {
  getDiyTemplateProperty,
  updateDiyTemplateProperty,
} from '#/api/mall/promotion/diy/template';
import { DiyEditor, PAGE_LIBS } from '#/views/mall/promotion/components'; // 特殊：存储 reset 重置时，当前 selectedTemplateItem 值，从而进行恢复

/** 装修模板表单 */
defineOptions({ name: 'DiyTemplateDecorate' });

const route = useRoute();
const { refreshTab } = useTabs();

const DIY_PAGE_INDEX_KEY = 'diy_page_index'; // 特殊：存储 reset 重置时，当前 selectedTemplateItem 值，从而进行恢复

const selectedTemplateItem = ref(0);
const templateItems = reactive([
  { name: '基础设置', icon: 'ep:iphone' },
  { name: '首页', icon: 'ep:home-filled' },
  { name: '我的', icon: 'ep:user-filled' },
]); // 左上角工具栏操作按钮

const formData = ref<MallDiyTemplateApi.DiyTemplateProperty>();
const currentFormData = ref<
  MallDiyPageApi.DiyPage | MallDiyTemplateApi.DiyTemplateProperty
>({
  property: '',
} as MallDiyPageApi.DiyPage); // 当前编辑的属性
const currentFormDataMap = ref<
  Map<string, MallDiyPageApi.DiyPage | MallDiyTemplateApi.DiyTemplateProperty>
>(new Map()); // templateItem 对应的缓存

const previewUrl = ref(''); // 商城 H5 预览地址

const templateLibs = [] as DiyComponentLibrary[]; // 模板组件库
const libs = ref<DiyComponentLibrary[]>(templateLibs); // 当前组件库

/** 获取详情 */
async function getPageDetail(id: any) {
  const loadingInstance = ElLoading.service({
    text: '加载中...',
  });
  try {
    formData.value = await getDiyTemplateProperty(id);

    // 拼接手机预览链接
    const domain = import.meta.env.VITE_MALL_H5_DOMAIN;
    const accessStore = useAccessStore();
    previewUrl.value = `${domain}?templateId=${formData.value.id}&${accessStore.tenantId}`;
  } finally {
    loadingInstance.close();
  }
}

/** 模板选项切换 */
function handleTemplateItemChange(val: any) {
  // 缓存模版编辑数据
  currentFormDataMap.value.set(
    templateItems[selectedTemplateItem.value]?.name || '',
    currentFormData.value!,
  );
  // 读取模版缓存
  const data = currentFormDataMap.value.get(templateItems[val]?.name || '');

  // 切换模版
  selectedTemplateItem.value = val;

  // 情况一：编辑模板
  if (val === 0) {
    libs.value = templateLibs;
    currentFormData.value = (isEmpty(data) ? formData.value : data) as
      | MallDiyPageApi.DiyPage
      | MallDiyTemplateApi.DiyTemplateProperty;
    return;
  }

  // 情况二：编辑页面
  libs.value = PAGE_LIBS;
  currentFormData.value = (
    isEmpty(data)
      ? formData.value!.pages.find(
          (page: MallDiyPageApi.DiyPage) =>
            page.name === templateItems[val]?.name,
        )
      : data
  ) as MallDiyPageApi.DiyPage | MallDiyTemplateApi.DiyTemplateProperty;
}

/** 提交表单 */
async function submitForm() {
  const loadingInstance = ElLoading.service({
    text: '保存中...',
  });
  try {
    // 对所有的 templateItems 都进行保存，有缓存则保存缓存，解决都有修改时只保存了当前所编辑的 templateItem，导致装修效果存在差异
    for (const [i, templateItem] of templateItems.entries()) {
      const data = currentFormDataMap.value.get(templateItem.name) as any;
      // 情况一：基础设置
      if (i === 0) {
        // 提交模板属性
        await updateDiyTemplateProperty(isEmpty(data) ? formData.value! : data);
        continue;
      }
      // 提交页面属性
      // 情况二：提交当前正在编辑的页面
      if (currentFormData.value?.name.includes(templateItem.name)) {
        await updateDiyPageProperty(currentFormData.value!);
        continue;
      }
      // 情况三：提交页面编辑缓存
      if (!isEmpty(data)) {
        await updateDiyPageProperty(data!);
      }
    }
    ElMessage.success('保存成功');
  } finally {
    loadingInstance.close();
  }
}

/** 刷新当前 Tab */
function handleEditorReset() {
  sessionStorage.setItem(DIY_PAGE_INDEX_KEY, `${selectedTemplateItem.value}`);
  refreshTab();
}

/** 恢复当前 Tab 之前的 index（selectedTemplateItem） */
function recoverPageIndex() {
  // 恢复重置前的页面，默认是第一个页面
  const pageIndex = Number(sessionStorage.getItem(DIY_PAGE_INDEX_KEY)) || 0;
  // 移除标记
  sessionStorage.removeItem(DIY_PAGE_INDEX_KEY);

  // 重新初始化数据
  currentFormData.value = formData.value as
    | MallDiyPageApi.DiyPage
    | MallDiyTemplateApi.DiyTemplateProperty;
  currentFormDataMap.value = new Map<
    string,
    MallDiyPageApi.DiyPage | MallDiyTemplateApi.DiyTemplateProperty
  >();
  // 切换页面
  if (pageIndex !== selectedTemplateItem.value) {
    handleTemplateItemChange(pageIndex);
  }
}

/** 初始化 */
onMounted(async () => {
  if (!route.params.id) {
    ElMessage.warning('参数错误，页面编号不能为空！');
    return;
  }
  // 查询详情
  formData.value = {} as MallDiyTemplateApi.DiyTemplateProperty;
  await getPageDetail(route.params.id);
  // 恢复重置前的页面
  recoverPageIndex();
});
</script>
<template>
  <DiyEditor
    v-if="formData?.id"
    v-model="currentFormData!.property"
    :libs="libs"
    :preview-url="previewUrl"
    :show-navigation-bar="selectedTemplateItem !== 0"
    :show-page-config="selectedTemplateItem !== 0"
    :show-tab-bar="selectedTemplateItem === 0"
    :title="templateItems[selectedTemplateItem]?.name || ''"
    @reset="handleEditorReset"
    @save="submitForm"
  >
    <template #toolBarLeft>
      <ElRadioGroup
        :model-value="selectedTemplateItem"
        class="h-full!"
        @change="handleTemplateItemChange"
      >
        <ElTooltip
          v-for="(item, index) in templateItems"
          :key="index"
          :content="item.name"
        >
          <ElRadioButton :value="index">
            <IconifyIcon :icon="item.icon" :size="24" />
          </ElRadioButton>
        </ElTooltip>
      </ElRadioGroup>
    </template>
  </DiyEditor>
</template>
