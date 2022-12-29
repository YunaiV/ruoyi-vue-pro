<template>
  <view>
    <text v-for="(item, index) in textArray" :key="index" :style="{ fontSize: (index === 1 ? integerSize : size) + 'px', color, textDecoration }">
      {{ item }}
    </text>
  </view>
</template>

<script>
/**
 * 此组件简单的显示（驼峰式）的价格
 */
export default {
  name: 'yd-text-price',
  components: {},
  props: {
    //货币符号
    symbol: {
      type: String,
      default: '￥'
    },
    price: {
      type: [String, Number],
      default: ''
    },
    color: {
      type: String,
      default: '#333333'
    },
    //字体大小
    size: {
      type: [String, Number],
      default: 15
    },
    //整形部分字体大小可单独定义
    intSize: {
      type: [String, Number],
      default: ''
    },
    //文字装饰，下划线，中划线等
    decoration: {
      type: String,
      default: 'none'
    }
  },
  data() {
    return {
      textDecoration: this.decoration
    }
  },
  computed: {
    textArray() {
      let array = []
      if (this.price === '' || this.price === undefined) {
        return []
      }
      array.push(this.symbol)
      if (!/^\d+(\.\d+)?$/.test(this.price)) {
        console.error('组件<yd-text-price :text="???" 此处参数应为金额数字')
      } else {
        let arr = parseFloat(this.price).toFixed(2).split('.')
        array.push(arr[0])
        array.push('.' + arr[1])
      }
      return array
    },
    integerSize() {
      return this.intSize ? this.intSize : this.size
    }
  }
}
</script>
<style scoped></style>
