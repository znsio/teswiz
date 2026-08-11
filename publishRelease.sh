#!/usr/bin/env bash

set -eo pipefail

RUN_UNIT_TESTS=true

# Safety check for uncommitted changes
if [ -n "$(git status --porcelain)" ]; then
  echo "⚠️ Warning: You have uncommitted changes. Please commit or stash them before releasing."
  exit 1
fi

# 1. Detect current version from build.gradle
CURRENT_VERSION=$(grep -E '^\s*def\s+teswizVersion\s*=\s*' build.gradle | sed -E 's/.*"([^"]+)".*/\1/')
if [ -z "$CURRENT_VERSION" ]; then
  echo "❌ Error: Could not determine current version from build.gradle"
  exit 1
fi

# 2. Suggest next patch version
IFS='.' read -r major minor patch <<< "$CURRENT_VERSION"
NEXT_PATCH=$((patch + 1))
SUGGESTED_VERSION="$major.$minor.$NEXT_PATCH"

echo "Current version: $CURRENT_VERSION"
read -p "Enter version to use [$SUGGESTED_VERSION]: " USER_VERSION
VERSION=${USER_VERSION:-$SUGGESTED_VERSION}

read -p "Run unit tests before release? [Y/n]: " RUN_TESTS_CONFIRMATION
if [[ "$RUN_TESTS_CONFIRMATION" == "n" || "$RUN_TESTS_CONFIRMATION" == "N" ]]; then
  RUN_UNIT_TESTS=false
fi

# 3. Build release notes from git commits since last tag
LAST_TAG=$(git describe --tags --abbrev=0 2>/dev/null || echo "")
if [ -z "$LAST_TAG" ]; then
  echo "No previous tags found. Collecting all commits..."
  RELEASE_NOTES=$(git log --oneline)
else
  echo "Collecting commits since last tag: $LAST_TAG"
  RELEASE_NOTES=$(git log "$LAST_TAG"..HEAD --oneline)
fi

if [ -z "$RELEASE_NOTES" ]; then
  echo "⚠️ Warning: No commits found since the last tag."
  RELEASE_NOTES="- Maintenance and dependency updates."
else
  # Format git commit log to bullet points
  RELEASE_NOTES=$(echo "$RELEASE_NOTES" | sed -E 's/^[a-f0-9]+ (.*)/- \1/')
fi

# 4. Prepare temporary release notes file
TEMP_NOTES=$(mktemp)
echo "$RELEASE_NOTES" > "$TEMP_NOTES"

# 5. Show summary and ask for user confirmation
echo -e "\n========================================"
echo "Proposed Release Version: $VERSION"
echo "Run unit tests before release: $RUN_UNIT_TESTS"
echo -e "Proposed Release Notes:\n$RELEASE_NOTES"
echo "========================================\n"

read -p "Do you want to proceed with building and publishing release $VERSION? (y/n): " CONFIRM
if [[ "$CONFIRM" != "y" && "$CONFIRM" != "Y" ]]; then
  echo "Release process aborted."
  rm -f "$TEMP_NOTES"
  exit 0
fi

echo "🔄 Updating version to $VERSION in project files..."

# Update build.gradle
sed -i '' -E 's/(def teswizVersion = ")[^"]*(")/\1'"$VERSION"'\2/' build.gradle

# Update package.json and package-lock.json using npm version
if [ -f package.json ]; then
  npm version "$VERSION" --no-git-tag-version
fi

# Update README.md badge version
if [ -f README.md ]; then
  sed -i '' -E 's/(release-)[^-]+(-blue.svg\))/\1'"$VERSION"'\2/' README.md
fi

# Update Changelog.MD
if [ -f Changelog.MD ]; then
  TEMP_CHANGELOG=$(mktemp)
  echo -e "## $VERSION\n$RELEASE_NOTES\n" > "$TEMP_CHANGELOG"
  cat Changelog.MD >> "$TEMP_CHANGELOG"
  mv "$TEMP_CHANGELOG" Changelog.MD
