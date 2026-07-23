import type { ScreenContext } from "../screen-context.ts";

export async function selectLogin(screen: ScreenContext): Promise<void> {
  await screen.page.getByRole("link", { name: "Form Authentication" }).click();
}
