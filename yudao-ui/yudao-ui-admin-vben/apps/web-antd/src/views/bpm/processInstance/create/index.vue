<script lang="ts" setup>
import type { BpmCategoryApi } from '#/api/bpm/category';
import type { BpmProcessDefinitionApi } from '#/api/bpm/definition';

import { computed, nextTick, onMounted, ref, watch } from 'vue';
import { useRoute } from 'vue-router';

import { Page } from '@vben/common-ui';

import {
  Card,
  Col,
  InputSearch,
  message,
  Row,
  Space,
  Tabs,
  Tooltip,
} from 'ant-design-vue';

import { getCategorySimpleList } from '#/api/bpm/category';
import { getProcessDefinitionList } from '#/api/bpm/definition';
import { getProcessInstance } from '#/api/bpm/processInstance';

import ProcessDefinitionDetail from './modules/form.vue';

defineOptions({ name: 'BpmProcessInstanceCreate' });

const route = useRoute();

// 当前搜索关键字
const searchName = ref('');
const isSearching = ref(false);
// 流程实例编号。场景：重新发起时
const processInstanceId: any = route.query.processInstanceId;
// 加载中
const loading = ref(true);
// 分类的列表
const categoryList: any = ref([]);
// 当前选中的分类
const activeCategory = ref('');
// 流程定义的列表
const processDefinitionList = ref<BpmProcessDefinitionApi.ProcessDefinition[]>(
  [],
);

// 实现 groupBy 功能
function groupBy(array: any[], key: string) {
  const result: Record<string, any[]> = {};
  for (const item of array) {
    const groupKey = item[key];
    if (!result[groupKey]) {
      result[groupKey] = [];
    }
    result[groupKey].push(item);
  }
  return result;
}

/** 查询列表 */
async function getList() {
  loading.value = true;
  try {
    // 所有流程分类数据
    await getCategoryList();
    // 所有流程定义数据
    await handleGetProcessDefinitionList();

    // 如果 processInstanceId 非空，说明是重新发起
    if (processInstanceId?.length > 0) {
      const processInstance = await getProcessInstance(processInstanceId);
      if (!processInstance) {
        message.error('重新发起流程失败，原因：流程实例不存在');
        return;
      }
      const processDefinition = processDefinitionList.value.find(
        (item: any) => item.key === processInstance.processDefinition?.key,
      );
      if (!processDefinition) {
        message.error('重新发起流程失败，原因：流程定义不存在');
        return;
      }
      await handleSelect(processDefinition, processInstance.formVariables);
    }
  } finally {
    loading.value = false;
  }
}

/** 获取所有流程分类数据 */
async function getCategoryList() {
  try {
    // 流程分类
    categoryList.value = await getCategorySimpleList();
  } catch {
    // 错误处理
  }
}

/** 获取所有流程定义数据 */
async function handleGetProcessDefinitionList() {
  try {
    // 流程定义
    processDefinitionList.value = await getProcessDefinitionList({
      suspensionState: 1,
    });
    // 初始化过滤列表为全部流程定义
    filteredProcessDefinitionList.value = processDefinitionList.value;

    // 在获取完所有数据后，设置第一个有效分类为激活状态
    if (availableCategories.value.length > 0 && !activeCategory.value) {
      activeCategory.value = availableCategories.value[0].code;
    }
  } catch {
    // 错误处理
  }
}

/** 用于存储搜索过滤后的流程定义 */
const filteredProcessDefinitionList = ref<
  BpmProcessDefinitionApi.ProcessDefinition[]
>([]);

