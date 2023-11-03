/**
 * v-dialogDragWidth 可拖动弹窗宽度（右侧边）
 * Copyright (c) 2019 ruoyi
 */

export default {
  bind(el) {
    const dragDom = el.querySelector('.el-dialog');
    const lineEl = document.createElement('div');
    lineEl.style = 'width: 5px; background: inherit; height: 80%; position: absolute; right: 0; top: 0; bottom: 0; margin: auto; z-index: 1; cursor: w-resize;';
    lineEl.addEventListener('mousedown',
      function (e) {
        // 鼠标按下，计算当前元素距离可视区的距离
        const disX = e.clientX - el.offsetLeft;
        // 当前宽度
        const curWidth = dragDom.offsetWidth;
        document.onmousemove = function (e) {
          e.preventDefault(); // 移动时禁用默认事件
          // 通过事件委托，计算移动的距离
          const l = e.clientX - disX;
          dragDom.style.width = `${curWidth + l}px`;
        };
        document.onmouseup = function (e) {
          document.onmousemove = null;
          document.onmouseup = null;
        };
      }, false);
    dragDom.appendChild(lineEl);
  }
}
