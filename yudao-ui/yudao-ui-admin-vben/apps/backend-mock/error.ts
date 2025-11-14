import type { NitroErrorHandler } from 'nitropack';

const errorHandler: NitroErrorHandler = function (error, event) {
  event.node.res.end(`[Error Handler] ${error.stack}`);
};

export default errorHandler;
