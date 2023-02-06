const isArray = function (obj: any): boolean {
  return Object.prototype.toString.call(obj) === '[object Array]'
}

const Logger = () => {}

Logger.typeColor = function (type: string) {
  let color = ''
  switch (type) {
    case 'primary':
      color = '#2d8cf0'
      break
    case 'success':
      color = '#19be6b'
      break
    case 'info':
      color = '#909399'
      break
    case 'warn':
      color = '#ff9900'
      break
    case 'error':
      color = '#f03f14'
      break
    default:
      color = '#35495E'
      break
  }
  return color
}

Logger.print = function (type = 'default', text: any, back = false) {
  if (typeof text === 'object') {
    // 如果是對象則調用打印對象方式
    isArray(text) ? console.table(text) : console.dir(text)
    return
  }
  if (back) {
    // 如果是打印帶背景圖的
    console.log(
      `%c ${text} `,
      `background:${Logger.typeColor(type)}; padding: 2px; border-radius: 4px; color: #fff;`
    )
  } else {
    console.log(
      `%c ${text} `,
      `border: 1px solid ${Logger.typeColor(type)};
        padding: 2px; border-radius: 4px;
        color: ${Logger.typeColor(type)};`
    )
  }
}

Logger.printBack = function (type = 'primary', text) {
  this.print(type, text, true)
}

Logger.pretty = function (type = 'primary', title, text) {
  if (typeof text === 'object') {
    console.group('Console Group', title)
    console.log(
      `%c ${title}`,
      `background:${Logger.typeColor(type)};border:1px solid ${Logger.typeColor(type)};
        padding: 1px; border-radius: 4px; color: #fff;`
    )
    isArray(text) ? console.table(text) : console.dir(text)
    console.groupEnd()
    return
  }
  console.log(
    `%c ${title} %c ${text} %c`,
    `background:${Logger.typeColor(type)};border:1px solid ${Logger.typeColor(type)};
      padding: 1px; border-radius: 4px 0 0 4px; color: #fff;`,
    `border:1px solid ${Logger.typeColor(type)};
      padding: 1px; border-radius: 0 4px 4px 0; color: ${Logger.typeColor(type)};`,
    'background:transparent'
  )
}

Logger.prettyPrimary = function (title, ...text) {
  text.forEach((t) => this.pretty('primary', title, t))
}

Logger.prettySuccess = function (title, ...text) {
  text.forEach((t) => this.pretty('success', title, t))
}

Logger.prettyWarn = function (title, ...text) {
  text.forEach((t) => this.pretty('warn', title, t))
}

Logger.prettyError = function (title, ...text) {
  text.forEach((t) => this.pretty('error', title, t))
}

Logger.prettyInfo = function (title, ...text) {
  text.forEach((t) => this.pretty('info', title, t))
}

export default Logger
