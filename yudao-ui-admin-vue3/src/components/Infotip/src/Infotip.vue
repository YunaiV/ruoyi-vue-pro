<script setup lang="ts">
import { PropType, defineEmits } from 'vue'
import { Highlight } from '@/components/Highlight'
import { useDesign } from '@/hooks/web/useDesign'
import { propTypes } from '@/utils/propTypes'
import { TipSchema } from '@/types/infoTip'

const { getPrefixCls } = useDesign()

const prefixCls = getPrefixCls('infotip')

defineProps({
  title: propTypes.string.def(''),
  schema: {
    type: Array as PropType<Array<string | TipSchema>>,
    required: true,
    default: () => []
  },
  showIndex: propTypes.bool.def(true),
  highlightColor: propTypes.string.def('var(--el-color-primary)')
})

const emit = defineEmits(['click'])

const keyClick = (key: string) => {
  emit('click', key)
}
</script>

<template>
  <div
    :class="[
      prefixCls,
      'p-20px mb-20px border-1px border-solid border-[var(--el-color-primary)] bg-[var(--el-color-primary-light-9)]'
    ]"
  >
    <div v-if="title" :class="[`${prefixCls}__header`, 'flex items-center']">
      <Icon icon="ep:warning-filled" :size="22" color="var(--el-color-primary)" />
      <span :class="[`${prefixCls}__title`, 'pl-5px text-16px font-bold']">{{ title }}</span>
    </div>
    <div :class="`${prefixCls}__content`">
      <p v-for="(item, $index) in schema" :key="$index" class="text-14px mt-15px">
        <Highlight
          :keys="typeof item === 'string' ? [] : item.keys"
          :color="highlightColor"
          @click="keyClick"
        >
          {{ showIndex ? `${$index + 1}、` : '' }}{{ typeof item === 'string' ? item : item.label }}
        </Highlight>
      </p>
    </div>
  </div>
</template>
