JParameter
==========

Basic Java parameter parsing.  Can parse command line arguments or run interactively to parse common Java data types such as int, float, string, and enum. 
Aliases can be defined for parameters as well as help messages. 
Parameters can be marked as required (default to optional) and help and input messages can be added which JParameter uses when running in interactive mode. 
Standard mode parses parameters from the program's arguments (e.g. main(String[] args)). 
Interactive mode asks the user for each required parameter in order and can be combined with standard mode to ask the user for parameters missing from the program's arguments. 
