@echo off

set "PROJECT_HOME=@PROJECT_HOME@"
set "PROJECT_DEPS=@PROJECT_DEPS@"
cd "%PROJECT_HOME%"
set CP=
set CP=@PROJECT_DEPS@;%CP%
set CP=build\classes;%CP%
for %%i in (build\lib\*.jar) do call "%PROJECT_HOME%\build\run\cp.bat" %%i
set CP=build\classes;%CP%
set CP=build\plugins;%CP%
set CP=build\test;%CP%
set CP=src\test\junit-resources;%CP%

java -classpath "%CP%" @LOG4J@ %*
