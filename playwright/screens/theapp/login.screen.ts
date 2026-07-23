import type { ScreenContext } from "../screen-context.ts";

export async function enterLoginDetails(
  screen: ScreenContext,
  username: string,
  password: string,
): Promise<void> {
  await screen.page.locator("#username").fill(username);
  await screen.page.locator("#password").fill(password);
}

export async function login(screen: ScreenContext): Promise<void> {
  await screen.page.getByRole("button", { name: "Login" }).click();
}

export async function getInvalidLoginError(screen: ScreenContext): Promise<string> {
  const invalidLoginError = await screen.page.locator("#flash").textContent();
  return invalidLoginError?.trim() ?? "";
}

export async function dismissAlert(): Promise<void> {
}
