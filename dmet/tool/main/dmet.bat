@echo off
@echo ************************************************
@echo Database Metadata Extract Tool V1.0
@echo Licensed to Teradata
@echo build 2012-03-03
@echo ************************************************
set CLASSPATH=.;%CLASSPATH%;.\dmet.jar
set JAVA=%JAVA_HOME%\bin\java

"%JAVA%" -jar dmet.jar C:\\Personal\\soft\\myeclipse8.5\\workspace\\dmet\\tool
@echo on