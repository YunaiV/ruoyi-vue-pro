export function debounce(fn, delay = 500) {
  let timer;
  return function(...args) {
    if (timer) {
      clearTimeout(timer);
      timer = null;
    }
    timer = setTimeout(fn.bind(this, ...args), delay);
  };
}
