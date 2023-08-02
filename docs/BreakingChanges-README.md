# Breaking Changes

# ![#f03c15](https://placehold.co/15x15/f03c15/f03c15.png) Breaking changes in Latest teswiz v0.0.81![#f03c15](https://placehold.co/15x15/f03c15/f03c15.png)

Below is the list of the breaking changes, and the corresponding new implementation starting from teswiz latest teswiz.

## Cloud Changes

In Config properties files, which are related to cloud execution platforms such as browserStack, HeadSpin, Lamda Tests, Pcloudy etc make the following change:
**CLOUD_USER** config will now be replaced with **CLOUD_USERNAME**.

## Browser Stack and Lambda Test changes
1. It is **Mandatory** to pass **CLOUD_USERNAME** & **CLOUD_KEY** as Environment variables when running on browser Stack from local
2. Similarly, All the pipelines using browser Stack will need to be updated, **CLOUD_USERNAME** & **CLOUD_KEY** needs to be added

**Note**: appium-device-farm plugin's version should be >= v8.1.0. minimum requirement is v8.1.0

**Please Refer:**
[Browser Stack Local File](https://github.com/znsio/teswiz/blob/main/docs/BrowserStackLocal_README.md)

## Method name and implementation changes

There are some method name and implementation changes as listed below:

| Purpose                                                                                            | ![#f03c15](https://placehold.co/15x15/f03c15/f03c15.png) Old ![#f03c15](https://placehold.co/15x15/f03c15/f03c15.png) | ![#c5f015](https://placehold.co/15x15/c5f015/c5f015.png) New ![#c5f015](https://placehold.co/15x15/c5f015/c5f015.png) |
|:---------------------------------------------------------------------------------------------------|:----------------------------------------------------------------------------------------------------------------------|:----------------------------------------------------------------------------------------------------------------------|
| To put App in Background for number of Seconds                                                     | putAppInBackground(int time)                                                                                          | putAppInBackgroundFor(int numberOfSeconds)                                                                            |
| Method Selects Device Notification from Notification Drawer                                        | selectNotification()	                                                                                                 | selectNotificationFromNotificationDrawer()                                                                            |
| Scroll In Dynamic Layer method is using Direction Enum instead of a String Parameter               | scrollInDynamicLayer(String direction)                                                                                | scrollInDynamicLayer(Direction direction)                                                                             |

## New Additions

There is a new method added:

| Purpose                                                                                                               | ![#c5f015](https://placehold.co/15x15/c5f015/c5f015.png) New ![#c5f015](https://placehold.co/15x15/c5f015/c5f015.png) |
|:----------------------------------------------------------------------------------------------------------------------|:----------------------------------------------------------------------------------------------------------------------|
| A new method is added to horizontal swipe an element using Gesture by passing the Direction and Element as parameters | horizontalSwipeWithGesture(WebElement element, Direction direction)                                                                                                                      |
| A new method is added to double tap on an element by passing the Element as parameter                                 | doubleTap(WebElement element)                                                                                                                                                                                         |
| A new method is added for swipe by passing the screen height and width in percentage as parameters                    | swipeByPassingPercentageAttributes(int percentScreenHeight, int fromPercentScreenWidth, int toPercentScreenWidth)     |

## Updated Usage Of Appium Driver in Methods
1. setWebViewContext()
2. setNativeAppContext()
3. scroll(Point fromPoint, Point toPoint) , scrollVertically() , scrollDownByScreenSize()
4. tapOnMiddleOfScreenOnDevice()
5. swipeLeft() , swipeRight() , swipe(int height, int fromWidth, int toWidth)

## References:
1. For appium2.0 : https://javadoc.io/doc/io.appium/java-client/8.0.0-beta/deprecated-list.html
2. For selenium 4: https://www.selenium.dev/selenium/docs/api/java/deprecated-list.html