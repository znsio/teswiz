import { unsupportedScreenAction } from "../screen-route.ts";

export function echoMessage(): object {
  return unsupportedScreenAction(
    "TheApp Echo screen is not supported on web for WEB_ENGINE=playwright-ts.",
  );
}
