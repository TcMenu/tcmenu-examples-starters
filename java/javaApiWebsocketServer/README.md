## Very basic Java Websocket example

This example shows how to use [Java-WebSocket](https://github.com/TooTallNate/Java-WebSocket) with TcMenu API to provide the "device" side of a websocket connection. Websocket connections are normally used with [EmbedControlJS](https://github.com/TcMenu/embedcontrolJS) to provide the API connection in a browser environment. 

This is for the case where the websocket is separate to the hosting, IE you for example are using Apache or other webserver to host the static application files, and want to handle the websocket connection separately.

This is often very useful during development of the JavaScript application, as it allows you to run a simple "device" on your machine that the application can connect to. 
