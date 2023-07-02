# mcpingtest-standalone

A standalone Minecraft server which allows players to test their latency, built on top of Minestom. Currently supports 1.19.3.  
Supports online mode and being behind BungeeCord (+ BungeeGuard!) and Velocity.
Note: there's also a plugin version of this that can run on top of a Spigot (or it's fork) server. You can check it out [here](https://github.com/bartosz11/mcpingtest).

## Why?

The reason for both versions to exist is simple: pinging the server host or sending a HTTP request to it isn't as accurate as testing latency on top of protocol that will be used.

## How?

This version calculates latency using keep alive packets. When Minestom sends a keep alive packet (every 10 seconds) this server intercepts it, then waits for client to respond. After the client responds, the difference of timestamps of response and interception is calculated and used as latency.

## Usage

The server is pretty easy to set up:
1. Download the latest JAR file [here](https://github.com/bartosz11/mcpingtest-standalone/releases/latest).
2. Start the server with ``java -jar mcpingtest-server.jar``. You can add a RAM limit or other flags as you wish.
3. Connect to ``localhost:25565``.  

Optionally, you can edit the config that was generated in ``config.properties``. All props have comments describing what they do.
