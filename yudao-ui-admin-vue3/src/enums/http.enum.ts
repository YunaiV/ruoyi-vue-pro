/**
 * @description: Request result set
 */
export enum ResultEnum {
  SUCCESS = 0,
  ERROR = 500,
  TIMEOUT = 401,
  TYPE = 'success'
}

/**
 * @description: request method
 */
export enum RequestEnum {
  GET = 'GET',
  POST = 'POST',
  PUT = 'PUT',
  DELETE = 'DELETE'
}

/**
 * @description:  contentType
 */
export enum ContentTypeEnum {
  // json
  JSON = 'application/json;charset=UTF-8',
  // form-data qs
  FORM_URLENCODED = 'application/x-www-form-urlencoded;charset=UTF-8',
  // form-data  upload
  FORM_DATA = 'multipart/form-data;charset=UTF-8'
}

/**
 * Exception related enumeration
 */
export enum ExceptionEnum {
  // page not access
  PAGE_NOT_ACCESS = 403,

  // page not found
  PAGE_NOT_FOUND = 404,

  // error
  ERROR = 500,

  // net work error
  NET_WORK_ERROR = 10000,

  // No data on the page. In fact, it is not an exception page
  PAGE_NOT_DATA = 10100
}
