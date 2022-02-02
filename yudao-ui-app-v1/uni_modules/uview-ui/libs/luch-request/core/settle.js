/**
 * Resolve or reject a Promise based on response status.
 *
 * @param {Function} resolve A function that resolves the promise.
 * @param {Function} reject A function that rejects the promise.
 * @param {object} response The response.
 */
export default function settle(resolve, reject, response) {
    const { validateStatus } = response.config
    const status = response.statusCode
    if (status && (!validateStatus || validateStatus(status))) {
        resolve(response)
    } else {
        reject(response)
    }
}
