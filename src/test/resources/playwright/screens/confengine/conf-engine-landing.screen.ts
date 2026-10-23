import { unsupportedScreenAction } from "../screen-route.ts";

export function getListOfConferences(): object {
  return unsupportedScreenAction(
    "ConfEngine list of conferences is not supported on web for WEB_ENGINE=playwright-ts.",
  );
}
