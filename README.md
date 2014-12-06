JParameter
==========

Basic Java parameter parsing.  Can parse command line arguments or run interactively to parse common Java data types such as int, float, string, and enum. 
Aliases can be defined for parameters as well as help messages. 
Parameters can be marked as required (default to optional) and help and input messages can be added which JParameter uses when running in interactive mode. 

Standard mode parses parameters from the program's arguments (e.g. main(String[] args)). 

Interactive mode asks the user for each required parameter in order and can be combined with standard mode to ask the user for parameters missing from the program's arguments. 

Example:
--------

    public static void main(String[] args) {
      ParameterMetaData<String, Path> searchPathParam = ParameterBuilder.<String>newPath(Path.class)
          .setNameAndAliases("-searchPath", "-search", "-sp")
          .setSetter(params::setSearchPath)  // a setter method that accepts a Path
          .setHelpMessage("the task search path")
          .setRequestParameterMessage("please enter the search path to search: ")
          .build();

      ParameterSet<String> paramSet = ParameterSet.newParameterSet(Arrays.asList(searchPathParam), true, "-help");

      paramSet.parseInteractive(args, 0, new BufferedReader(new InputStreamReader(System.in)), System.out, "help");
	}

The above example defines one parameter with the name '-searchPath' that can also be identified via the aliases '-search' and -'sp', this parameter has a help message and request parameter message which are printed to the command line when parsing interactively.
A parameter set is then created which writes to System.out and prints help information when a parameter named '-help' is parsed.