fi

if [ "$RUN_UNIT_TESTS" = true ]; then
  echo "⚙️ Building project and running tests..."
  ./gradlew clean build shadowJar
else
  echo "⚙️ Building project without running tests..."
  ./gradlew clean build shadowJar -x test
fi

# Verify build outputs exist
JAR_FILE="build/libs/teswiz-$VERSION.jar"
SOURCES_JAR_FILE="build/libs/teswiz-$VERSION-sources.jar"
FAT_JAR_FILE="build/libs/teswiz-$VERSION-all.jar"
if [ ! -f "$JAR_FILE" ]; then
  echo "❌ Error: Built JAR file not found at $JAR_FILE"
  rm -f "$TEMP_NOTES"
  exit 1
fi
if [ ! -f "$FAT_JAR_FILE" ]; then
  echo "❌ Error: Built fat JAR file not found at $FAT_JAR_FILE"
  rm -f "$TEMP_NOTES"
  exit 1
fi

echo "📦 Committing, tagging, and pushing changes to GitHub..."
git add build.gradle package.json package-lock.json README.md Changelog.MD
git commit -m "Release $VERSION"
git push origin main
git tag "$VERSION"
git push origin "$VERSION"

# 6. Trigger Jitpack build as soon as the tag is pushed, before uploading release artifacts.
# Jitpack builds directly from the git tag - it doesn't depend on GitHub Release assets at all -
# so kicking it off now lets it build in the background while the (slow, ~500MB) fat jar upload
# happens next. A short wait guards against Jitpack querying GitHub before the tag has propagated;
# if Jitpack still doesn't recognize the tag ("isTag": false) on the first try, retry once after a
# longer delay. Either way this is best-effort - Jitpack builds lazily on first real consumer
# request regardless, so a failed trigger here doesn't block the release.
JITPACK_URL="https://jitpack.io/api/builds/com.github.anandbagmar/teswiz/$VERSION"
echo "🔗 Triggering Jitpack build for com.github.anandbagmar/teswiz $VERSION..."
sleep 5
JITPACK_RESPONSE=$(curl -s "$JITPACK_URL" || echo '{}')
if [ "$(echo "$JITPACK_RESPONSE" | jq -r '.isTag // false')" != "true" ]; then
  echo "  Tag not yet visible to Jitpack, retrying in 20s..."
  sleep 20
  JITPACK_RESPONSE=$(curl -s "$JITPACK_URL" || echo '{}')
  if [ "$(echo "$JITPACK_RESPONSE" | jq -r '.isTag // false')" != "true" ]; then
    echo "  ⚠️ Jitpack still hasn't picked up the tag - it will build lazily on first consumer request instead."
  fi
fi
echo -e "\nJitpack build queued: https://jitpack.io/#com.github.anandbagmar/teswiz/$VERSION"

echo "🚀 Creating GitHub Release $VERSION with the thin and sources jars..."
gh release create "$VERSION" "$JAR_FILE" "$SOURCES_JAR_FILE" \
  --title "$VERSION" \
  --notes-file "$TEMP_NOTES"

echo "📤 Uploading fat jar (this can take a while)..."
gh release upload "$VERSION" "$FAT_JAR_FILE"

# 7. Prune artifacts of older releases (keep the last 3)
echo "🧹 Pruning older release artifacts (keeping top 3)..."
idx=0
gh release list --limit 100 --json tagName --jq '.[].tagName' | while read -r tag; do
  if [ $idx -ge 3 ]; then
    echo "  Pruning artifacts from older release: $tag"
    assets=$(gh release view "$tag" --json assets --jq '.assets[].name' 2>/dev/null || echo "")
    for asset in $assets; do
      echo "    Deleting asset: $asset"
      gh release delete-asset "$tag" "$asset" -y
    done
  fi
  idx=$((idx+1))
done

echo -e "\n✅ Release $VERSION successfully published!"

rm -f "$TEMP_NOTES"
