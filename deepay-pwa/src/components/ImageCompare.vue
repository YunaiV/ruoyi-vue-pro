<template>
  <div
    class="ic-wrap"
    ref="wrapEl"
    @mousedown.prevent="startDrag"
    @touchstart.prevent="startDrag"
  >
    <!-- Before image (full width, clipped right by slider) -->
    <div class="ic-before" :style="{ clipPath: `inset(0 ${100 - pos}% 0 0)` }">
      <img :src="before" :alt="beforeLabel" class="ic-img" draggable="false" />
      <span class="ic-label ic-label-before">{{ beforeLabel }}</span>
    </div>

    <!-- After image (full width underneath) -->
    <div class="ic-after">
      <img :src="after" :alt="afterLabel" class="ic-img" draggable="false" />
      <span class="ic-label ic-label-after">{{ afterLabel }}</span>
    </div>

    <!-- Divider line + handle -->
    <div class="ic-divider" :style="{ left: pos + '%' }">
      <div class="ic-handle">
        <svg width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2.5">
          <path d="M8 5L3 12l5 7M16 5l5 7-5 7"/>
        </svg>
      </div>
    </div>

    <!-- Drag hint -->
    <div v-if="showHint" class="ic-hint">← 拖动对比 →</div>
  </div>
</template>

<script setup>
import { ref, onMounted, onBeforeUnmount } from 'vue'

const props = defineProps({
  before:      { type: String, required: true },
  after:       { type: String, required: true },
  beforeLabel: { type: String, default: '原图' },
  afterLabel:  { type: String, default: '生成后' },
  initialPos:  { type: Number, default: 50 },
})

const wrapEl   = ref(null)
const pos      = ref(props.initialPos)
const dragging = ref(false)
const showHint = ref(true)

function startDrag(e) {
  dragging.value = true
  showHint.value = false
  updatePos(e)
  window.addEventListener('mousemove', onMove)
  window.addEventListener('mouseup',   stopDrag)
  window.addEventListener('touchmove', onMove, { passive: false })
  window.addEventListener('touchend',  stopDrag)
}

function onMove(e) {
  if (!dragging.value) return
  e.preventDefault()
  updatePos(e)
}

function stopDrag() {
  dragging.value = false
  window.removeEventListener('mousemove', onMove)
  window.removeEventListener('mouseup',   stopDrag)
  window.removeEventListener('touchmove', onMove)
  window.removeEventListener('touchend',  stopDrag)
}

function updatePos(e) {
  if (!wrapEl.value) return
  const rect   = wrapEl.value.getBoundingClientRect()
  const clientX = e.touches ? e.touches[0].clientX : e.clientX
  const raw    = ((clientX - rect.left) / rect.width) * 100
  pos.value    = Math.min(98, Math.max(2, raw))
}

onBeforeUnmount(stopDrag)
</script>

<style scoped>
.ic-wrap {
  position: relative;
  width: 100%;
  aspect-ratio: 1 / 1;
  overflow: hidden;
  border-radius: 12px;
  cursor: ew-resize;
  user-select: none;
  background: #000;
  touch-action: none;
}

/* Images */
.ic-before,
.ic-after {
  position: absolute;
  inset: 0;
  width: 100%;
  height: 100%;
}

.ic-after  { z-index: 1; }
.ic-before { z-index: 2; }

.ic-img {
  width: 100%;
  height: 100%;
  object-fit: cover;
  display: block;
  pointer-events: none;
}

/* Labels */
.ic-label {
  position: absolute;
  bottom: 12px;
  padding: 4px 10px;
  background: rgba(0, 0, 0, 0.55);
  backdrop-filter: blur(6px);
  border-radius: 8px;
  font-size: 12px;
  font-weight: 700;
  color: #fff;
  z-index: 4;
  pointer-events: none;
}
.ic-label-before { left: 12px; }
.ic-label-after  { right: 12px; }

/* Divider */
.ic-divider {
  position: absolute;
  top: 0;
  bottom: 0;
  width: 2px;
  background: rgba(255, 255, 255, 0.9);
  z-index: 5;
  transform: translateX(-50%);
  pointer-events: none;
  box-shadow: 0 0 8px rgba(0,0,0,0.5);
}

/* Handle circle */
.ic-handle {
  position: absolute;
  top: 50%;
  left: 50%;
  transform: translate(-50%, -50%);
  width: 44px;
  height: 44px;
  background: #fff;
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  box-shadow: 0 2px 12px rgba(0,0,0,0.4);
  color: #1a1a1a;
  pointer-events: none;
}

/* Drag hint */
.ic-hint {
  position: absolute;
  top: 50%;
  left: 50%;
  transform: translate(-50%, -50%);
  background: rgba(0,0,0,0.55);
  backdrop-filter: blur(8px);
  color: #fff;
  font-size: 13px;
  font-weight: 600;
  padding: 8px 16px;
  border-radius: 20px;
  pointer-events: none;
  z-index: 6;
  animation: fadeHint 3s ease-in-out forwards;
}

@keyframes fadeHint {
  0%   { opacity: 1; }
  60%  { opacity: 1; }
  100% { opacity: 0; }
}
</style>
