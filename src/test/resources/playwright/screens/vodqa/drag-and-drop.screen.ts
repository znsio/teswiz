import { unsupportedScreenAction } from "../screen-route.ts";

const MESSAGE = "Vodqa drag and drop is not supported on web for WEB_ENGINE=playwright-ts.";

export function isMessageVisible(): object {
  return unsupportedScreenAction(MESSAGE);
}

export function dragAndDropCircleObject(): object {
  return unsupportedScreenAction(MESSAGE);
}
