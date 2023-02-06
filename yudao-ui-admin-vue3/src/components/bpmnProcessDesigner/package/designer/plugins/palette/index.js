// import PaletteModule from "diagram-js/lib/features/palette";
// import CreateModule from "diagram-js/lib/features/create";
// import SpaceToolModule from "diagram-js/lib/features/space-tool";
// import LassoToolModule from "diagram-js/lib/features/lasso-tool";
// import HandToolModule from "diagram-js/lib/features/hand-tool";
// import GlobalConnectModule from "diagram-js/lib/features/global-connect";
// import translate from "diagram-js/lib/i18n/translate";
//
// import PaletteProvider from "./paletteProvider";
//
// export default {
//   __depends__: [PaletteModule, CreateModule, SpaceToolModule, LassoToolModule, HandToolModule, GlobalConnectModule, translate],
//   __init__: ["paletteProvider"],
//   paletteProvider: ["type", PaletteProvider]
// };

import CustomPalette from './CustomPalette'

export default {
  __init__: ['paletteProvider'],
  paletteProvider: ['type', CustomPalette]
}
