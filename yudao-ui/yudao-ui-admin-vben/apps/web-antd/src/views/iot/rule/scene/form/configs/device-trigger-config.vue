<!-- 设备触发配置组件 -->
<script setup lang="ts">
import type { RuleSceneApi } from '#/api/iot/rule/scene';

import { nextTick } from 'vue';

import { IconifyIcon } from '@vben/icons';

import { useVModel } from '@vueuse/core';
import { Button, Tag } from 'ant-design-vue';

import MainConditionInnerConfig from './main-condition-inner-config.vue';
import SubConditionGroupConfig from './sub-condition-group-config.vue';

/** 设备触发配置组件 */
defineOptions({ name: 'DeviceTriggerConfig' });

const props = defineProps<{
  index: number;
  modelValue: RuleSceneApi.Trigger;
}>();

const emit = defineEmits<{
  (e: 'update:modelValue', value: RuleSceneApi.Trigger): void;
  (e: 'triggerTypeChange', type: number): void;
}>();

const trigger = useVModel(props, 'modelValue', emit);

const maxSubGroups = 3; // 最多 3 个子条件组
const maxConditionsPerGroup = 3; // 每组最多 3 个条件

/**
 * 更新条件
 * @param condition 条件对象
 */
function updateCondition(condition: RuleSceneApi.Trigger) {
  trigger.value = condition;
}

/**
 * 处理触发器类型变化事件
 * @param type 触发器类型
 */
function handleTriggerTypeChange(type: number) {
  trigger.value.type = type.toString();
  emit('triggerTypeChange', type);
}

/** 添加子条件组 */
async function addSubGroup() {
  if (!trigger.value.conditionGroups) {
    trigger.value.conditionGroups = [];
  }

  // 检查是否达到最大子组数量限制
  if (trigger.value.conditionGroups?.length >= maxSubGroups) {
    return;
  }

  // 使用 nextTick 确保响应式更新完成后再添加新的子组
  await nextTick();
  if (trigger.value.conditionGroups) {
    trigger.value.conditionGroups.push([] as any);
  }
}

/**
 * 移除子条件组
 * @param index 子条件组索引
 */
function removeSubGroup(index: number) {
  if (trigger.value.conditionGroups) {
    trigger.value.conditionGroups.splice(index, 1);
  }
}

/**
 * 更新子条件组
 * @param index 子条件组索引
 * @param subGroup 子条件组数据
 */
function updateSubGroup(index: number, subGroup: any) {
  if (trigger.value.conditionGroups) {
    trigger.value.conditionGroups[index] = subGroup;
  }
}

/** 移除整个条件组 */
function removeConditionGroup() {
  trigger.value.conditionGroups = undefined;
}
</script>

