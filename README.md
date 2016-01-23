# acarsclient

_acarsclient_ is a Java client for _acarsd_ servers.

[acarsd](http://www.acarsd.org/) is a free [ACARS](http://en.wikipedia.org/wiki/ACARS) decoder for Linux and Windows. It attempts to decode ACARS transmissions in real-time using soundcards. A server offers access to the database of received ACARS transmissions.

My plans were to write an Android application that gives access to ACARS databases and also informs about new ACARS transmissions.

However I discontinued the project when I found out that the original acarsd site was badly neglected. The [project's wiki](http://www.acarsd.org/wiki/index.php) was constantly unavailable, so I had to look for the documentation at archive.org. A lot of servers in the official server list did not have proper URLs or were offline/unreachable. It seems like the acarsd.org web site itself is the only reliable way to access the ACARS database.

## Source Code and License

You can find the source code [at github](https://github.com/shred/acarsclient).

This source code is licensed under [Creative Commons CC-BY-SA 3.0](http://creativecommons.org/licenses/by-sa/3.0/).

## Current Status

The communication is quite completed. The only thing that is still missing is the support of uncompressed binary packages (`AS_BINARY`).

A small test application connects to an acarsd server, starts the login procedure and initiates a search for an airline code. The transmissions from the server are decoded and printed to stdout.

There should be a common transmission manager, consisting of three threads. One thread manages the transmission queue and sends commands to the server. A second thread manages the receive queue. A third thread watches the transmission queue, and if the last transmission was a certain couple of seconds ago, it sends a heartbeat (`AS_HEART`) command. If there is no incoming traffic for a certain number of seconds, it assumes that the connection is broken.
