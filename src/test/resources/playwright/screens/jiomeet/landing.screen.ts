import type { ScreenContext } from "../screen-context.ts";

const LOCATORS = {
  heading: "h3[class*='heading']",
  description: "p.desc",
  startMeetingOption: "div:has-text(\"Start a Meeting\")",
  startMeetingButton: "button:has-text(\"Start\")",
  micLabel: "div[class*='mic-section'] img",
} as const;

export async function getSignedInWelcomeMessage(screen: ScreenContext): Promise<string> {
  const heading = (await screen.page.locator(LOCATORS.heading).textContent())?.trim() ?? "";
  const description = (await screen.page.locator(LOCATORS.description).textContent())?.trim() ?? "";
  return `${heading} ${description}`.trim();
}

export async function startInstantMeeting(screen: ScreenContext): Promise<void> {
  await screen.page.locator(LOCATORS.startMeetingOption).click();
  await screen.page.locator(LOCATORS.startMeetingButton).click();
  await screen.page.locator(LOCATORS.micLabel).waitFor();
}

export async function waitTillWelcomeMessageIsSeen(screen: ScreenContext): Promise<void> {
  await screen.page.locator(LOCATORS.heading).waitFor();
}
