<script setup lang="ts">
import { cloneDeep } from 'lodash-es'
import { ref, computed, CSSProperties, toRef, watch, defineEmits } from 'vue'
import {
  ElInput,
  ElPopover,
  ElDivider,
  ElScrollbar,
  ElTabs,
  ElTabPane,
  ElPagination
} from 'element-plus'
import { IconJson } from '@/components/Icon/src/data'

type ParameterCSSProperties = (item?: string) => CSSProperties | undefined

const props = defineProps({
  modelValue: {
    require: false,
    type: String
  }
})
const emit = defineEmits<{ (e: 'update:modelValue', v: string) }>()

const visible = ref(false)
const inputValue = toRef(props, 'modelValue')
const iconList = ref(IconJson)
const icon = ref('add-location')
const currentActiveType = ref('ep:')
// 深拷贝图标数据，前端做搜索
const copyIconList = cloneDeep(iconList.value)

const pageSize = ref(96)
const currentPage = ref(1)

// 搜索条件
const filterValue = ref('')

const tabsList = [
  {
    label: 'Element Plus',
    name: 'ep:'
  },
  {
    label: 'Font Awesome 4',
    name: 'fa:'
  },
  {
    label: 'Font Awesome 5 Solid',
    name: 'fa-solid:'
  }
]

const pageList = computed(() => {
  if (currentPage.value === 1) {
    return copyIconList[currentActiveType.value]
      .filter((v) => v.includes(filterValue.value))
      .slice(currentPage.value - 1, pageSize.value)
  } else {
    return copyIconList[currentActiveType.value]
      .filter((v) => v.includes(filterValue.value))
      .slice(
        pageSize.value * (currentPage.value - 1),
        pageSize.value * (currentPage.value - 1) + pageSize.value
      )
  }
})

const iconItemStyle = computed((): ParameterCSSProperties => {
  return (item) => {
    if (inputValue.value === currentActiveType.value + item) {
      return {
        borderColor: 'var(--el-color-primary)',
        color: 'var(--el-color-primary)'
      }
    }
  }
})

function handleClick({ props }) {
  currentPage.value = 1
  currentActiveType.value = props.name
  emit('update:modelValue', currentActiveType.value + iconList.value[currentActiveType.value][0])
  icon.value = iconList.value[currentActiveType.value][0]
}

function onChangeIcon(item) {
  icon.value = item
  emit('update:modelValue', currentActiveType.value + item)
  visible.value = false
}

function onCurrentChange(page) {
  currentPage.value = page
}

watch(
  () => {
    return props.modelValue
  },
  () => {
    if (props.modelValue) {
      currentActiveType.value = props.modelValue.substring(0, props.modelValue.indexOf(':') + 1)
      icon.value = props.modelValue.substring(props.modelValue.indexOf(':') + 1)
    }
  }
)
watch(
  () => {
    return filterValue.value
  },
  () => {
    currentPage.value = 1
  }
)
</script>

<template>
  <div class="selector">
    <ElInput v-model="inputValue" @click="visible = !visible">
      <template #append>
        <ElPopover
          :width="350"
          trigger="click"
          popper-class="pure-popper"
          :popper-options="{
            placement: 'auto'
          }"
          :visible="visible"
        >
          <template #reference>
            <div
              class="w-40px h-32px cursor-pointer flex justify-center items-center"
              @click="visible = !visible"
            >
              <Icon :icon="currentActiveType + icon" />
            </div>
          </template>

          <ElInput class="p-2" v-model="filterValue" placeholder="搜索图标" clearable />
          <ElDivider border-style="dashed" />

          <ElTabs v-model="currentActiveType" @tab-click="handleClick">
            <ElTabPane
              v-for="(pane, index) in tabsList"
              :key="index"
              :label="pane.label"
              :name="pane.name"
            >
              <ElDivider class="tab-divider" border-style="dashed" />
              <ElScrollbar height="220px">
                <ul class="flex flex-wrap px-2 ml-2">
                  <li
                    v-for="(item, key) in pageList"
                    :key="key"
                    :title="item"
                    class="icon-item p-2 w-1/10 cursor-pointer mr-2 mt-1 flex justify-center items-center border border-solid"
                    :style="iconItemStyle(item)"
                    @click="onChangeIcon(item)"
                  >
                    <Icon :icon="currentActiveType + item" />
                  </li>
                </ul>
              </ElScrollbar>
            </ElTabPane>
          </ElTabs>
          <ElDivider border-style="dashed" />

          <ElPagination
            small
            :total="copyIconList[currentActiveType].length"
            :page-size="pageSize"
            :current-page="currentPage"
            background
            layout="prev, pager, next"
            class="flex items-center justify-center h-10"
            @current-change="onCurrentChange"
          />
        </ElPopover>
      </template>
    </ElInput>
  </div>
</template>

<style lang="less" scoped>
.el-divider--horizontal {
  margin: 1px auto !important;
}

.tab-divider.el-divider--horizontal {
  margin: 0 !important;
}

.icon-item {
  &:hover {
    border-color: var(--el-color-primary);
    color: var(--el-color-primary);
    transition: all 0.4s;
    transform: scaleX(1.05);
  }
}

:deep(.el-tabs__nav-next) {
  font-size: 15px;
  line-height: 32px;
  box-shadow: -5px 0 5px -6px #ccc;
}

:deep(.el-tabs__nav-prev) {
  font-size: 15px;
  line-height: 32px;
  box-shadow: 5px 0 5px -6px #ccc;
}

:deep(.el-input-group__append) {
  padding: 0;
}

:deep(.el-tabs__item) {
  font-size: 12px;
  font-weight: normal;
  height: 30px;
  line-height: 30px;
}

:deep(.el-tabs__header),
:deep(.el-tabs__nav-wrap) {
  margin: 0;
  position: static;
}
</style>
