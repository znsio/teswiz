import type { ScreenContext } from "../screen-context.ts";

const LOCATORS = {
  input: "#demo-input",
  output: "#demo-output",
} as const;

export async function open(screen: ScreenContext, url: string): Promise<void> {
  await screen.page.goto(url, { waitUntil: "load" });
}

export async function enterValue(screen: ScreenContext, value: string): Promise<void> {
  await screen.page.locator(LOCATORS.input).fill(value);
  await screen.page.locator(LOCATORS.output).evaluate((element, nextValue) => {
    element.textContent = String(nextValue);
  }, value);
}

export async function readValue(screen: ScreenContext): Promise<string> {
  const currentValue = await screen.page.locator(LOCATORS.output).textContent();
  return currentValue?.trim() ?? "";
}