<template>
  <div class="gap-16px flex flex-col">
    <!-- 主条件配置 - 默认直接展示 -->
    <div class="space-y-16px">
      <!-- 主条件配置 -->
      <div class="gap-16px flex flex-col">
        <!-- 主条件配置 -->
        <div class="space-y-16px">
          <!-- 主条件头部 - 与附加条件组保持一致的绿色风格 -->
          <div
            class="p-16px rounded-8px flex items-center justify-between border border-green-200 bg-gradient-to-r from-green-50 to-emerald-50"
          >
            <div class="gap-12px flex items-center">
              <div
                class="gap-8px text-16px font-600 flex items-center text-green-700"
              >
                <div
                  class="w-24px h-24px text-12px flex items-center justify-center rounded-full bg-green-500 font-bold text-white"
                >
                  主
                </div>
                <span>主条件</span>
              </div>
              <el-tag size="small" type="success">必须满足</el-tag>
            </div>
          </div>

          <!-- 主条件内容配置 -->
          <MainConditionInnerConfig
            :model-value="trigger"
            @update:model-value="updateCondition"
            :trigger-type="trigger.type as any"
            @trigger-type-change="handleTriggerTypeChange"
          />
        </div>
      </div>
    </div>

    <!-- 条件组配置 -->
    <div class="space-y-16px">
      <!-- 条件组配置 -->
      <div class="gap-16px flex flex-col">
        <!-- 条件组容器头部 -->
        <div
          class="p-16px rounded-8px flex items-center justify-between border border-green-200 bg-gradient-to-r from-green-50 to-emerald-50"
        >
          <div class="gap-12px flex items-center">
            <div
              class="gap-8px text-16px font-600 flex items-center text-green-700"
            >
              <div
                class="w-24px h-24px text-12px flex items-center justify-center rounded-full bg-green-500 font-bold text-white"
              >
                组
              </div>
              <span>附加条件组</span>
            </div>
            <el-tag size="small" type="success">与"主条件"为且关系</el-tag>
            <el-tag size="small" type="info">
              {{ trigger.conditionGroups?.length || 0 }} 个子条件组
            </el-tag>
          </div>
          <div class="gap-8px flex items-center">
            <Button
              type="primary"
              size="small"
              @click="addSubGroup"
              :disabled="(trigger.conditionGroups?.length || 0) >= maxSubGroups"
            >
              <IconifyIcon icon="lucide:plus" />
              添加子条件组
            </Button>
            <Button danger size="small" text @click="removeConditionGroup">
              <IconifyIcon icon="lucide:trash-2" />
              删除条件组
            </Button>
          </div>
        </div>

        <!-- 子条件组列表 -->
        <div
          v-if="trigger.conditionGroups && trigger.conditionGroups.length > 0"
          class="space-y-16px"
        >
          <!-- 逻辑关系说明 -->
          <div class="relative">
            <div
              v-for="(subGroup, subGroupIndex) in trigger.conditionGroups"
              :key="`sub-group-${subGroupIndex}`"
              class="relative"
            >
              <!-- 子条件组容器 -->
              <div
                class="rounded-8px border-2 border-orange-200 bg-orange-50 shadow-sm transition-shadow hover:shadow-md"
              >
                <div
                  class="p-16px rounded-t-6px flex items-center justify-between border-b border-orange-200 bg-gradient-to-r from-orange-50 to-yellow-50"
                >
                  <div class="gap-12px flex items-center">
                    <div
                      class="gap-8px text-16px font-600 flex items-center text-orange-700"
                    >
                      <div
                        class="w-24px h-24px text-12px flex items-center justify-center rounded-full bg-orange-500 font-bold text-white"
                      >
                        {{ subGroupIndex + 1 }}
                      </div>
                      <span>子条件组 {{ subGroupIndex + 1 }}</span>
                    </div>
                    <Tag size="small" type="warning" class="font-500">
                      组内条件为"且"关系
                    </Tag>
                    <Tag size="small" type="info">
                      {{ (subGroup as any)?.length || 0 }}个条件
                    </Tag>
                  </div>
                  <Button
                    danger
                    size="small"
                    text
                    @click="removeSubGroup(subGroupIndex)"
                    class="hover:bg-red-50"
                  >
                    <IconifyIcon icon="lucide:trash-2" />
                    删除组
                  </Button>
                </div>

                <SubConditionGroupConfig
                  :model-value="subGroup as any"
                  @update:model-value="
                    (value) => updateSubGroup(subGroupIndex, value)
                  "
                  :trigger-type="trigger.type as any"
                  :max-conditions="maxConditionsPerGroup"
                />
              </div>

              <!-- 子条件组间的'或'连接符 -->
              <div
                v-if="subGroupIndex < trigger.conditionGroups!.length - 1"
                class="py-12px flex items-center justify-center"
              >
                <div class="gap-8px flex items-center">
                  <!-- 连接线 -->
                  <div class="w-32px h-1px bg-orange-300"></div>
                  <!-- 或标签 -->
                  <div
                    class="px-16px py-6px rounded-full border-2 border-orange-300 bg-orange-100"
                  >
                    <span class="text-14px font-600 text-orange-600">或</span>
                  </div>
                  <!-- 连接线 -->
                  <div class="w-32px h-1px bg-orange-300"></div>
                </div>
              </div>
            </div>
          </div>
        </div>

        <!-- 空状态 -->
        <div
          v-else
          class="p-24px rounded-8px border-2 border-dashed border-orange-200 bg-orange-50 text-center"
        >
          <div class="gap-12px flex flex-col items-center">
            <IconifyIcon icon="lucide:plus" class="text-32px text-orange-400" />
            <div class="text-orange-600">
              <p class="text-14px font-500 mb-4px">暂无子条件组</p>
              <p class="text-12px">点击上方"添加子条件组"按钮开始配置</p>
            </div>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>
