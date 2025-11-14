export function resetSize(vm: any) {
  const EmployeeWindow = window as any;
  const parentWidth =
    vm.$el.parentNode.offsetWidth || EmployeeWindow.offsetWidth;
  const parentHeight =
    vm.$el.parentNode.offsetHeight || EmployeeWindow.offsetHeight;
  const img_width = vm.imgSize.width.includes('%')
    ? `${(Number.parseInt(vm.imgSize.width) / 100) * parentWidth}px`
    : vm.imgSize.width;

  const img_height = vm.imgSize.height.includes('%')
    ? `${(Number.parseInt(vm.imgSize.height) / 100) * parentHeight}px`
    : vm.imgSize.height;

  const bar_width = vm.barSize.width.includes('%')
    ? `${(Number.parseInt(vm.barSize.width) / 100) * parentWidth}px`
    : vm.barSize.width;

  const bar_height = vm.barSize.height.includes('%')
    ? `${(Number.parseInt(vm.barSize.height) / 100) * parentHeight}px`
    : vm.barSize.height;

  return {
    barHeight: bar_height,
    barWidth: bar_width,
    imgHeight: img_height,
    imgWidth: img_width,
  };
}

export const _code_chars = [
  1,
  2,
  3,
  4,
  5,
  6,
  7,
  8,
  9,
  'a',
  'b',
  'c',
  'd',
  'e',
  'f',
  'g',
  'h',
  'i',
  'j',
  'k',
  'l',
  'm',
  'n',
  'o',
  'p',
  'q',
  'r',
  's',
  't',
  'u',
  'v',
  'w',
  'x',
  'y',
  'z',
  'A',
  'B',
  'C',
  'D',
  'E',
  'F',
  'G',
  'H',
  'I',
  'J',
  'K',
  'L',
  'M',
  'N',
  'O',
  'P',
  'Q',
  'R',
  'S',
  'T',
  'U',
  'V',
  'W',
  'X',
  'Y',
  'Z',
];
export const _code_color1 = ['#fffff0', '#f0ffff', '#f0fff0', '#fff0f0'];
export const _code_color2 = [
  '#FF0033',
  '#006699',
  '#993366',
  '#FF9900',
  '#66CC66',
  '#FF33CC',
];
