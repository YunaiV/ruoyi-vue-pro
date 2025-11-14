/**
 * 加载js文件
 * @param src js文件地址
 */
function loadScript(src: string) {
  return new Promise<void>((resolve, reject) => {
    if (document.querySelector(`script[src="${src}"]`)) {
      // 如果已经加载过，直接 resolve
      return resolve();
    }
    const script = document.createElement('script');
    script.src = src;
    script.addEventListener('load', () => resolve());
    script.addEventListener('error', () =>
      reject(new Error(`Failed to load script: ${src}`)),
    );
    document.head.append(script);
  });
}

export { loadScript };
