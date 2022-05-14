# 7Bee

## Purpose
7Bee is a scripting program with main purpose to execute project building scripts. However it isn't limited to the purpose yet and can be used for any other tasks where a scripting language makes sense. 
7Bee scripting language is a mark up languages syntax based, so a learning curve can be reasonably short. Read more about 7Bee
scripting language under <b>doc</b> directory for the project.
## Getting started
Being GitHub project, 7Bee doesn't offer binaries so they should be built. 7Bee is a self building tool, however if you build it
first time, you need bootstrap it. You can use either bootstrap or bootstrap.bat accordingly your platform. Needless to remind
that 7Bee is Java application so you have to have Java version 11 or better installed.
The Linux bootstrap script is more advanced and you need just run it and proceed with instructions. The Windows script may require
adjust JDK path and also download <a href="http://www.jcraft.com/jsch/">JSCH</a> and place it in 3rdparty\jsch\lib\. 
bee.jar will be created in <b>lib</b> directory after running bootstrap. However before using 7Bee, it is recommended to install it.
## Installation
Obtain administrator privileges of your platform. <i>sudo</i> can be used on Linux, or <i>cmd</i> as Administrator on Windows.
Type

   java -jar ./lib/bee.jar install

or

   java -jar .\lib\bee.jar install

Note that if you are on Linux/Mac/FreeBSD, then bootstrap script will offer you an installation.
You will be prompted few simple questions, you can just use default answers. Keep attention only to JDK root directory on
WIndows systems.
If your platform is Windows, then it is recommended to add 7Bee installation bin directory in your
SYSTEM path. Just change Windows environment to add the path as the installation script advises.
Now you should be able to launch <b>bee</b> command and execute build scripts without limitations.

## Documentation

