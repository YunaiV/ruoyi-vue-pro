<script lang="ts" setup>
import type { MallDiyPageApi } from '#/api/mall/promotion/diy/page';
import type { MallDiyTemplateApi } from '#/api/mall/promotion/diy/template';
import type { DiyComponentLibrary } from '#/views/mall/promotion/components'; // 商城的 DIY 组件，在 DiyEditor 目录下

import { onMounted, ref } from 'vue';
import { useRoute } from 'vue-router';

import { useTabs } from '@vben/hooks';
import { IconifyIcon } from '@vben/icons';
import { useAccessStore } from '@vben/stores';
import { isEmpty } from '@vben/utils';

import { message, Radio, RadioGroup } from 'ant-design-vue';

import { updateDiyPageProperty } from '#/api/mall/promotion/diy/page';
import {
  getDiyTemplateProperty,
  updateDiyTemplateProperty,
} from '#/api/mall/promotion/diy/template';
import { DiyEditor, PAGE_LIBS } from '#/views/mall/promotion/components';

/** 装修模板表单 */
defineOptions({ name: 'DiyTemplateDecorate' });

const route = useRoute();
const { refreshTab } = useTabs();

const domain = import.meta.env.VITE_MALL_H5_DOMAIN;
// 特殊：存储 reset 重置时，当前 selectedTemplateItem 值，从而进行恢复
const DIY_PAGE_INDEX_KEY = 'diy_page_index';

const selectedTemplateItem = ref(0);
// 左上角工具栏操作按钮
const templateItems = ref([
  { key: 0, name: '基础设置', icon: 'lucide:settings' },
  { key: 1, name: '首页', icon: 'lucide:home' },
  { key: 2, name: '我的', icon: 'lucide:user' },
]);

const formData = ref<MallDiyTemplateApi.DiyTemplateProperty>();
// 当前编辑的属性
const currentFormData = ref<
  MallDiyPageApi.DiyPage | MallDiyTemplateApi.DiyTemplateProperty
>({
  property: '',
} as MallDiyPageApi.DiyPage);
// templateItem 对应的缓存
const currentFormDataMap = ref<
  Map<string, MallDiyPageApi.DiyPage | MallDiyTemplateApi.DiyTemplateProperty>
>(new Map());
// 商城 H5 预览地址
const previewUrl = ref('');
// 模板组件库
const templateLibs = [] as DiyComponentLibrary[];
// 当前组件库
const libs = ref<DiyComponentLibrary[]>(templateLibs);

/** 获取详情 */
async function getPageDetail(id: any) {
  const hideLoading = message.loading({
    content: '加载中...',
    duration: 0,
  });
  try {
    formData.value = await getDiyTemplateProperty(id);
    // 拼接手机预览链接
    const accessStore = useAccessStore();
    previewUrl.value = `${domain}?templateId=${formData.value.id}&tenantId=${accessStore.tenantId}`;
  } finally {
    hideLoading();
  }
}

/** 模板选项切换 */
function handleTemplateItemChange(val: any) {
  const changeValue = val.target.value;
  // 缓存模版编辑数据
  currentFormDataMap.value.set(
    templateItems.value[changeValue]!.name,
    currentFormData.value!,
  );
  // 切换模版
  selectedTemplateItem.value = changeValue;

  // 读取模版缓存
  const data = currentFormDataMap.value.get(
    templateItems.value[changeValue]!.name,
  );

  // 情况一：编辑模板
  if (changeValue === 0) {
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
            page.name === templateItems.value[changeValue]!.name,
        )
      : data
  ) as MallDiyPageApi.DiyPage | MallDiyTemplateApi.DiyTemplateProperty;
}

/** 提交表单 */
async function submitForm() {
  const hideLoading = message.loading({
    content: '保存中...',
    duration: 0,
  });
  try {
    // 对所有的 templateItems 都进行保存，有缓存则保存缓存，解决都有修改时只保存了当前所编辑的 templateItem，导致装修效果存在差异
    for (const [i, templateItem] of templateItems.value.entries()) {
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
    message.success('保存成功');
  } finally {
    hideLoading();
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
    message.warning('参数错误，页面编号不能为空！');
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
      <RadioGroup
        :value="selectedTemplateItem"
        class="flex items-center"
        size="large"
        @change="handleTemplateItemChange"
      >
        <template v-for="item in templateItems" :key="item.key">
          <Radio.Button :value="item.key">
            <IconifyIcon
              :icon="item.icon"
              class="mt-2 flex size-5 items-center"
            />
          </Radio.Button>
        </template>
      </RadioGroup>
    </template>
  </DiyEditor>
</template>
