JCli
==========
version: 0.2.0

Basic Java parameter parsing.  Can parse command line arguments or run interactively to parse common Java data types such as int, float, string, Path, and enum. 
Aliases can be defined for parameters as well as help messages. 
Parameters can be marked as required (defaults to optional) and help and input messages can be added which JParameter uses when running in interactive mode.
Validators and validator error message generators can also be provided for parameters which are only valid within a certain subset of their type.
For example, a validator could be added to an integer parameter to ensure only positive values are parsed and error messages are generated for negative values.

Standard mode can be used to parse program's arguments (e.g. from `main(String[] args)`). 

Interactive mode asks the user for each required parameter in order and can be combined with standard mode to ask the user for parameters missing from the program's arguments. 

Example:
--------
```java
public class UserParameters {
  private Path searchPath;
  
  public void setSearchPath(Path searchPath) {
    this.searchPath = searchPath;
  }
}
//...

public static void main(String[] args) {
  UserParameters params = new UserParameters();
  
  ParameterData<String, Path> searchPathParam = ParameterBuilder.newPath()
    .setNameAndAliases("-searchPath", "-search", "-sp")
    .setSetter(params::setSearchPath)  // a setter method that accepts a Path object
    .setHelpMessage("the task search path")
    .setValidator((path) -> path.toFile().getName().indexOf('&') == -1)
    .setValidatorMessageGenerator((path) -> "paths containing '&', such as '" + path + "' are not allowed")
    .setRequestParameterMessage("please enter the path to search: ")
    .setRequired(true)
    .build();

  ParameterSet<String> paramSet = ParameterSet.newParameterSet(Arrays.asList(searchPathParam), true, "-help");

  paramSet.parseInteractive(args, new BufferedReader(new InputStreamReader(System.in)), System.out, "help");
  
  System.out.println("params object: searchPath='" + params.searchPath + "'");
}
```
The above example defines one parameter with the name '-searchPath' that can also be identified via the aliases '-search' and '-sp', this parameter has a help message and request parameter message which are printed to the command line when parsing interactively, as well as a validator which prevents paths with '&' in them from being parsed and a validator message generator which is called if the validator fails and returns an informative error message. 
Note: many of these arguments are optional, such as set required, validator, and validator message generator, see ParameterData and ParameterBuilder documentation for required arguments. 
A parameter set is then created which writes to System.out and prints help information when a parameter named '-help' is parsed.
