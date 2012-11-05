@echo on
rem $Id: bootstrap.bat,v 1.7 2012/04/17 22:04:26 dmitriy Exp $
set JAVA_HOME=C:\Program Files\Java\jdk1.7.0_03
set JAVA_DEPENDENCY=.\3rdparty\jdepend
set JSCH=C:\tools\libs\jsch-0.1.47\jsch-0.1.47.jar 
if exist lib goto :compile
mkdir lib
:compile
"%JAVA_HOME%\bin\javac" -source 1.5 -sourcepath %JAVA_DEPENDENCY%\src\java -d %JAVA_DEPENDENCY%\lib %JAVA_DEPENDENCY%\src\java\jdepend\framework\*.java
"%JAVA_HOME%\bin\javac" -source 1.5 -classpath "lib;%JAVA_DEPENDENCY%\lib;%JSCH%" -sourcepath src -d lib src\org\bee\processor\*.java src\org\bee\util\*.java src\org\bee\oper\*.java src\org\bee\func\*.java 
if errorlevel 1 goto :end
"%JAVA_HOME%\bin\jar" -cvmf .\manifest.mf lib\bee.jar -C lib org
:end