<script lang="ts" setup>
import type { BpmCategoryApi } from '#/api/bpm/category';
import type { BpmProcessDefinitionApi } from '#/api/bpm/definition';

import { computed, nextTick, onMounted, ref, watch } from 'vue';
import { useRoute } from 'vue-router';

import { Page } from '@vben/common-ui';
import { groupBy } from '@vben/utils';

import {
  ElCard,
  ElCol,
  ElInput,
  ElMessage,
  ElRow,
  ElSpace,
  ElTabPane,
  ElTabs,
  ElTooltip,
} from 'element-plus';

import { getCategorySimpleList } from '#/api/bpm/category';
import { getProcessDefinitionList } from '#/api/bpm/definition';
import { getProcessInstance } from '#/api/bpm/processInstance';

import ProcessDefinitionDetail from './modules/form.vue';

defineOptions({ name: 'BpmProcessInstanceCreate' });

const route = useRoute();

const loading = ref(true); // 加载中
const processInstanceId: any = route.query.processInstanceId; // 流程实例编号。场景：重新发起时

const categoryList: any = ref([]); // 分类的列表
const activeCategory = ref(''); // 当前选中的分类

const searchName = ref(''); // 当前搜索关键字
const processDefinitionList = ref<BpmProcessDefinitionApi.ProcessDefinition[]>(
  [],
); // 流程定义的列表
const filteredProcessDefinitionList = ref<
  BpmProcessDefinitionApi.ProcessDefinition[]
>([]); // 用于存储搜索过滤后的流程定义

const selectProcessDefinition = ref(); // 选择的流程定义
const processDefinitionDetailRef = ref();

/** 查询列表 */
async function getList() {
  loading.value = true;
  try {
    // 1.1 所有流程分类数据
    await loadCategoryList();
    // 1.2 所有流程定义数据
    await loadProcessDefinitionList();

    // 2. 如果 processInstanceId 非空，说明是重新发起
    if (processInstanceId?.length > 0) {
      const processInstance = await getProcessInstance(processInstanceId);
      if (!processInstance) {
        ElMessage.error('重新发起流程失败，原因：流程实例不存在');
        return;
      }
      const processDefinition = processDefinitionList.value.find(
        (item: any) => item.key === processInstance.processDefinition?.key,
      );
      if (!processDefinition) {
        ElMessage.error('重新发起流程失败，原因：流程定义不存在');
        return;
      }
      await handleSelect(processDefinition, processInstance.formVariables);
    }
  } finally {
    loading.value = false;
  }
}

/** 获取所有流程分类数据 */
async function loadCategoryList() {
  categoryList.value = await getCategorySimpleList();
}

/** 获取所有流程定义数据 */
async function loadProcessDefinitionList() {
  // 流程定义
  processDefinitionList.value = await getProcessDefinitionList({
    suspensionState: 1,
  });

  // 空搜索，初始化相关数据
  handleQuery();
}

/** 搜索流程 */
function handleQuery() {
  if (searchName.value.trim()) {
    // 如果有搜索关键字，进行过滤
    filteredProcessDefinitionList.value = processDefinitionList.value.filter(
      (definition: any) =>
        definition.name.toLowerCase().includes(searchName.value.toLowerCase()),
    );
    // 如果有匹配，切换到第一个包含匹配结果的分类
    activeCategory.value = availableCategories.value[0]?.name;
  } else {
    // 如果没有搜索关键字，恢复所有数据
    filteredProcessDefinitionList.value = processDefinitionList.value;
    // 恢复到第一个可用分类
    if (availableCategories.value.length > 0) {
      activeCategory.value = availableCategories.value[0].code;
    }
  }
}

/** 流程定义的分组 */
const processDefinitionGroup = computed(() => {
  if (!processDefinitionList.value?.length) {
    return {};
  }
  // 按照 categoryList 的顺序重新组织数据
  const grouped = groupBy(filteredProcessDefinitionList.value, 'category');
  const orderedGroup: Record<
    string,
    BpmProcessDefinitionApi.ProcessDefinition[]
  > = {};
  categoryList.value.forEach((category: BpmCategoryApi.Category) => {
    if (grouped[category.code]) {
      orderedGroup[category.code] = grouped[
        category.code
      ] as BpmProcessDefinitionApi.ProcessDefinition[];
    }
  });
  return orderedGroup;
});

/** 处理选择流程的按钮操作 */
async function handleSelect(
  row: BpmProcessDefinitionApi.ProcessDefinition,
  formVariables?: any,
) {
  // 设置选择的流程
  selectProcessDefinition.value = row;
  // 初始化流程定义详情
  await nextTick();
  processDefinitionDetailRef.value?.initProcessInfo(row, formVariables);
}

