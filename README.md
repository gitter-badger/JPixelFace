JPixelFace
==========


[ ![Codeship Status for TehRainbowGuy/JPixelFace](https://codeship.com/projects/b7afa930-bef4-0132-4c9f-12924cb0f754/status?branch=refactor)](https://codeship.com/projects/72864)

Building
========

To build with maven just run `maven package`

Running
=======

To run with docker simply use docker-compose.
```
docker-compose build
docker-compose up
```

To run manually, you must have a pre set up redis server.

Set `REDIS_PORT_6379_TCP_ADDR` to your redis servers address and run. Example below.
```
REDIS_PORT_6379_TCP_ADDR="127.0.0.1"
java -Xmx1G -Xms1G -XX:+UseG1GC -jar /path/to/JPixelFace-1.0-SNAPSHOT.jar
```

Contributing
============
I am always looking for help and advice with this project.

If you have code contributions, feel free to open up a pull request.
For anything else, open up a ticket here or ping me on [twitter](https://twitter.com/TehRainbowGuy).
