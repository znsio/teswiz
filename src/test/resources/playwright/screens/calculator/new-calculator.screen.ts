import { unsupportedScreenAction } from "../screen-route.ts";

export function selectNumber(): object {
  return unsupportedScreenAction(
    "NewCalculator is not supported on web for WEB_ENGINE=playwright-ts.",
  );
}

export function pressOperation(): object {
  return unsupportedScreenAction(
    "NewCalculator is not supported on web for WEB_ENGINE=playwright-ts.",
  );
}

export function launch(): object {
  return unsupportedScreenAction(
    "NewCalculator is not supported on web for WEB_ENGINE=playwright-ts.",
  );
}