/** 过滤出有流程的分类列表。目的：只展示有流程的分类 */
const availableCategories = computed(() => {
  if (!categoryList.value?.length || !processDefinitionGroup.value) {
    return [];
  }
  // 获取所有有流程的分类代码
  const availableCategoryCodes = Object.keys(processDefinitionGroup.value);
  // 过滤出有流程的分类
  return categoryList.value.filter((category: BpmCategoryApi.Category) =>
    availableCategoryCodes.includes(category.code),
  );
});

/** 监听可用分类变化，自动设置正确的活动分类 */
watch(
  availableCategories,
  (newCategories) => {
    if (newCategories.length > 0) {
      // 如果当前活动分类不在可用分类中，切换到第一个可用分类
      const currentCategoryExists = newCategories.some(
        (category: BpmCategoryApi.Category) =>
          category.code === activeCategory.value,
      );
      if (!currentCategoryExists) {
        activeCategory.value = newCategories[0].code;
      }
    }
  },
  { immediate: true },
);

/** 初始化 */
onMounted(() => {
  getList();
});
</script>

<template>
  <Page auto-content-height>
    <!-- 第一步，通过流程定义的列表，选择对应的流程 -->
    <template v-if="!selectProcessDefinition">
      <ElCard
        class="h-full"
        shadow="never"
        :class="{
          'process-definition-container': filteredProcessDefinitionList?.length,
        }"
        v-loading="loading"
      >
        <template #header>
          <div class="flex items-center justify-between">
            <span class="text-lg font-medium">全部流程</span>
            <div class="w-1/3">
              <ElInput
                v-model="searchName"
                placeholder="请输入流程名称检索"
                clearable
                @input="handleQuery"
                @clear="handleQuery"
              />
            </div>
          </div>
        </template>

        <div v-if="filteredProcessDefinitionList?.length" class="h-full">
          <ElTabs v-model="activeCategory" tab-position="left" class="h-full">
            <ElTabPane
              v-for="category in availableCategories"
              :key="category.code"
              :label="category.name"
              :name="category.code"
              class="h-full"
            >
              <div class="px-4">
                <ElRow :gutter="16">
                  <ElCol
                    v-for="definition in processDefinitionGroup[category.code]"
                    :key="definition.id"
                    :xs="24"
                    :sm="12"
                    :md="8"
                    :lg="8"
                    :xl="6"
                    class="mb-4"
                    @click="handleSelect(definition)"
                  >
                    <ElCard
                      shadow="hover"
                      class="definition-item-card w-full cursor-pointer"
                      :class="{
                        'search-match': searchName.trim().length > 0,
                      }"
                      :body-style="{
                        width: '100%',
                        padding: '16px',
                      }"
                    >
                      <div class="flex items-center">
                        <img
                          v-if="definition.icon"
                          :src="definition.icon"
                          class="flow-icon-img object-contain"
                          alt="流程图标"
                        />
                        <div v-else class="flow-icon flex-shrink-0">
                          <span class="text-xs text-white">
                            {{ definition.name?.slice(0, 2) }}
                          </span>
                        </div>
                        <span class="ml-3 flex-1 truncate text-base">
                          <ElTooltip
                            placement="top-start"
                            :content="definition.description"
                          >
                            {{ definition.name }}
                          </ElTooltip>
                        </span>
                      </div>
                    </ElCard>
                  </ElCol>
                </ElRow>
              </div>
            </ElTabPane>
          </ElTabs>
        </div>
        <div v-else class="!py-48 text-center">
          <ElSpace direction="vertical" size="large">
            <span class="text-gray-500">没有找到搜索结果</span>
          </ElSpace>
        </div>
      </ElCard>
    </template>

    <!-- 第二步，填写表单，进行流程的提交 -->
    <ProcessDefinitionDetail
      v-else
      ref="processDefinitionDetailRef"
      :select-process-definition="selectProcessDefinition"
      @cancel="selectProcessDefinition = undefined"
    />
  </Page>
</template>

<style lang="scss" scoped>
@keyframes bounce {
  0%,
  50% {
    transform: translateY(-5px);
  }

  100% {
    transform: translateY(0);
  }
}

.process-definition-container {
  .definition-item-card {
    .flow-icon-img {
      width: 48px;
      height: 48px;
      border-radius: 0.25rem;
    }

    .flow-icon {
      @apply bg-primary;

      display: flex;
      align-items: center;
      justify-content: center;
      width: 48px;
      height: 48px;
      border-radius: 0.25rem;
    }

    &.search-match {
      background-color: rgb(63 115 247 / 10%);
      border: 1px solid var(--primary);
      animation: bounce 0.5s ease;
    }
  }
}

:deep(.el-tabs__content) {
  height: 100%;
  overflow-y: auto;
}

:deep(.el-tabs) {
  height: 100%;
}
</style>
