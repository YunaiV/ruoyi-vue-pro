<script lang="ts" setup>
import type { ModelCategoryInfo } from '#/api/bpm/model';

import { onActivated, reactive, ref, useTemplateRef, watch } from 'vue';

import { Page, useVbenModal } from '@vben/common-ui';
import { IconifyIcon } from '@vben/icons';
import { cloneDeep } from '@vben/utils';

import { useSortable } from '@vueuse/integrations/useSortable';
import { Button, Card, Dropdown, Input, Menu, message } from 'ant-design-vue';

import {
  getCategorySimpleList,
  updateCategorySortBatch,
} from '#/api/bpm/category';
import { getModelList } from '#/api/bpm/model';
import { router } from '#/router';

// 流程分类对话框
import CategoryForm from '../category/modules/form.vue';
import CategoryDraggableModel from './modules/category-draggable-model.vue';

// 新建流程分类对话框
const [CategoryFormModal, categoryFormModalApi] = useVbenModal({
  connectedComponent: CategoryForm,
  destroyOnClose: true,
});
// 模型列表加载状态
const modelListSpinning = ref(false);
// 保存排序状态
const saveSortLoading = ref(false);
// 按照 category 分组的数据
const categoryGroup = ref<ModelCategoryInfo[]>([]);
// 未排序前的原始数据
const originalData = ref<ModelCategoryInfo[]>([]);
// 可以排序元素的容器
const sortable = useTemplateRef<HTMLElement>('categoryGroupRef');
// 排序引用，以便后续启用或禁用排序
const sortableInstance = ref<any>(null);
// 分类排序状态
const isCategorySorting = ref(false);
// 查询参数
const queryParams = reactive({
  name: '',
});

// 监听分类排序模式切换
watch(
  () => isCategorySorting.value,
  (newValue) => {
    if (sortableInstance.value) {
      if (newValue) {
        // 启用排序功能
        sortableInstance.value.option('disabled', false);
      } else {
        // 禁用排序功能
        sortableInstance.value.option('disabled', true);
      }
    }
  },
);

/** 加载数据 */
async function getList() {
  modelListSpinning.value = true;
  try {
    const modelList = await getModelList(queryParams.name);
    const categoryList = await getCategorySimpleList();
    // 按照 category 聚合
    categoryGroup.value = categoryList.map((category: any) => ({
      ...category,
      modelList: modelList.filter(
        (model: any) => model.categoryName === category.name,
      ),
    }));
    // 重置排序实例
    sortableInstance.value = null;
  } finally {
    modelListSpinning.value = false;
  }
}

/** 初始化 */
onActivated(() => {
  getList();
});

/** 新增模型 */
function createModel() {
  router.push({
    name: 'BpmModelCreate',
  });
}

/** 处理下拉菜单命令 */
function handleCommand(command: string) {
  if (command === 'handleCategoryAdd') {
    // 打开新建流程分类弹窗
    categoryFormModalApi.open();
  } else if (command === 'handleCategorySort') {
    originalData.value = cloneDeep(categoryGroup.value);
    isCategorySorting.value = true;
    // 如果排序实例不存在，则初始化
    if (sortableInstance.value) {
      // 已存在实例，则启用排序功能
      sortableInstance.value.option('disabled', false);
    } else {
      sortableInstance.value = useSortable(sortable, categoryGroup, {
        disabled: false, // 启用排序
      });
    }
  }
}

/** 取消分类排序 */
function handleCategorySortCancel() {
  // 恢复初始数据
  categoryGroup.value = cloneDeep(originalData.value);
  isCategorySorting.value = false;
  // 直接禁用排序功能
  if (sortableInstance.value) {
    sortableInstance.value.option('disabled', true);
  }
}

/** 提交分类排序 */
async function handleCategorySortSubmit() {
  saveSortLoading.value = true;
  try {
    // 保存排序逻辑
    const ids = categoryGroup.value.map((item: any) => item.id);
    await updateCategorySortBatch(ids);
  } finally {
    saveSortLoading.value = false;
  }
  message.success('分类排序成功');
  isCategorySorting.value = false;
  // 刷新列表
  await getList();
  // 禁用排序功能
  if (sortableInstance.value) {
    sortableInstance.value.option('disabled', true);
  }
}
</script>

<template>
  <Page auto-content-height>
    <!-- 流程分类表单弹窗 -->
    <CategoryFormModal @success="getList" />
    <Card
      :body-style="{ padding: '10px' }"
      class="mb-4"
      title="流程模型"
      v-spinning="modelListSpinning"
    >
      <template #extra>
        <Input
          v-model:value="queryParams.name"
          placeholder="搜索流程"
          allow-clear
          @press-enter="getList"
          class="!w-60"
        />
        <Button class="ml-2" type="primary" @click="createModel">
          <IconifyIcon icon="lucide:plus" /> 新建模型
        </Button>
        <Dropdown class="ml-2" placement="bottomRight" arrow>
          <Button>
            <template #icon>
              <div class="flex items-center justify-center">
                <IconifyIcon icon="lucide:settings" />
              </div>
            </template>
          </Button>
          <template #overlay>
            <Menu @click="(e) => handleCommand(e.key as string)">
              <Menu.Item key="handleCategoryAdd">
                <div class="flex items-center gap-1">
                  <IconifyIcon icon="lucide:plus" />
                  新建分类
                </div>
              </Menu.Item>
              <Menu.Item key="handleCategorySort">
                <div class="flex items-center gap-1">
                  <IconifyIcon icon="lucide:align-start-vertical" />
                  分类排序
                </div>
              </Menu.Item>
            </Menu>
          </template>
        </Dropdown>
      </template>
      <div class="flex h-full items-center justify-between pl-5">
        <div class="mb-4 mr-6" v-if="isCategorySorting">
          <Button @click="handleCategorySortCancel" class="mr-3">
            取 消
          </Button>
          <Button
            type="primary"
            :loading="saveSortLoading"
            @click="handleCategorySortSubmit"
          >
            保存排序
          </Button>
        </div>
      </div>

      <!-- 按照分类，展示其所属的模型列表 -->
      <div class="px-3" ref="categoryGroupRef">
        <CategoryDraggableModel
          v-for="(element, index) in categoryGroup"
          :class="isCategorySorting ? 'cursor-move' : ''"
          :key="element.id"
          :category-info="element"
          :is-category-sorting="isCategorySorting"
          :is-first="index === 0"
          @success="getList"
        />
      </div>
    </Card>
  </Page>
</template>
