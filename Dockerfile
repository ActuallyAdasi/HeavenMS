# Docker support, thanks to xinyifly

FROM openjdk:8-alpine
RUN apk -U add tini
WORKDIR /mnt
ADD wz ./wz
ADD docker-launch.sh configuration.ini world.ini ./
ADD build/libs/HeavenMS.jar HeavenMS.jar
EXPOSE 8484 7575 7576 7577
ENTRYPOINT ["tini", "--"]
CMD /wait && sh ./posix-launch.sh
