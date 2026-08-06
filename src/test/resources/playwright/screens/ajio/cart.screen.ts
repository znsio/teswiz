import { unsupportedScreenAction } from "../screen-route.ts";

export function getActualProductName(): object {
  return unsupportedScreenAction(
    "Cart is not supported on web for WEB_ENGINE=playwright-ts.",
  );
}
