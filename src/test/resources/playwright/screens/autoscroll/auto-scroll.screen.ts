import { unsupportedScreenAction } from "../screen-route.ts";

export function goToDropdownWindow(): object {
  return unsupportedScreenAction(
    "AutoScroll is not supported on web for WEB_ENGINE=playwright-ts.",
  );
}

export function scrollInDynamicLayer(): object {
  return unsupportedScreenAction(
    "AutoScroll is not supported on web for WEB_ENGINE=playwright-ts.",
  );
}

export function isScrollSuccessful(): object {
  return unsupportedScreenAction(
    "AutoScroll is not supported on web for WEB_ENGINE=playwright-ts.",
  );
}
