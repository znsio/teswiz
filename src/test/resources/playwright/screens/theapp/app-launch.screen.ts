import type { ScreenContext } from "../screen-context.ts";
import { stayOnCurrentScreen, unsupportedScreenAction } from "../screen-route.ts";

const LOCATORS = {
  loginLink: "Form Authentication",
} as const;

export async function selectLogin(screen: ScreenContext): Promise<void> {
  await screen.page.getByRole("link", { name: LOCATORS.loginLink }).click();
}

export function goBack() {
  return stayOnCurrentScreen();
}

export function selectEcho() {
  return unsupportedScreenAction(
    "TheApp Echo screen is not supported on web for WEB_ENGINE=playwright-ts.",
  );
}

export function goToClipboardDemo() {
  return unsupportedScreenAction(
    "TheApp Clipboard Demo screen is not supported on web for WEB_ENGINE=playwright-ts.",
  );
}
