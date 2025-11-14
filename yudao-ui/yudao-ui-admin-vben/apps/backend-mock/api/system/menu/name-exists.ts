import { eventHandler, getQuery } from 'h3';
import { verifyAccessToken } from '~/utils/jwt-utils';
import { MOCK_MENU_LIST } from '~/utils/mock-data';
import { unAuthorizedResponse, useResponseSuccess } from '~/utils/response';

const namesMap: Record<string, any> = {};

function getNames(menus: any[]) {
  menus.forEach((menu) => {
    namesMap[menu.name] = String(menu.id);
    if (menu.children) {
      getNames(menu.children);
    }
  });
}
getNames(MOCK_MENU_LIST);

export default eventHandler(async (event) => {
  const userinfo = verifyAccessToken(event);
  if (!userinfo) {
    return unAuthorizedResponse(event);
  }
  const { id, name } = getQuery(event);

  return (name as string) in namesMap &&
    (!id || namesMap[name as string] !== String(id))
    ? useResponseSuccess(true)
    : useResponseSuccess(false);
});
