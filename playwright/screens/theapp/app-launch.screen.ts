import type { ScreenContext } from "../screen-context.ts";
import { returnNoScreen, stayOnCurrentScreen } from "../screen-route.ts";

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
}

export function goToClipboardDemo() {
  return returnNoScreen();
}
