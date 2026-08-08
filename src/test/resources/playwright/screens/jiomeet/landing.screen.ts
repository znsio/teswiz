import type { ScreenContext } from "../screen-context.ts";

const LOCATORS = {
  heading: '//h3[contains(@class,"heading")]',
  description: '//p[@class="desc"]',
  startMeetingOption: '//div[contains(text(), "Start a Meeting")]',
  startMeetingButton: '//button[contains(text(), "Start")]',
  micLabel: '//div[contains(@class, "mic-section")]//img',
} as const;

export async function getSignedInWelcomeMessage(screen: ScreenContext): Promise<string> {
  const heading = (await screen.page.locator(`xpath=${LOCATORS.heading}`).textContent())?.trim() ?? "";
  const description = (await screen.page.locator(`xpath=${LOCATORS.description}`).textContent())?.trim() ?? "";
  return `${heading} ${description}`.trim();
}

export async function startInstantMeeting(screen: ScreenContext): Promise<void> {
  await screen.page.locator(`xpath=${LOCATORS.startMeetingOption}`).click();
  await screen.page.locator(`xpath=${LOCATORS.startMeetingButton}`).click();
  await screen.page.locator(`xpath=${LOCATORS.micLabel}`).waitFor();
}

export async function waitTillWelcomeMessageIsSeen(screen: ScreenContext): Promise<void> {
  await screen.page.locator(`xpath=${LOCATORS.heading}`).waitFor();
}