7Bee uses a language with a simple set of predefined operations for creation build scripts. The language is XML based, so [DTD](http://7bee.j2ee.us/xml/DTD/bee.dtd) is very helpful to understand general rules of creation language constructions. Although base set of operations is very limited, it can be easily extended by adding user defined operators and functions. Creation specialized tasks can also reduce efforts of a preparation of build scripts. Another powerful thing is weakly hardcoded DTD, so generally the 7Bee processor can be configured for parsing different languages by just changing a grammar definition in externally defined file. See [Getting Started](#Getting Started) section how to start using 7Bee.

### Basic 7Bee language elements

7Bee provides variables, expression, operators, functions, conditional operators, and loops. All variables are typeless, and they obtain a global scope in context where they defined. 

#### Basic statements

The set include statements for creation build files.

*   _variable_ - defines a variable with specified name and set value when specified. [example](#variable)
*   _value_ - specifies a value which can be a literal constant, a value of variable, a name of a property, a directory, a path, eval, url, a repo\_artifact and some others. Uses the attribute type for qualification.
    
    *   to get property value: **name**\="property\_name" **type**\="_property_"
    *   to consider value as directory: **type**\="_directory_"
    *   to consider value as absolute path: **type**\="_path_"
    *   to consider value as array: **type**\="_array_", default separator in a blank, unless **separator** defined.
    *   to consider value as file or array of files: **type**\="_file_", wildcard extended to get matching array of files without extending name to absolute path
    *   to consider value as environment value: **type**\="_environment_" **name**\="name of env variable"
    *   to consider value as number: **type**\="_number_"
    *   to consider value as date: **type**\="_date_"
    *   to consider value as name of variable **type**\="_eval_"
    *   to consider value as URL **type**\="_url_"
    *   to consider value as a repository artifact **type**\="_repo\_artifact_"
    
    The Maven repository artifact format is _maven:vendor:product:version_.
    
    The final value of a value gets a template processed.(since 1.3) A template element looks like:  
    `${name}`  
    The name is a name of any variable in the current scope. The name can't be longer than 128 characters. If a name is longer, then the element gets ignored. If a value ends before `}`, then the element is ignored as well. If there is no an element with the provided _name_, then the template element gets ignored (some difference in a behavior of a non template element variable). A value of a template element is considering as a template as well. A recursive processing stops as all templates got expanded or a loop is detected. If a template element is an array, then the following syntax gets considered:  
    `${name,separator}`_The approach is under consideration_  
    a name can't contain commas. The separator is used to separate values and considered as empty string when not provided.
    
*   _target_ \- defines an addressable build task. Attribute **name** defines a name of target which can be specified in command line of 7Bee. Attribute **dir** is optional and provides a work directory for target if it's different than for a project. A target description can be specified in attribute **comment**. [example](#target)
*   _expression_ - allows to combine operators functions to set a variable value. [example](#expression)
*   _task_ - define a build task. Attributes **name** and **code** used for calling a class, **exec** for an OS executable, **tool** for Java build tool as a compiler, jar or others when _code_ doesn't work due security restrictions of JDK 11 and up, **path** provides additional path or class path. Attributes **stdout**, **stderr** and **stdin** can specify variables keeping a content of corresponding streams of a running process. Parameters (_parameter_) specify strings passed in command line or main() arguments. If name of a parameter specified then it is used as a definition which is available by _System.getProperty()_. When both attributes _code_ and _tool_ are specified, tool is used for JDK 11 and up. [example](#task)
*   _dependency_ - define a a dependency of a build task. All dependencies are evaluated. Any dependency returned true or not empty result means that a task will be executed, unless a dependency has attribute process\_only="yes". [exampe](#dependency)
*   _for_ - define iteration. Attributes specify iteration **variable**, **in** interval, and an optional **separator** to convert scalar interval value in an array. [example](#for)
*   _if_ - if statement. Calculate first _expression_ and then execute _then block_ if expression value is true, otherwise _else block_. _else block_ can be omitted. [example](#if)
*   _block_ - allows to combine different statements in one batch. [example](#block)
*   _else_ - else branch inside **if**, since 1.1
*   _then_ - then branch inside **if**, since 1.1
*   _elseif_ - else branch inside **if** starting a new if block, since 1.1
*   _echo_ - displays a value or a variable on a console. Two options are available, as  _\-n_ do not output the trailing newline, and _\-E_ disable interpretation of escape sequences in Strings
*   _switch_ - provides switch, attribute **variable** defines a value which checked against attribute **value** of _case block_s. A _default block_ is executed when no one other block matches.. [example](#switch)
*   _function_ - calls a function, see predefined functions below. Function can be also custom defined. [example](#function)
*   _parameter_ - used for specifying parameters for _task_s and _function_s. It acts the same as a _value_, however it can set system variables with value specified., when used with types _environment_ or _property_ .
*   _operator_ - executes an operator, see list of available operators below. An operator can be custom defined. [example](#operator)
*   _interrupt_ - interrupts current context execution, useful for early termination of loops or leaving blocks.

Note that 7Bee is highly expandable language for creation build files, test scripts, and many others tasks. Therefore, set of above statements can be extended by adding new ones. An external grammar definition can be applied without recompiling of any core code.

#### Operators

Generally any operator defined as a two addresses operation, when it applied on two operands and result takes place of first operand. Further a result used as first operand for the next operand in the chain. An operator can have unlimited number of operands. An operand can be a value, a variable or a result of a function. There are no parentheses to prioritize or group operands. However operators can be nested and operators of lower levels are calculated first. An operator has a required attribute _name_ and an optional _variable_ specifying a variable name for a result, otherwise an operator result passed to up level context. The first version of 7Bee doesn't have calling operators ability, so most of expressions will look a bit bulky. It considered as not a big disadvantage, because build scripts are very rarely modified, therefore one time creation efforts can be neglected in a lifetime of a project.

*   **append** - provides concatenation of variables and/or values. 
*   **array** - groups set of scalar or vector operands in a vector or array.
*   **eq** - returns true if all operands are equal.
*   **exclude** - used for excluding elements from array or vector based on a regular expression. The operator arguments can be as single regular expressions as arrays of them.
*   **lt** - less than.
*   **neq** - not equal.
*   **and** - logical AND
*   **or** - logical OR
*   **xor** - logical XOR
*   **not** - logical NOT

Some other operators can be build using the basis. Set of operators is customizable, when an operator _name_ is a name of a class of its implementation. All operators are considered in package _org.bee.oper_. Basic arithmetic operations are implemented as **calc** function currently.

#### Functions

Set of functions is expandable and useful for different build tasks. Functions can be used in parameters of a _task_, or in _expression_s and _operator_s.

*   **anynewer** - scans for last modification time of files defined by first parameter and last modification time specified by second and returns true if most newest file in first parameter newer than most newest file in second parameter. Any parameter can be a directory, in this case entire directory is checked. Filtering is allowed using Wildcard char in file names.(**allnewer** depreciated)
*   **ask** - displays a prompt to a user specified by first parameter, second parameter provides default answer if a user pressed enter.
*   **askpass** - displays a prompt to a user specified by first parameter, second parameter provides default answer for empty input. User input isn't echoed, so this function can be used for entering sensitive data. Available since Java 6.
*   **element** - gives element of an array specified by first parameter with index specified by second
*   **calc** - calculates expression given by parameters. Numbers and following operations +,-,\*,/,(,),power, sqrt, sqr, sin, cos, tan, atan, log, ln are allowed.. Calc acknowledges order of operations and priority of \*,/,power, and (). Parameters have to be scalar, however if first parameter is array, then its elements represent an expression elements and other parameter get ignored.
*   **cp, cpa** - file copy command similar used for Unix. Number of parameter pairs is not limited.
*   **scp** - secure network file copy command similar to scp used for Unix. It copies to host and takes two parameters : **source file name** in local system and remote file name in format: _user:password@host:remote\_file_. Optional 3rd parameter can specify connection port number unless it is 22. '@' is not allowed in remote file name.
*   **cropname** - removes a part of a file name defined as a regular expression. First parameter specifies a name, second - a regular expression. An optional 3rd parameter defines a string to substitute a deleted part. And an optional forth parameter specifies 'true' | 'all' if all matches need to be substituted. If a name includes wildcard symbols like \*, or ?, then it considered as a mask for file names to perform the operation on. The function returns always an array.
*   **filename** - extracts file name from an arbitrary name, it cuts extension.
*   **mkd** - creates directories from list of parameters. Returns array of success code for every parameter. Directories get created from current work directory unless fully qualified name is specified.
*   **mv** - similar to cp, but does move.
*   **match** - matches string value to pattern, 1st parameter is a pattern, second is a string. More about patterns is [here](http://java.sun.com/j2se/1.5.0/docs/api/java/util/regex/Pattern.html).
*   **newerthan** - compares last modification time of files defined by first parameter with last modification time corresponding files defined by second parameters and returns a list of files with later time. If extensions are defined in first and second parameters, then they are ignored to find corresponding files.
*   **newerwithdependency** - is similar to _newerthan_ , but looks in Java dependency, so for example if some Java file has newer date than corresponding byte code file, all dependent on this byte code Java files will be added to a  result. One or two additional parameters specify default class name to return, unless non empty result produced, and a package prefix, when source directory points to not top level package.
*   **now** - returns current time.
*   **read** - reads content of file and returns it as a result, 1st parameter is a file name, 2nd a separator pattern for returning an array, and 3rd is encoding.
*   **region** - extracts groups from a string specified by first parameter and identified by a regular expression specified as second parameter. Returns always an array.
*   **rm** - removes files defined in parameters.
*   **scalar** - convenient function to convert an array to single value, 1st parametere specifies an array and 2nd an optional separator.
*   **stop** - stops processing of 7Bee build file.
*   **string** - provides string operations as one of the following:
    *   upper - convert to upper case
    *   lower - convert to lower case
    *   capital - convert to capitalized case
    *   trim - trim string removing white spaces at begining and end
    *   reverse - reverse characters in string
    *   trunc - truncate string length to specified length by third parameter
    *   sub - extract substring of string using range indexes from third and forth parametersFirst parameter is a value/variable considered as a string, second parameter specifies one of above operation, and thrird and forth parametrs are used for trunc and sub operations to specify length or range.
*   **timestamp** - returns a time stamp of a file specified as a parameter.
*   **touch** - is similar to Unix touch command, updates time of last file modification to **now**.
*   **unzip** - unzip file provided by first parameter in a directory specified by second parameter. If the former isn't specified then current directory is used. Extra parameters can be added to unzip specific entries matching to this parameters. Wild cards are supported for names. It is possible to use a prefix like "./" to match any directories depth like \*/\*..\*.'
*   **warit** - builds .war file, parameters define 1. web.xml, then sets of parameters defining .war content. See mode detail [below](#warit).
*   **write, writea** - creates a new file with content specified by parameters, 1st provides file name, 2nd and following provide content of file, every new parameter just appends to the file (no automatic line feed happens).
*   **zip** - zip in a file provided by first parameter. Files or directories to zip are specified by following parameters. Array type parameters are supported. If a name of added file ended with /., then it is considered as a directory, with adding only its content. If a wildcard is used, and prepended by ./, then it is recursively applied for subdirectories. If a file name ended with _!._, then it gets unzipped first and then its content added to the result archive.

A function name is a class name in package _org.bee.func_. Function can have a variable number of Object or String parameters, a result can be any type, which will be interpreted accordingly context where the function met.

#### Scope rules

A variable obtains scope of its declaration and below. So variables with the same name can coexist as different only in sibling branches. When any statement is processed, all variables get evaluated first in order of appearing.

#### Changeable Grammar

A language understandable by 7Bee can be changed. Take a look at _alt-grammar.bgr_ in examples. It provides a handler class for paths in XML tree. So generally 3 different changes can be done:

1.  changing tag names 
2.  changing tag hierarchy
3.  adding new tags with corresponding handlers

All these changes should be accompanied corresponding changes of DTD.

#### Function parameter details

##### warit

This function creates **.war** file specified by 1st parameter

2nd provides a location of _web.xml_, but it can be considered as content of it, if the location can't be resolved. (a standard 7Bee behavior)

Further parameters grouped by 2 or more parameters, and 1st in a group provides

operation abbreviations, like

*   **A \[prfx\]** \- add files to .war
    1.  source path, can include wild cards like \* or ?
*   **E \[prfx\]** \- add files not in exclude list
    1.  source path, can include wild cards like \* or ?
    2.  exclude list comma separated with names only, wild card can be used as well
*   **C \[prfx\]** \- add files in classes
    1.  source path
    2.  exclude list, can be empty
*   **L \[prfx\]** \- add files in library
    1.  source path
    2.  exclude list, can be empty
*   **W filename\_path** \- add content of parameter
    1.  content to add

prfx -is path in .war to add files, it shouldn't start with / or \\, if source path represent a single file, then prefix will be destination file name, unless it ended by / or /\*. If a group started parameter has type an array, then its content considered as additional parameters. It allows to create group of parameters separately.

##### ask

1st parameter provides prompt and 2nd default value. Result of the function is user input or default value if user entered nothing.

#### 7Bee file structure

7Bee build file starts with entities definitions. They used for definition build elements used multiple times, for example output directories, file names, main class name and so on. Using entities simplifies maintenance build file and allows to reuse it. Then global variables can be defined, for example class paths. The rest includes multiple target sections. If name of target section not specified at starting 7bee, then an execution starts from the last target section. Every target section can include several dependencies. In this case all dependencies evaluated first. If dependency is a target section, then process repeated recursively.

#### Predefined variable names

Bee provides a mechanism of predefined variable 

1.  ~#dir#~ - current working directory
2.  ~#args#~ - array of command line arguments
3.  ~#error#~ - value of last error
4.  ~#exception#~ - value of last exception
5.  ~#option-noinput#~ - value set if option -noinput used
6.  ~#build-file#~ - path of build file
7.  ~#class-lib#~ - array of class libraries set by command argument -lib

**Help to promote 7Bee**

If you use 7Bee in your development, then you can help to promote this tool. You can add a small icon somewhere on your site web pages inserting the following simple fragment:

<a href="https://github.com/drogatkin/7Bee"><img src="https://github.com/drogatkin/7Bee/blob/master/doc/7bee\_logo\_avatar.png?raw=true" border=0></a>

**Getting Started**

Although 7Bee build files look bulky, creation of a build script is fairly simple when templates of build files used. Folder _examples_ of 7Bee distribution contains many useful templates for different type of projects. Consider a simple project with compilation, jar creation and run tasks. Examples for such projects can be found in _examples/generic_ folder. The templates consider some project tree structures, with separate folders for sources, result of compilation and build target files. Let's create a classic **Hello, world!** project.

1.  cd in user home directory and then create a project directory, mkdir helloworld-bee
2.  create src directory, mkdir src
3.  copy generic 7Bee templates form 7bee\\deploy\\examples\\generic\\bee.xml and 7bee\\deploy\\examples\\generic\\env.xml
4.  step in src directory (cd src) and create domain directory, mkdir userdomain
5.  step in userdomain directory and create HelloWorld.java, echo "package userdomain; public class HelloWorld { public static void main(String ... args) { System.out.printf(\\"Hello, World\\!\\\\n\\");}}" > HelloWorld.java
6.  go back in the project home directory (home/userdomain/helloworld-bee) and edit bee.xml, vi bee.xml
    
    before:
    
    <?xml version="1.0" encoding="utf-8"?>
     <!DOCTYPE bee PUBLIC "-//Dmitriy Rogatkin//DTD 7Bee Project Builder 1.0//EN"
        "http://7bee.j2ee.us/xml/DTD/bee.dtd" \[
          <!ENTITY env SYSTEM "./env.xml">
          <!ENTITY build\_directory "your build directory">
    
          <!ENTITY source\_directory "src">
          <!ENTITY build\_file "your jar.jar">
          <!ENTITY domain "com">
          <!ENTITY manifestf "type relative path to manifest">
          <!ENTITY main\_class "type main class for run">
          \]>
    
    after:
    
    <?xml version="1.0" encoding="utf-8"?>
     <!DOCTYPE bee PUBLIC "-//Dmitriy Rogatkin//DTD 7Bee Project Builder 1.0//EN"
        "http://7bee.j2ee.us/xml/DTD/bee.dtd" \[
          <!ENTITY env SYSTEM "./env.xml">
          <!ENTITY build\_directory "build">
          <!ENTITY source\_directory "src">
          <!ENTITY build\_file "helloworld.jar">
          <!ENTITY domain "userdomain">
          <!ENTITY manifestf "">
          <!ENTITY main\_class "userdomain.HelloWorld">
          \]>
    
7.  Now you can run 7Bee.
    
    \[dmitriy@BEEMACHINE mydomain\]$bee  
    Compiling...  
    Jarring...  
    Running...  
    Hello, World!
    

**Examples**

#### variable

     <variable name="jdk version">1.5</variable>
     <variable name="resource path" type="path">/src/resources</variable>

#### expression

     <expression variable="class path">
        <operator name="append">
           <value variable="jdk root"/>
           <value variable="jdk version"/>
           <value>/lib/rt.jar</value>
        </operator>
     </expression>

#### task

<task exec="mkdir">
       <parameter value="&build\_directory;"/>
</task>
<task name="main" code="com.sun.tools.javac.Main" path="compiler classes">
  <parameter value="-classpath"/>
  <parameter variable="class path"/>
  <parameter value="-source"/>
  <parameter value="1.5"/>
  <parameter value="-d"/>
  <parameter value="&build\_directory;" type="dir"/>
  <parameter variable="java sources"/>>
  <onexit>
     <if>
        <expression>
           <operator name="neq"><value variable="resultcode"/><value>0</value></operator>
        </expression>
        <block type="then">
           <echo>Error(s) at compilation</echo>
           <function name="stop">
	      <parameter value="1"/>
           </function>
        </block>
     </if>
  </onexit>
</task>
    

#### dependency

     <dependency>
        <expression>
          <operator name="eq">
            <function name ="timestamp">
               <parameter value="lib"/>
            </function>
            <value/>
          </operator>
        </expression>
     </dependency>
     <dependency target="check lib"/>
     

#### for

<for variable="idl file" in="event, naming IDLs">
      <task name="idl compilation" exec="idl compiler">
         <parameter variable="old compatible"/>
         <parameter value="-i"/>
         <parameter variable="include\_lib"/>
         <parameter value="-i"/>
         <parameter variable="common\_src"/>
         <parameter value="-keep"/>
         <parameter value="-fall"/>
         <parameter value="-td"/>
         <parameter variable="Java src common"/>
         <parameter variable="packages list">
            <expression variable="packages list">
              <variable name="packages list"/>
              <for variable="package name" in="event, naming IDLs">
                  <operator name="array" variable="packages list">
                      <value variable="packages list"/>
                      <value>-pkgPrefix</value>
                      <function name="filename">
                         <parameter variable="package name"/>
                      </function>
                      <value>org.omg</value>
                  </operator>
               </for>
            </expression>
         </parameter>
         <parameter variable="idl file"/>
         <onexit>
            <if>
              <expression>
                 <operator name="neq"><value variable="resultcode"/><value>0</value></operator>
              </expression>
              <block type="then">
                 <echo>Error in IDL</echo>
                 <function name="stop">
			<parameter value="1"/>
                 </function>
              </block>
              <block type="else">
                  <function name="write">
                     <parameter variable="idl.timestamp"/>
                     <parameter value=""/>
                  </function>
              </block>
            </if>
        </onexit>
      </task>
    </for>
      

#### if

<if>
   <expression>
      <operator name="eq">
         <value>80</value>
         <value variable="access port"/>
      </operator>
   </expression>
   <block type="then">
      <expression variable="access port">
         <value></value>
      </expression>
   </block>
   <block type="else"> 
      <expression variable="access port">
        <operator name="append">
           <value>:</value>
           <value variable="access port"/>
        </operator>
      </expression>
   </block>
 </if>
      

#### block

 <block>
    <function name="touch">
       <parameter value="src/Java/org/omg/timestamp.touch" type="path"/>
    </function>
 </block>
     

#### switch

<switch variable="component">
  <block type="case" value="s">
     <echo>EPM server configuration</echo>
  </block>
  <block type="case" value="c">
     <echo>EPM client configuration</echo>
  </block>
  <block type="case" value="a">
     <echo>EPM All-in-one configuration</echo>
  </block>
  <block type="case" value="r">
     <echo>Scheduler only configuration</echo>
  </block>

</switch>
     

#### function

<function name="warit">
         <parameter value="&build\_directory;/&webapp\_file;"/>
         <parameter>src\\javaarchitect\\servlet\\web.xml</parameter>
         <parameter>C &domain;</parameter>
         <parameter>&build\_directory;/&domain;</parameter>
         <parameter/>
         <parameter>E config/</parameter>
         <parameter>src\\javaarchitect\\servlet\\\*.properties</parameter>
         <parameter>servlets\\.properties|aliases.properties</parameter>
         <parameter>C &domain;/tree/resource/</parameter>
         <parameter>src\\&domain;/tree/resource</parameter>
         <parameter>CVS\*</parameter>
         <parameter>C &domain;/tree/</parameter>
         <parameter>src\\&domain;/tree/tree.dtd</parameter>
         <parameter></parameter>
         <parameter>C &domain;/mishka/resource/</parameter>
         <parameter>src\\&domain;/mishka/resource</parameter>
         <parameter>CVS\*</parameter>
         <parameter>L</parameter>
         <parameter>
           <expression>
             <operator name="append">
		<value variable="DEMROG\_HOME"/>
                <value>/lib/demrog.jar</value>              
             </operator>
           </expression>
         </parameter>
         <parameter/>
         <parameter>A</parameter>
         <parameter>src\\javaarchitect\\servlet\\tree\\resource/\*.htm</parameter>
         <parameter>A tre\_eview-doc.html</parameter>
         <parameter>doc\\treeview-doc.html</parameter>
         <parameter>A</parameter>
         <parameter>doc\\\*.png</parameter>
         <parameter>A</parameter>
         <parameter>src\\snoop.jsp</parameter>
      </function>
     

#### operator

<operator name="array">
  <value>-cmf</value>
  <value variable="manifest file"/>
</operator>
    

#### target

<target name="war" dir="." comment="Build J2EE deployer war.jar">
  <echo>Jarring war...</echo>
  <dependency target="compile j2ee"/>
  <dependency>
    <function name="allnewer">
      <parameter value="&build\_directory;\\rogatkin" type="dir"/>
      <parameter value="&build\_directory;\\war.jar" type="file"/>
    </function>
  </dependency>
  <task name="jar\_do" code="sun.tools.jar.Main">
     <parameter value="-cf"/>
     <parameter value="&build\_directory;/war.jar" type="file"/>
     <parameter value="-C"/>
     <parameter value="&build\_directory;" type="dir"/>
     <parameter value="rogatkin" type="dir"/>
  </task>
</target>    


