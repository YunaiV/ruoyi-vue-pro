<template>
    <uvText
        :type="type"
        :show="show"
        :text="text"
        :prefixIcon="prefixIcon"
        :suffixIcon="suffixIcon"
        :mode="mode"
        :href="href"
        :format="format"
        :call="call"
        :openType="openType"
        :bold="bold"
        :block="block"
        :lines="lines"
        :color="color"
		:decoration="decoration"
        :size="size"
        :iconStyle="iconStyle"
        :margin="margin"
        :lineHeight="lineHeight"
        :align="align"
        :wordWrap="wordWrap"
        :customStyle="customStyle"
        @click="$emit('click')"
    ></uvText>
</template>

<script>
/**
 * 此组件存在的理由是，在nvue下，u-text被uni-app官方占用了，u-text在nvue中相当于input组件
 * 所以在nvue下，取名为u--input，内部其实还是u-text.vue，只不过做一层中转
 * 不使用v-bind="$attrs"，而是分开独立写传参，是因为微信小程序不支持此写法
 */
import uvText from "../u-text/u-text.vue";
import props from "../u-text/props.js";
export default {
    name: "u--text",
    mixins: [uni.$u.mpMixin, props, uni.$u.mixin],
    components: {
        uvText,
    },
};
</script>
