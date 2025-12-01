// https://github.com/vuejs/pinia/issues/2098
declare module 'pinia' {
  export function acceptHMRUpdate(
    initialUseStore: any | StoreDefinition,
    hot: any,
  ): (newModule: any) => any;
}

export { acceptHMRUpdate };
