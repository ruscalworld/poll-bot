FROM openjdk:16
WORKDIR /home/container
ADD target/PollBot.jar .
CMD java -jar PollBot.jar