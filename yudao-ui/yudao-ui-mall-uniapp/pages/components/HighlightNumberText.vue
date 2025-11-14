<template>
  <view class="content-container">
    <span v-for="(part, index) in formattedContent" :key="index"
          @click="handleClick(part)"
          :class="{'highlight-number': part.isNumber, 'phone-number': part.isPhone}">
      {{ part.text }}
    </span>
  </view>
</template>

<script>
  export default {
    name: 'HighlightNumber',
    props: {
      content: {
        type: String,
        required: true
      }
    },
    computed: {
      formattedContent() {
        const phoneRegex = /(1[3-9]\d{9})/g;
        const numberRegex = /(\d+)/g;
        let text = this.content;
        let result = [];
        let match;

        // Step 1: 提取手机号
        while ((match = phoneRegex.exec(text)) !== null) {
          if (match.index > 0) {
            const before = text.slice(0, match.index);
            result.push(...this.splitAndPush(before, false, false));
          }
          result.push({ text: match[0], isNumber: true, isPhone: true });
          text = text.slice(match.index + match[0].length);
        }

        // Step 2: 提取普通数字
        while ((match = numberRegex.exec(text)) !== null) {
          if (match.index > 0) {
            const before = text.slice(0, match.index);
            result.push(...this.splitAndPush(before, false, false));
          }
          result.push({ text: match[0], isNumber: true });
          text = text.slice(match.index + match[0].length);
        }

        // Step 3: 添加剩余文本
        if (text.length > 0) {
          result.push(...this.splitAndPush(text, false, false));
        }

        return result;
      }
    },
    methods: {
      splitAndPush(str, isNumber = false, isPhone = false) {
        return str.split('').map(char => ({ text: char, isNumber, isPhone }));
      },
      handleClick(part) {
        if (part.isPhone) {
          this.$emit('phone-click', { phoneNumber: part.text });
        } else if (part.isNumber) {
          this.$emit('number-click', { number: part.text });
        }
      }
    }
  };
</script>

<style scoped>
  .highlight-number {
    color: #ff5722;
    font-weight: bold;
  }

  .phone-number {
    color: #007AFF;
    text-decoration: underline;
  }
</style>
