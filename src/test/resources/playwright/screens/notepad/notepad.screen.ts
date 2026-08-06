import { unsupportedScreenAction } from "../screen-route.ts";

export function typeMessage(): object {
  return unsupportedScreenAction(
    "Notepad is not supported on web for WEB_ENGINE=playwright-ts.",
  );
}
