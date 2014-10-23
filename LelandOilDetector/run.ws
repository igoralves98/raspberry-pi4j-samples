#!/bin/bash
echo Read an ADC, feed a WebSocket
#
if [ "$PI4J_HOME" = "" ]
then
  PI4J_HOME=/opt/pi4j
fi
#
CP=./classes
CP=$CP:$PI4J_HOME/lib/pi4j-core.jar
CP=$CP:./lib/java_websocket.jar
#
sudo java -cp $CP -Dws.uri=ws://localhost:9876/ ws.WebSocketFeeder $*
