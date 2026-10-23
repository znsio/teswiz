import { unsupportedScreenAction } from "../screen-route.ts";

export function handlePopupIfPresent(): object {
  return unsupportedScreenAction(
    "Calculator is not supported on web for WEB_ENGINE=playwright-ts.",
  );
}

export function selectNumber(): object {
  return unsupportedScreenAction(
    "Calculator is not supported on web for WEB_ENGINE=playwright-ts.",
  );
}

export function pressOperation(): object {
  return unsupportedScreenAction(
    "Calculator is not supported on web for WEB_ENGINE=playwright-ts.",
  );
}
