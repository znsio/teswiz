import type { ScreenContext } from "../screen-context.ts";

const LOCATORS = {
  loginLink: "Form Authentication",
} as const;

export async function selectLogin(screen: ScreenContext): Promise<void> {
  await screen.page.getByRole("link", { name: LOCATORS.loginLink }).click();
}
