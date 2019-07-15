@echo off
@title HeavenMS
Rem set PATH=C:\Program Files\Java\jdk1.8.0_211\bin;%PATH%
Rem set CLASSPATH=.;dist\*
Rem java -Xmx2048m -Dwzpath=wz\ net.server.Server
gradlew run
pause