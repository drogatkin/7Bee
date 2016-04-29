<h1>7Bee</h1>
<h2>Purpose</h2>
7Bee is scripting program with main purpose to execute project building scripts. However it isn't limited to the purpose yet and can be used for any other tasks where scripting language makes sense. 
7Bee scripting language is mark up languages syntax based, so learning curve can be reasonably short. Read more about 7Bee
scripting language under <b>doc</b> directory for the project.
<h2>Getting started</h2>
Being GitHub project, 7Bee doesn't offer binaries so they should be build. 7Bee is self building tool, however if you build it
first time, you need bootstrap it. You can use either bootstrap or bootstrap.bat accordingly your platform. Needless to remind
that 7Bee is Java application so you have to have Java version 7 or better installed.
Linux boostrap script is more advanced and you need just run it and proceed with instructions. Windows script may require
adjust SDK path and also download <a href="http://www.jcraft.com/jsch/">JSCH</a> and place in 3rdparty\jsch\lib\. 
Now you can launch the script and if everything right, then
bee.jar will be created in <b>lib</b> directory. However before using 7Bee, it is recommended to install it.
<h2>Installation</h2>
Obtain administrator privileges of your platform. <i>sudo</i> can be used on Linux, or <i>cmd</i> as Administrator on Windows.
Type

   java -jar ./lib/bee.jar install

or

   java -jar .\lib\bee.jar install

Note that if you are on Linux/Mac/FreeBSD, then bootstrap script will offer you an installation.
You will be prompted few simple questions, you can just use default answers, except JDK location. Specify
correct JDK root directory on the prompt.
If your platform is Windows, then it is recommended to add 7Bee installation bin directory in your
SYSTEM path. Just change Windows environment to add the path as installation script advises.
Now you should be able to launch <b>bee</b> command and execute build scripts without limitations.


