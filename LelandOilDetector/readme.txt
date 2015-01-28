Oil Detector prototyping
========================
The MCP3008 has its channel 0 connected.
The relay uses the GPIO pin #00 (relay 1, 2 is unused)

To run:
Start the node server on the RasPI:
Prompt> node server.js
(Ctrl+C to stop it)
It runs by default on port 9876

Then run the WebSocket feeder (that also reads the sensor and drives the relay):
Prompt> ./run.ws
(Ctrl+C to stop it)
WebSocket URI can be customized with -Dws.uri=ws://localhost:9876/
Default value is ws://localhost:9876/

From any device connected on the RasPI network, reach 
http://192.168.1.1:9876/data/display.html

The relay will turn off when the value read from the potentiometer (1-100%) goes above 75%.
