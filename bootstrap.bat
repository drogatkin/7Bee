@echo off
rem $Id: bootstrap.bat,v 1.7 2012/04/17 22:04:26 dmitriy Exp $
set JAVA_HOME=C:\Program Files\Java\jdk1.7.0_17
set JAVA_DEPENDENCY=.\3rdparty\jdepend
set JSCH=.\3rdparty\jSch\lib\jsch-0.1.53.jar 
if exist lib goto :jdependlib
mkdir lib
:jdependlib
if exist %JAVA_DEPENDENCY%\lib goto :compile
mkdir %JAVA_DEPENDENCY%\lib
:compile
"%JAVA_HOME%\bin\javac" -source 1.5 -sourcepath %JAVA_DEPENDENCY%\src\java -d %JAVA_DEPENDENCY%\lib %JAVA_DEPENDENCY%\src\java\jdepend\framework\*.java
if errorlevel 1 goto :end
echo package org.bee;  public class CompileStamp { public static final String getStamp() { return  "Fri Aug 31 20:42:28 PDT 2012"; } }>src\java\org\bee\CompileStamp.java
"%JAVA_HOME%\bin\javac" -source 1.5 -classpath "lib;%JAVA_DEPENDENCY%\lib;%JSCH%" -sourcepath src\java -d lib src\java\org\bee\processor\*.java src\java\org\bee\util\*.java src\java\org\bee\oper\*.java src\java\org\bee\func\*.java 
if errorlevel 1 goto :end
"%JAVA_HOME%\bin\jar" -cvmf .\manifest.mf lib\bee.jar -C lib org
:end
