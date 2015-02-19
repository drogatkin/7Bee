<h1>7Bee</h1>
<h2>Purpose</h2>
7Bee is scripting program with main purpose to execute project building scripts. However it isn't limited to the purpose yet and can be used for any other tasks where scripting language makes sense. 
7Bee scripting language is mark up languages syntax based, so learning curve can be reasonably short. Read more about 7Bee
scripting language under <b>doc</b> directory for the project.
<h2>Getting started</h2>
Being GitHub project, 7Bee doesn't offer binaries so they should be build. 7Bee is self building tool, however if you build it
first time, you need bootstrap it. You can use either bootstrap or bootstrap.bat accordingly your platform. Needless to remind
that 7Bee is Java application so you have to have Java version 7 or better installed.
Therefore before running bootstrap script, you need to specify right path to your JDK. Open bootstrap script in any text
editor and specify correct value of environment variable JAVA_HOME. 7Bee has one external dependency as JSCH. Download it from <a href="http://www.jcraft.com/jsch/">here</a>, and then specify <b>JSCH</b> variable in bootstrap script to
jsch jar file location after opening JSCH downloaded archive.
Now you can launch the script and if everything right, then
bee.jar will be created in <b>lib</b> directory. However before using 7Bee, it is recommended to install it.
<h2>Installation</h2>
Obtain administrator privileges of your platform. <i>sudo</i> can be used on Linux, or <i>cmd</i> as Administrator on Windows.
Type

   java -jar ./lib/bee.jar install

or

   java -jar .\lib\bee.jar install

You will be prompted few simple questions, you can just use default answers, except JDK location. Specify
correct JDK root directory on the prompt.
If your platform is WIndows, then it is recommended to add 7Bee installation bin directory in your
SYSTEM path. Just change Windows environment to add the path as installation script advises.
Now you should be able to launch <b>bee</b> command and execute build scripts without limitations.