/** 搜索流程 */
function handleQuery() {
  if (searchName.value.trim()) {
    // 如果有搜索关键字，进行过滤
    isSearching.value = true;
    filteredProcessDefinitionList.value = processDefinitionList.value.filter(
      (definition: any) =>
        definition.name.toLowerCase().includes(searchName.value.toLowerCase()),
    );

    // 获取搜索结果中的分类
    const searchResultGroups = groupBy(
      filteredProcessDefinitionList.value,
      'category',
    );
    const availableCategoryCodes = Object.keys(searchResultGroups);

    // 如果有匹配的分类，切换到第一个包含匹配结果的分类
    if (availableCategoryCodes.length > 0 && availableCategoryCodes[0]) {
      activeCategory.value = availableCategoryCodes[0];
    }
  } else {
    // 如果没有搜索关键字，恢复所有数据
    isSearching.value = false;
    filteredProcessDefinitionList.value = processDefinitionList.value;

    // 恢复到第一个可用分类
    if (availableCategories.value.length > 0) {
      activeCategory.value = availableCategories.value[0].code;
    }
  }
}

/** 判断流程定义是否匹配搜索 */
function isDefinitionMatchSearch(definition: any) {
  if (!isSearching.value) return false;
  return definition.name.toLowerCase().includes(searchName.value.toLowerCase());
}

/** 流程定义的分组 */
const processDefinitionGroup = computed(() => {
  if (!processDefinitionList.value?.length) {
    return {};
  }

  const grouped = groupBy(filteredProcessDefinitionList.value, 'category');
  // 按照 categoryList 的顺序重新组织数据
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

/** 通过分类 code 获取对应的名称 */
// eslint-disable-next-line no-unused-vars
function _getCategoryName(categoryCode: string) {
  return categoryList.value?.find((ctg: any) => ctg.code === categoryCode)
    ?.name;
}

// ========== 表单相关 ==========
const selectProcessDefinition = ref(); // 选择的流程定义
const processDefinitionDetailRef = ref();

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

/** 获取 tab 的位置 */
const tabPosition = computed(() => {
  return window.innerWidth < 768 ? 'top' : 'left';
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
    <!-- TODO @ziye：【优先级：低】这里交互，可以做成类似 vue3 + element-plus 那个一样，滚动切换分类哈？对标钉钉、飞书哈； -->
    <!-- 第一步，通过流程定义的列表，选择对应的流程 -->
    <template v-if="!selectProcessDefinition">
      <Card
        class="h-full"
        title="全部流程"
        :class="{
          'process-definition-container': filteredProcessDefinitionList?.length,
        }"
        :loading="loading"
      >
        <template #extra>
          <div class="flex h-full items-center justify-center">
            <InputSearch
              v-model:value="searchName"
              class="!w-50%"
              placeholder="请输入流程名称检索"
              allow-clear
              @input="handleQuery"
              @clear="handleQuery"
            />
          </div>
        </template>

        <div v-if="filteredProcessDefinitionList?.length">
          <Tabs v-model:active-key="activeCategory" :tab-position="tabPosition">
            <Tabs.TabPane
              v-for="category in availableCategories"
              :key="category.code"
              :tab="category.name"
            >
              <Row :gutter="[16, 16]" :wrap="true">
                <Col
                  v-for="definition in processDefinitionGroup[category.code]"
                  :key="definition.id"
                  :xs="24"
                  :sm="12"
                  :md="8"
                  :lg="8"
                  :xl="6"
                  @click="handleSelect(definition)"
                >
                  <Card
                    hoverable
                    class="definition-item-card w-full cursor-pointer"
                    :class="{
                      'search-match': isDefinitionMatchSearch(definition),
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
                        <Tooltip
                          placement="topLeft"
                          :title="`${definition.description}`"
                        >
                          {{ definition.name }}
                        </Tooltip>
                      </span>
                    </div>
                  </Card>
                </Col>
              </Row>
            </Tabs.TabPane>
          </Tabs>
        </div>
        <div v-else class="!py-48 text-center">
          <Space direction="vertical" size="large">
            <span class="text-gray-500">没有找到搜索结果</span>
          </Space>
        </div>
      </Card>
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

@keyframes bounce {
  0%,
  100% {
    transform: translateY(0);
  }

  50% {
    transform: translateY(-5px);
  }
}
</style>
