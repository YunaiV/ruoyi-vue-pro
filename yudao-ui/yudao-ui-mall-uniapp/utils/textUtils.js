// sheep/utils/textUtils.js
export function measureTextWidth(text, fontSize = 14, fontFamily = 'sans-serif') {
  // 钉钉小程序没有 uni.createCanvasContext 方法
  if (typeof uni === 'undefined' || typeof uni.createCanvasContext !== 'function') {
    return estimateTextWidth(text, fontSize);
  }

  try {
    const ctx = uni.createCanvasContext('tempCanvasForText');
    ctx.setFontSize(fontSize);
    ctx.font = `${fontSize}px ${fontFamily}`;
    const metrics = ctx.measureText(text);
    return metrics.width;
  } catch (e) {
    // 某些平台可能不支持 measureText，降级使用估算
    return estimateTextWidth(text, fontSize);
  }
}

// 简单估算中文和英文字符宽度
function estimateTextWidth(text, fontSize = 14) {
  let width = 0;
  for (let i = 0; i < text.length; i++) {
    const charCode = text.charCodeAt(i);
    if (charCode >= 0x4e00 && charCode <= 0x9fff) {
      // 中文字符
      width += fontSize;
    } else {
      // 英文字符
      width += fontSize * 0.5;
    }
  }
  return width;
}
