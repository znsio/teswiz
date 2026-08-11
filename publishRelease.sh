#!/usr/bin/env bash

set -eo pipefail

JITPACK_GROUP_ARTIFACT="com.github.anandbagmar/teswiz"

RUN_UNIT_TESTS=true
CURRENT_VERSION=""
VERSION=""
RELEASE_NOTES=""
TEMP_NOTES=""
JAR_FILE=""
SOURCES_JAR_FILE=""
FAT_JAR_FILE=""

check_working_tree_clean() {
  if [ -n "$(git status --porcelain)" ]; then
    echo "⚠️ Warning: You have uncommitted changes. Please commit or stash them before releasing."
    exit 1
  fi
}

detect_current_version() {
  CURRENT_VERSION=$(grep -E '^\s*def\s+teswizVersion\s*=\s*' build.gradle | sed -E 's/.*"([^"]+)".*/\1/')
  if [ -z "$CURRENT_VERSION" ]; then
    echo "❌ Error: Could not determine current version from build.gradle"
    exit 1
  fi
}

prompt_release_version() {
  IFS='.' read -r major minor patch <<< "$CURRENT_VERSION"
  local next_patch=$((patch + 1))
  local suggested_version="$major.$minor.$next_patch"

  echo "Current version: $CURRENT_VERSION"
  read -p "Enter version to use [$suggested_version]: " user_version
  VERSION=${user_version:-$suggested_version}
}

prompt_run_tests() {
  read -p "Run unit tests before release? [Y/n]: " run_tests_confirmation
  if [[ "$run_tests_confirmation" == "n" || "$run_tests_confirmation" == "N" ]]; then
    RUN_UNIT_TESTS=false
  fi
}

build_release_notes() {
  local last_tag
  last_tag=$(git describe --tags --abbrev=0 2>/dev/null || echo "")
  if [ -z "$last_tag" ]; then
    echo "No previous tags found. Collecting all commits..."
    RELEASE_NOTES=$(git log --oneline)
  else
    echo "Collecting commits since last tag: $last_tag"
    RELEASE_NOTES=$(git log "$last_tag"..HEAD --oneline)
  fi

  if [ -z "$RELEASE_NOTES" ]; then
    echo "⚠️ Warning: No commits found since the last tag."
    RELEASE_NOTES="- Maintenance and dependency updates."
  else
    RELEASE_NOTES=$(echo "$RELEASE_NOTES" | sed -E 's/^[a-f0-9]+ (.*)/- \1/')
  fi

  TEMP_NOTES=$(mktemp)
  trap 'rm -f "$TEMP_NOTES"' EXIT
  echo "$RELEASE_NOTES" > "$TEMP_NOTES"
}

confirm_release() {
  echo -e "\n========================================"
  echo "Proposed Release Version: $VERSION"
  echo "Run unit tests before release: $RUN_UNIT_TESTS"
  echo -e "Proposed Release Notes:\n$RELEASE_NOTES"
  echo "========================================\n"

  read -p "Do you want to proceed with building and publishing release $VERSION? (y/n): " confirm
  if [[ "$confirm" != "y" && "$confirm" != "Y" ]]; then
    echo "Release process aborted."
    exit 0
  fi
}

update_version_in_project_files() {
  echo "🔄 Updating version to $VERSION in project files..."

  sed -i '' -E 's/(def teswizVersion = ")[^"]*(")/\1'"$VERSION"'\2/' build.gradle

  if [ -f package.json ]; then
    npm version "$VERSION" --no-git-tag-version
  fi

  if [ -f README.md ]; then
    sed -i '' -E 's/(release-)[^-]+(-blue.svg\))/\1'"$VERSION"'\2/' README.md
  fi

  if [ -f Changelog.MD ]; then
    local temp_changelog
    temp_changelog=$(mktemp)
    echo -e "## $VERSION\n$RELEASE_NOTES\n" > "$temp_changelog"
    cat Changelog.MD >> "$temp_changelog"
    mv "$temp_changelog" Changelog.MD
  fi
}

