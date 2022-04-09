<template>
  <view class="u-text-price-wrap">
    <uvText
        class="u-text-price-item"
        v-for="(item,index) in textArray"
        :key="index"
        :text="item"
        :size="(index === 1 && integerSize) ? integerSize : size"
        :color="color"
    >
    </uvText>
  </view>
</template>

<script>
/**
 * 此组件存在只为简单的显示特定样式的价格数字
 */
import uvText from "../u-text/u-text.vue";
import props from "../u-text/props.js";
export default {
  name: "u--text-price",
  mixins: [uni.$u.mpMixin, props, uni.$u.mixin],
  components: {
    uvText,
  },
  props: {
    //整形部分字体大小可单独定义
    integerSize: {
      type: [String, Number],
      default: uni.$u.props.text.size
    }
  },
  computed: {
    textArray() {
      let array = ['￥'];
      if (!/^\d+(\.\d+)?$/.test(this.text)) {
        uni.$u.error('组件<u--text-price :text="???" 此处参数应为金额数字');
      } else {
        let arr = parseFloat(this.text).toFixed(2).split('.');
        array.push(arr[0]);
        array.push('.' + arr[1]);
      }
      return array;
    }
  }
};
</script>
<style>
.u-text-price-wrap {
  display: flex;
  flex-direction: row;
  justify-content: left;
}

.u-text-price-item{
  flex: 0;
}
</style>