import { unsupportedScreenAction } from "../screen-route.ts";

export function isUserOnNativeViewScreen(): object {
  return unsupportedScreenAction(
    "Vodqa native view is not supported on web for WEB_ENGINE=playwright-ts.",
  );
}
