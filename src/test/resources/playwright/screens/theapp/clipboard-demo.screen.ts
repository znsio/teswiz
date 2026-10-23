import { unsupportedScreenAction } from "../screen-route.ts";

const MESSAGE = "TheApp Clipboard Demo screen is not supported on web for WEB_ENGINE=playwright-ts.";

export function setInClipboard(): object {
  return unsupportedScreenAction(MESSAGE);
}

export function doesAddedContentExistInClipboard(): object {
  return unsupportedScreenAction(MESSAGE);
}
