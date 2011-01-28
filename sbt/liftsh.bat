@echo off

set DEFAULT_OPTS="-Xmx1g -noverify -XX:+CMSClassUnloadingEnabled -XX:MaxPermSize=256M -Dsbt.intransitive=true"

if "%LIFTSH_OPTS%"=="" (
  set LIFTSH_OPTS=DEFAULT_OPTS
)

java "%LIFTSH_OPTS%" -jar "%~dp0\project\sbt-launch-0.7.5.RC0.jar" %*
