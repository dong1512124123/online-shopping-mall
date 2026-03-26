@REM Maven Wrapper batch script
@echo off

@setlocal

set WRAPPER_JAR="%~dp0\.mvn\wrapper\maven-wrapper.jar"
set WRAPPER_PROPERTIES="%~dp0\.mvn\wrapper\maven-wrapper.properties"

set MAVEN_PROJECTBASEDIR=%~dp0
set MAVEN_CMD_LINE_ARGS=%*

java %MAVEN_OPTS% -jar %WRAPPER_JAR% %MAVEN_CMD_LINE_ARGS%
