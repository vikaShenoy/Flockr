/**
 * An object containing the rules that can be used in forms.
 * @type {{POJO}}
 */
export const rules = {
  required: field => !!field || "Field is required",
  requiredArray: field => field.length > 0 || "Field is required",
  noNumbers: field => !/\d/.test(field) || "No Numbers Allowed",
  onlyNumbers: field => /\d/.test(field) || "Only Numbers Allowed",
  absoluteRange: (limit, name) => (field => Math.abs(parseFloat(field)) <= limit || `Invalid ${name} please enter a number between ${-limit} and ${limit}`),
  twoOrMoreCharacters: field => field.length < 2 || "2 or more characters are required in this field",
  validEmailRule: field => /^[a-zA-Z0-9.!#$%&'*+/=?^_`{|}~-]+@[a-zA-Z0-9](?:[a-zA-Z0-9-]{0,61}[a-zA-Z0-9])?(?:\.[a-zA-Z0-9](?:[a-zA-Z0-9-]{0,61}[a-zA-Z0-9])?)*$/.test(field) || "This is not a valid email"
};