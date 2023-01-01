<script setup lang="ts">
import dayjs from 'dayjs'
import { ElCollapseTransition, ElDescriptions, ElDescriptionsItem, ElTooltip } from 'element-plus'
import { useDesign } from '@/hooks/web/useDesign'
import { propTypes } from '@/utils/propTypes'
import { ref, unref, PropType, computed, useAttrs, useSlots } from 'vue'
import { useAppStore } from '@/store/modules/app'
import { DescriptionsSchema } from '@/types/descriptions'

const appStore = useAppStore()

const mobile = computed(() => appStore.getMobile)

const attrs = useAttrs()

const slots = useSlots()

const props = defineProps({
  title: propTypes.string.def(''),
  message: propTypes.string.def(''),
  collapse: propTypes.bool.def(true),
  columns: propTypes.number.def(1),
  schema: {
    type: Array as PropType<DescriptionsSchema[]>,
    default: () => []
  },
  data: {
    type: Object as PropType<Recordable>,
    default: () => ({})
  }
})

const { getPrefixCls } = useDesign()

const prefixCls = getPrefixCls('descriptions')

const getBindValue = computed(() => {
  const delArr: string[] = ['title', 'message', 'collapse', 'schema', 'data', 'class']
  const obj = { ...attrs, ...props }
  for (const key in obj) {
    if (delArr.indexOf(key) !== -1) {
      delete obj[key]
    }
  }
  return obj
})

const getBindItemValue = (item: DescriptionsSchema) => {
  const delArr: string[] = ['field']
  const obj = { ...item }
  for (const key in obj) {
    if (delArr.indexOf(key) !== -1) {
      delete obj[key]
    }
  }
  return obj
}

// 折叠
const show = ref(true)

const toggleClick = () => {
  if (props.collapse) {
    show.value = !unref(show)
  }
}
</script>

<template>
  <div
    :class="[
      prefixCls,
      'bg-[var(--el-color-white)] dark:(bg-[var(--el-bg-color)] border-[var(--el-border-color)] border-1px)'
    ]"
  >
    <div
      v-if="title"
      :class="[
        `${prefixCls}-header`,
        'h-50px flex justify-between items-center mb-10px border-bottom-1 border-solid border-[var(--tags-view-border-color)] px-10px cursor-pointer dark:border-[var(--el-border-color)]'
      ]"
      @click="toggleClick"
    >
      <div :class="[`${prefixCls}-header__title`, 'relative font-18px font-bold ml-10px']">
        <div class="flex items-center">
          {{ title }}
          <ElTooltip v-if="message" :content="message" placement="right">
            <Icon icon="ep:warning" class="ml-5px" />
          </ElTooltip>
        </div>
      </div>
      <Icon v-if="collapse" :icon="show ? 'ep:arrow-down' : 'ep:arrow-up'" />
    </div>

    <ElCollapseTransition>
      <div v-show="show" :class="[`${prefixCls}-content`, 'p-10px']">
        <ElDescriptions
          :column="props.columns"
          border
          :direction="mobile ? 'vertical' : 'horizontal'"
          v-bind="getBindValue"
        >
          <template v-if="slots['extra']" #extra>
            <slot name="extra"></slot>
          </template>
          <ElDescriptionsItem
            v-for="item in schema"
            :key="item.field"
            min-width="80"
            v-bind="getBindItemValue(item)"
          >
            <template #label>
              <slot :name="`${item.field}-label`" :label="item.label">{{ item.label }}</slot>
            </template>

            <template #default>
              <slot v-if="item.dateFormat">
                {{
                  data[item.field] !== null ? dayjs(data[item.field]).format(item.dateFormat) : ''
                }}
              </slot>
              <slot v-else-if="item.dictType">
                <DictTag :type="item.dictType" :value="data[item.field] + ''" />
              </slot>
              <slot v-else :name="item.field" :row="data">{{ data[item.field] }}</slot>
            </template>
          </ElDescriptionsItem>
        </ElDescriptions>
      </div>
    </ElCollapseTransition>
  </div>
</template>

<style lang="scss" scoped>
$prefix-cls: #{$namespace}-descriptions;

.#{$prefix-cls}-header {
  &__title {
    &::after {
      position: absolute;
      top: 3px;
      left: -10px;
      width: 4px;
      height: 70%;
      background: var(--el-color-primary);
      content: '';
    }
  }
}

.#{$prefix-cls}-content {
  :deep(.#{$elNamespace}-descriptions__cell) {
    width: 0;
  }
}
</style>
