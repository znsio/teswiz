import type { ScreenContext } from "../screen-context.ts";

const LOCATORS = {
  signInLink: '//a[text()="Sign In"]',
  welcomeBackImage: '//img[contains(@class, "signin-banner")]',
  username: "#username",
  proceedButton: "#proceedButton",
  password: "#password",
  signInButton: "#signinButton",
  joinMeetingButton: "#headerJoinMeetingButton",
  meetingId: "#meetingId",
  meetingPassword: "#pin",
  participantName: "#name",
  joinButton: '//button[contains(text(), "Join")]',
  micLabel: '//div[contains(@class, "mic-section")]//img',
} as const;

export async function signIn(screen: ScreenContext, username: string, password: string): Promise<void> {
  await screen.page.locator(`xpath=${LOCATORS.signInLink}`).click();
  await screen.page.locator(`xpath=${LOCATORS.welcomeBackImage}`).waitFor();
  await screen.page.locator(LOCATORS.username).fill(username);
  await screen.page.locator(LOCATORS.proceedButton).click();
  await screen.page.locator(LOCATORS.password).fill(password);
  await screen.page.locator(LOCATORS.signInButton).click();
}

export async function joinAMeeting(
  screen: ScreenContext,
  meetingId: string,
  meetingPassword: string,
  currentUserPersona: string,
): Promise<void> {
  await screen.page.locator(LOCATORS.joinMeetingButton).click();
  await screen.page.locator(LOCATORS.meetingId).fill(meetingId);
  await screen.page.locator(LOCATORS.meetingPassword).fill(meetingPassword);
  await screen.page.locator(LOCATORS.participantName).fill(currentUserPersona);
  await screen.page.locator(`xpath=${LOCATORS.joinButton}`).click();
  await screen.page.locator(`xpath=${LOCATORS.micLabel}`).waitFor();
}
