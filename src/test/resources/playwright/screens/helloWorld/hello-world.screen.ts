import { unsupportedScreenAction } from "../screen-route.ts";

export function generateRandomNumber(): object {
  return unsupportedScreenAction(
    "HelloWorld is not supported on web for WEB_ENGINE=playwright-ts.",
  );
}
