#!/bin/bash
#set -e

echo "Upload apk to BrowserStack and if TARGET_ENVIRONMENT is UAT, Run BrowserStackLocalTesting"
artifactName=$1
artifactPath=$2

echo "artifactName is $artifactName"
echo "artifactPath is $artifactPath"

if [ -z "$artifactName" ]; then
  echo "artifactName should be set"
  exit 1
fi

if [ -z "$artifactPath" ]; then
  echo "artifactPath should be set"
  exit 1
fi


username=$SYSTEM_ACCESSTOKEN
token=$SYSTEM_ACCESSTOKEN

echo "Create temp directory, if it does not exist"
mkdir -p temp

echo "###################################### Started: Upload test apk to BrowserStack ######################################"
export http_proxy=http://prodproxy.jio.com:8080
export https_proxy=https://prodproxy.jio.com:8080
export HTTP_PROXY=http://prodproxy.jio.com:8080
export HTTPS_PROXY=https://prodproxy.jio.com:8080

newAPKFileName=$(echo $artifactName | sed 's/[-_()]*//g')
echo "New apk file name (remove '-_' from file name): " + $newAPKFileName
newAPKFilePath="./temp/$newAPKFileName"
mv $artifactPath $newAPKFilePath


export artifactName="$newAPKFileName"
export artifactPath="$newAPKFilePath"

echo "Updated artifactName=$artifactName"
echo "Updated artifactPath=$artifactPath"

echo "Uploading apk from " $artifactPath " to BrowserStack"

echo $CLOUD_USERNAME
echo $CLOUD_KEY
echo "Print Cloud USer and Key"
listOfAlreadyUploadedFiles=$(curl --insecure  -u "$CLOUD_USERNAME:$CLOUD_KEY" -X GET https://api-cloud.browserstack.com/app-automate/recent_apps)
echo "listOfAlreadyUploadedFiles: $listOfAlreadyUploadedFiles"

checkFileName=$(printf '%s\n' "${listOfAlreadyUploadedFiles[@]}" | grep $artifactName)
echo "Is file already available in Browserstack?: $checkFileName"

if [ -z "$checkFileName" ]; then
  echo "$artifactPath needs to be uploaded to Browserstack"
  uploadToBrowserstack=$(curl --insecure -u "$CLOUD_USERNAME:$CLOUD_KEY" -X POST "https://api-cloud.browserstack.com/app-automate/upload" -F "file=@$artifactPath" -F "custom_id=$artifactName")
  echo "uploadToBrowserstack: $uploadToBrowserstack"
  echo "$artifactPath uploaded to Browserstack"
  listOfFilesAfterUpload=$(curl --insecure  -u "$CLOUD_USERNAME:$CLOUD_KEY" -X GET https://api-cloud.browserstack.com/app-automate/recent_apps)
  echo "listOfFilesAfterUpload: $listOfFilesAfterUpload"
else
  echo "file $artifactName already uploaded to Browserstack"
fi
echo "(BrowserStack) Setting APP_PATH environment variable in pipeline: $artifactPath"
echo "##vso[task.setvariable variable=APP_PATH]$artifactPath"

echo ""
echo "###################################### Finished: Upload test apk to BrowserStack ######################################"
