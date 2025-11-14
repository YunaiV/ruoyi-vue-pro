/**
 * @param { Readonly<Promise> } promise
 * @param {object=} errorExt - Additional Information you can pass to the err object
 * @return { Promise }
 */
export async function to<T, U = Error>(
  promise: Readonly<Promise<T>>,
  errorExt?: object,
): Promise<[null, T] | [U, undefined]> {
  try {
    const data = await promise;
    const result: [null, T] = [null, data];
    return result;
  } catch (error) {
    if (errorExt) {
      const parsedError = Object.assign({}, error, errorExt);
      return [parsedError as U, undefined];
    }
    return [error as U, undefined];
  }
}
