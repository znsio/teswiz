# Setting up docker containers

It is now easy to start and stop containers for Selenium-Hub, Chrome and Firefox.

## To start the containers

    ./dockerContainers.sh up 

or 

    ./dockerContainers.sh start

## To stop the containers

    ./dockerContainers.sh down

or

    ./dockerContainers.sh stop

the [dockerContainers.sh](../dockerContainers.sh) file uses [docker-compose-v3.yml](../docker-compose-v3.yml) - which uses the latest chrome, firefox and selenium-hub.
If you need to pass proxy information to the docker container, the [dockerContainers.sh](../dockerContainers.sh) file will automatically use [docker-compose-proxy-v3.yml](../docker-compose-proxy-v3.yml) to start/stop the containers.

**The [dockerContainers.sh](../dockerContainers.sh) script also takes care of running tests on 
Windows, Linux, Mac OSX 
(intel 
chip, M1, 
M1-Pro).**

## Changes required
Each team will need to do the following changes to use this effectively:

In [dockerContainers.sh](../dockerContainers.sh) ,
* replace all references of **TESWIZ_GRID_PORT** with the actual environment variable you are using 
for setting a dynamic port via **REMOTE_WEBDRIVER_GRID_PORT** (in config.properties).
    * Default is 4444
* replace all references of **HTTP_PROXY** with the actual environment variable you are using for 
  setting the **PROXY_KEY** (in config.properties). 
  * Default is NOT_SET