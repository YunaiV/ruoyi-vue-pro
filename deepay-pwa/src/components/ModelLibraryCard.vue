<template>
  <div class="model-card dp-card">
    <div class="card-image" :style="{ background: gradient }">
      <div class="tag-badge" v-if="tag">{{ tag }}</div>
      <button class="fav-btn" :class="{ active: localFav }" @click.stop="toggleFav" :aria-label="localFav ? '取消收藏' : '收藏'">
        <svg width="18" height="18" viewBox="0 0 24 24" :fill="localFav ? '#1abc9c' : 'none'" stroke="currentColor" stroke-width="2">
          <path d="M20.84 4.61a5.5 5.5 0 00-7.78 0L12 5.67l-1.06-1.06a5.5 5.5 0 00-7.78 7.78l1.06 1.06L12 21.23l7.78-7.78 1.06-1.06a5.5 5.5 0 000-7.78z"/>
        </svg>
      </button>
      <div class="image-placeholder">
        <span>{{ emoji }}</span>
      </div>
    </div>
    <div class="card-info">
      <span class="model-name">{{ name }}</span>
    </div>
  </div>
</template>

<script setup>
import { ref, computed } from 'vue'

const props = defineProps({
  name:      { type: String, default: '模特' },
  tag:       { type: String, default: '' },
  imageUrl:  { type: String, default: '' },
  isFavorite:{ type: Boolean, default: false },
  index:     { type: Number, default: 0 },
})

const emit = defineEmits(['toggle-favorite'])
const localFav = ref(props.isFavorite)

const gradients = [
  'linear-gradient(160deg,#1a1a2e,#16213e)',
  'linear-gradient(160deg,#0f3460,#533483)',
  'linear-gradient(160deg,#1b262c,#0f4c75)',
  'linear-gradient(160deg,#2d1b69,#11998e)',
  'linear-gradient(160deg,#1a1a1a,#2d6a4f)',
  'linear-gradient(160deg,#2c1810,#1abc9c44)',
]
const emojis = ['👗','👘','🥻','🧣','🧥','👔']
const gradient = computed(() => gradients[props.index % gradients.length])
const emoji = computed(() => emojis[props.index % emojis.length])

function toggleFav() {
  localFav.value = !localFav.value
  emit('toggle-favorite', localFav.value)
}
</script>

<style scoped>
.model-card {
  cursor: pointer;
  overflow: hidden;
  padding: 0;
}
.model-card:hover .card-image { transform: scale(1.02); }
.card-image {
  aspect-ratio: 3/4;
  border-radius: 14px 14px 0 0;
  position: relative;
  overflow: hidden;
  display: flex; align-items: center; justify-content: center;
  transition: transform 0.3s ease;
}
.image-placeholder {
  font-size: 56px;
  opacity: 0.6;
}
.tag-badge {
  position: absolute; top: 10px; left: 10px;
  background: rgba(26,188,156,0.85);
  color: white; font-size: 11px; font-weight: 700;
  padding: 3px 10px; border-radius: 999px;
  backdrop-filter: blur(4px);
}
.fav-btn {
  position: absolute; top: 8px; right: 8px;
  width: 32px; height: 32px;
  background: rgba(0,0,0,0.45);
  border: none; border-radius: 50%;
  display: flex; align-items: center; justify-content: center;
  cursor: pointer; color: #fff;
  backdrop-filter: blur(4px);
  transition: all 0.15s;
}
.fav-btn:hover { background: rgba(26,188,156,0.6); transform: scale(1.1); }
.fav-btn.active { background: rgba(26,188,156,0.25); color: #1abc9c; }
.card-info {
  padding: 10px 12px 12px;
}
.model-name {
  font-size: 13px; font-weight: 600;
  color: var(--dp-text);
  display: block;
}
</style>
