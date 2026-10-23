import { unsupportedScreenAction } from "../screen-route.ts";

const MESSAGE = "Vodqa web view is not supported on web for WEB_ENGINE=playwright-ts.";

export function isUserOnNewsWebViewScreen(): object {
  return unsupportedScreenAction(MESSAGE);
}

export function isLoginOptionVisible(): object {
  return unsupportedScreenAction(MESSAGE);
}

export function navigateToSamplesList(): object {
  return unsupportedScreenAction(MESSAGE);
}
