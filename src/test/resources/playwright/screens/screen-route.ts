export function stayOnCurrentScreen() {
  return { teswizScreenRoute: "current" } as const;
}

export function returnNoScreen() {
  return { teswizScreenRoute: "none" } as const;
}

export function unsupportedScreenAction(message: string) {
  return { teswizScreenRoute: "unsupported", message } as const;
}
