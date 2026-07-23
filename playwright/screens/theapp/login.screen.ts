import type { ScreenContext } from "../screen-context.ts";

const LOCATORS = {
  username: "#username",
  password: "#password",
  loginButtonRole: "button",
  loginButtonName: "Login",
  flashMessage: "#flash",
} as const;

export async function enterLoginDetails(
  screen: ScreenContext,
  username: string,
  password: string,
): Promise<void> {
  await screen.page.locator(LOCATORS.username).fill(username);
  await screen.page.locator(LOCATORS.password).fill(password);
}

export async function login(screen: ScreenContext): Promise<void> {
  await screen.page.getByRole(LOCATORS.loginButtonRole, { name: LOCATORS.loginButtonName }).click();
}

export async function getInvalidLoginError(screen: ScreenContext): Promise<string> {
  const invalidLoginError = await screen.page.locator(LOCATORS.flashMessage).textContent();
  return invalidLoginError?.trim() ?? "";
}

export async function dismissAlert(): Promise<void> {
}