build_project() {
  if [ "$RUN_UNIT_TESTS" = true ]; then
    echo "⚙️ Building project and running tests..."
    ./gradlew clean build shadowJar
  else
    echo "⚙️ Building project without running tests..."
    ./gradlew clean build shadowJar -x test
  fi
}

verify_build_outputs() {
  JAR_FILE="build/libs/teswiz-$VERSION.jar"
  SOURCES_JAR_FILE="build/libs/teswiz-$VERSION-sources.jar"
  FAT_JAR_FILE="build/libs/teswiz-$VERSION-all.jar"

  if [ ! -f "$JAR_FILE" ]; then
    echo "❌ Error: Built JAR file not found at $JAR_FILE"
    exit 1
  fi
  if [ ! -f "$FAT_JAR_FILE" ]; then
    echo "❌ Error: Built fat JAR file not found at $FAT_JAR_FILE"
    exit 1
  fi
}

commit_tag_and_push() {
  echo "📦 Committing, tagging, and pushing changes to GitHub..."
  git add build.gradle package.json package-lock.json README.md Changelog.MD
  git commit -m "Release $VERSION"
  git push origin main
  git tag "$VERSION"
  git push origin "$VERSION"
}

# Jitpack builds directly from the git tag - it doesn't depend on GitHub Release assets at all -
# so kicking it off now lets it build in the background while the (slow, ~500MB) fat jar upload
# happens next. A short wait guards against Jitpack querying GitHub before the tag has propagated;
# if Jitpack still doesn't recognize the tag ("isTag": false) on the first try, retry once after a
# longer delay. Either way this is best-effort - Jitpack builds lazily on first real consumer
# request regardless, so a failed trigger here doesn't block the release.
trigger_jitpack_build() {
  local jitpack_url="https://jitpack.io/api/builds/$JITPACK_GROUP_ARTIFACT/$VERSION"
  echo "🔗 Triggering Jitpack build for $JITPACK_GROUP_ARTIFACT $VERSION..."

  sleep 5
  if [ "$(jitpack_is_tag_visible "$jitpack_url")" != "true" ]; then
    echo "  Tag not yet visible to Jitpack, retrying in 20s..."
    sleep 20
    if [ "$(jitpack_is_tag_visible "$jitpack_url")" != "true" ]; then
      echo "  ⚠️ Jitpack still hasn't picked up the tag - it will build lazily on first consumer request instead."
    fi
  fi
  echo -e "\nJitpack build queued: https://jitpack.io/#$JITPACK_GROUP_ARTIFACT/$VERSION"
}

jitpack_is_tag_visible() {
  local jitpack_url="$1"
  local response
  response=$(curl -s "$jitpack_url" || echo '{}')
  echo "$response" | jq -r '.isTag // false'
}

create_github_release() {
  echo "🚀 Creating GitHub Release $VERSION with the thin and sources jars..."
  gh release create "$VERSION" "$JAR_FILE" "$SOURCES_JAR_FILE" \
    --title "$VERSION" \
    --notes-file "$TEMP_NOTES"
}

upload_fat_jar() {
  echo "📤 Uploading fat jar (this can take a while)..."
  gh release upload "$VERSION" "$FAT_JAR_FILE"
}

prune_old_release_artifacts() {
  echo "🧹 Pruning older release artifacts (keeping top 3)..."
  local idx=0
  gh release list --limit 100 --json tagName --jq '.[].tagName' | while read -r tag; do
    if [ $idx -ge 3 ]; then
      echo "  Pruning artifacts from older release: $tag"
      local assets
      assets=$(gh release view "$tag" --json assets --jq '.assets[].name' 2>/dev/null || echo "")
      for asset in $assets; do
        echo "    Deleting asset: $asset"
        gh release delete-asset "$tag" "$asset" -y
      done
    fi
    idx=$((idx+1))
  done
}

main() {
  check_working_tree_clean
  detect_current_version
  prompt_release_version
  prompt_run_tests
  build_release_notes
  confirm_release

  update_version_in_project_files
  build_project
  verify_build_outputs

  commit_tag_and_push
  trigger_jitpack_build
  create_github_release
  upload_fat_jar
  prune_old_release_artifacts

  echo -e "\n✅ Release $VERSION successfully published!"
}

main
