<script lang="tsx">
import { defineComponent, PropType, computed, h, unref } from 'vue'
import { propTypes } from '@/utils/propTypes'

export default defineComponent({
  name: 'Highlight',
  props: {
    tag: propTypes.string.def('span'),
    keys: {
      type: Array as PropType<string[]>,
      default: () => []
    },
    color: propTypes.string.def('var(--el-color-primary)')
  },
  emits: ['click'],
  setup(props, { emit, slots }) {
    const keyNodes = computed(() => {
      return props.keys.map((key) => {
        return h(
          'span',
          {
            onClick: () => {
              emit('click', key)
            },
            style: {
              color: props.color,
              cursor: 'pointer'
            }
          },
          key
        )
      })
    })

    const parseText = (text: string) => {
      props.keys.forEach((key, index) => {
        const regexp = new RegExp(key, 'g')
        text = text.replace(regexp, `{{${index}}}`)
      })
      return text.split(/{{|}}/)
    }

    const renderText = () => {
      if (!slots?.default) return null
      const node = slots?.default()[0].children

      if (!node) {
        return slots?.default()[0]
      }

      const textArray = parseText(node as string)
      const regexp = /^[0-9]*$/
      const nodes = textArray.map((t) => {
        if (regexp.test(t)) {
          return unref(keyNodes)[t] || t
        }
        return t
      })
      return h(props.tag, nodes)
    }

    return () => renderText()
  }
})
</script>
