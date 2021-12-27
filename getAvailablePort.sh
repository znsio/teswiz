gridPort=4444
appiumPort=4723
projectName=`pwd | rev | cut -d'/' -f 1 | rev`
echo $projectName
while lsof -Pi :${gridPort} -sTCP:LISTEN -t >/dev/null ; do
    echo "port -  ${gridPort} is consumed, checking the next port"
    gridPort=`expr ${gridPort} + 1`
    echo $gridPort
done

while lsof -Pi :${appiumPort} -sTCP:LISTEN -t >/dev/null ; do
    echo "port -  ${appiumPort} is consumed, checking the next port"
    appiumPort=`expr ${appiumPort} + 1`
    echo $appiumPort
done

export APPIUM_PORT=$appiumPort
export GRID_PORT=$gridPort
