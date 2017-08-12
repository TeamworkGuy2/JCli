# Change Log
All notable changes to this project will be documented in this file.
This project does its best to adhere to [Semantic Versioning](http://semver.org/).


--------
### [0.2.0](N/A) - 2017-08-12
#### Changed
* Renamed project from JParameter -> JCli
* Renamed `programParameter` package -> `twg2.cli`


--------
### [0.1.2](https://github.com/TeamworkGuy2/JParameter/commit/a7305eedc7092fc40c5216346db34119e26edaa0) - 2016-10-21
#### Changed
* Added unit test for non-interactive parameter parsing

#### Fixed
* Fixed an issue with parsing enums


--------
### [0.1.1](https://github.com/TeamworkGuy2/JParameter/commit/393f65924b375af3949767606e90ca1327e97376) - 2016-08-21
#### Changed
* Added `jcollection-builders` dependency
* Updated `jcollection-util` dependency to latest 0.7.0 version


--------
### [0.1.0](https://github.com/TeamworkGuy2/JParameter/commit/63df5669659db8195e2a6261d492fc2d234a3718) - 2016-06-26
#### Added
* Versioning of existing code. Basic parameter parsing for command line arguments or interactive user input. Includes support for int, float, string, Path, and enum.
* Updated to latest version of `JCollectionFiller` library, (MapBuilder newMutableEnumNames() renamed mutableEnumNames())
