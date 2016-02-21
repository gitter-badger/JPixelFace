FROM maven:3.3-jdk-8-onbuild
RUN cp /usr/src/app/target/JPixelFace-1.0-SNAPSHOT.jar pixelface.jar
EXPOSE 8000
ENTRYPOINT java -Xmx256M -Xms256M -jar pixelface.jar