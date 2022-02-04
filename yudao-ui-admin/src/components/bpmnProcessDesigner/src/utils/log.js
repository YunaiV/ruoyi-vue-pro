class Log {
  static type = ["primary", "success", "warn", "error", "info"];

  static typeColor(type = "default") {
    let color = "";
    switch (type) {
      case "primary":
        color = "#2d8cf0";
        break;
      case "success":
        color = "#19be6b";
        break;
      case "info":
        color = "#909399";
        break;
      case "warn":
        color = "#ff9900";
        break;
      case "error":
        color = "#f03f14";
        break;
      case "default":
        color = "#35495E";
        break;
      default:
        color = type;
        break;
    }
    return color;
  }

  static print(text, type = "default", back = false) {
    if (typeof text === "object") {
      // 如果是對象則調用打印對象方式
      console.dir(text);
      return;
    }
    if (back) {
      // 如果是打印帶背景圖的
      console.log(`%c ${text} `, `background:${this.typeColor(type)}; padding: 2px; border-radius: 4px;color: #fff;`);
    } else {
      console.log(`%c ${text} `, `color: ${this.typeColor(type)};`);
    }
  }

  static pretty(title, text, type = "primary") {
    if (typeof text === "object") {
      console.log(
        `%c ${title} %c`,
        `background:${this.typeColor(type)};border:1px solid ${this.typeColor(type)}; padding: 1px; border-radius: 4px 0 0 4px; color: #fff;`
      );
      console.dir(text);
      return;
    }
    console.log(
      `%c ${title} %c ${text} %c`,
      `background:${this.typeColor(type)};border:1px solid ${this.typeColor(type)}; padding: 1px; border-radius: 4px 0 0 4px; color: #fff;`,
      `border:1px solid ${this.typeColor(type)}; padding: 1px; border-radius: 0 4px 4px 0; color: ${this.typeColor(type)};`,
      "background:transparent"
    );
  }
}
export default Log;
