package programParameter.test;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;
import java.util.function.Function;

import programParameter.ParameterBuilder;
import programParameter.ParameterData;
import programParameter.ParameterParser;
import programParameter.ParameterSet;
import checks.Check;

/**
 * @author TeamworkGuy2
 * @since 2014-11-16
 */
public class ProgramArgsTest {


	public static class Params {
		private int loops;
		private TimeUnit timeUnit;
		private boolean regexSearch;
		private Path searchPath;
		private List<Path> recentSaved;
		private String taskName;

		public int getLoops() {
			return loops;
		}

		public void setLoops(int loops) {
			this.loops = loops;
		}

		public TimeUnit getTimeUnit() {
			return timeUnit;
		}

		public void setTimeUnit(TimeUnit timeUnit) {
			this.timeUnit = timeUnit;
		}

		public boolean isRegexSearch() {
			return regexSearch;
		}

		public void setRegexSearch(boolean regexSearch) {
			this.regexSearch = regexSearch;
		}

		public Path getSearchPath() {
			return searchPath;
		}

		public void setSearchPath(Path searchPath) {
			this.searchPath = searchPath;
		}

		public Path[] getRecentSaved() {
			if(this.recentSaved == null) {
				this.recentSaved = new ArrayList<>();
			}
			return recentSaved.toArray(new Path[recentSaved.size()]);
		}

		public void setRecentSaved(Path[] recentSaved) {
			if(this.recentSaved == null) {
				this.recentSaved = new ArrayList<>();
			}
			this.recentSaved.clear();
			Collections.addAll(this.recentSaved, recentSaved);
		}

		public void addRecentSaved(Path recentSaved) {
			if(this.recentSaved == null) {
				this.recentSaved = new ArrayList<>();
			}
			this.recentSaved.add(recentSaved);
		}

		public String getTaskName() {
			return taskName;
		}

		public void setTaskName(String taskName) {
			this.taskName = taskName;
		}

		@Override
		public String toString() {
			return "task_name=" + taskName + ", time_units=" + timeUnit + ", loops=" + loops + ", search_path=" + searchPath +
					", regex_search=" + regexSearch + ", recent_saved=" + recentSaved;
		}

	}


	public static void programArgsTest(String[] args) {
		Params params = new Params();
		ParameterData<String, Integer> loopParam = ParameterBuilder.newInteger()
				.setNameAndAliases("-loopCount", "-loops", "-loop-count")
				.setSetter((arg) -> params.setLoops(arg))
				.setHelpMessage("how many times to run the task")
				.setRequestParameterMessage("enter task run count: ")
				.setValidator((num) -> num > 0 && num < 11)
				.setValidatorMessageGenerator((num) -> "loop count must be great than 0 and less than 11, was '" + num + "'")
				.setRequired(true)
				.build();

		ParameterData<String, TimeUnit> timeUnitParam = ParameterBuilder.newEnum(TimeUnit.class)
				.setNameAndAliases("-timeUnit")
				.setSetter(params::setTimeUnit)
				.setHelpMessage("the time units of the task")
				.setRequestParameterMessage("please enter task time unit: ")
				.setRequired(true)
				.build();

		ParameterData<String, Path> searchPathParam = ParameterBuilder.newPath()
				.setNameAndAliases("-searchPath")
				.setSetter(params::setSearchPath)
				.setHelpMessage("the task search path")
				.setRequestParameterMessage("please enter the search path to search: ")
				.setValidator((path) -> path.toString().indexOf('&') == -1) // don't allow paths with '&' in them
				.setValidatorMessageGenerator((path) -> "paths containing '&', such as '" + path + "' are not allowed")
				.setRequired(true)
				.build();

		ParameterData<String, Path[]> recentPathsParam = ParameterBuilder.newPathArray()
				.setNameAndAliases("-recentPaths")
				.setSetter(params::setRecentSaved)
				.setHelpMessage("recently used paths")
				.setRequestParameterMessage("enter a list of recent paths: ")
				.build();

		ParameterData<String, String> taskNameParam = ParameterBuilder.newText()
				.setNameAndAliases("-name", "-taskName")
				.setSetter(params::setTaskName)
				.setHelpMessage("the task name")
				.setRequestParameterMessage("enter the task name: ")
				.setRequired(true)
				.build();

		ParameterData<String, Boolean> regexSearchParam = ParameterBuilder.newFlag()
				.setNameAndAliases("-regexSearch", "-useRegex", "-regex")
				.setSetter(params::setRegexSearch)
				.setHelpMessage("flag indicating that the search string is a regex string")
				.setRequestParameterMessage("is this search string a regex string: ")
				.build();

		ParameterSet<String> paramSet = ParameterSet.newParameterSet(Arrays.asList(loopParam, timeUnitParam,
				taskNameParam, searchPathParam, recentPathsParam, regexSearchParam), true, "-help");
		//paramSet.parse(args, 0, System.out);
		paramSet.parseInteractive(args, 0, new BufferedReader(new InputStreamReader(System.in)), System.out, "help");

		System.out.println("params obj: " + params);
	}


	private static final void testParseString() {
		String[] params = new String[] {
				"1 w 3 \"arg 4\" \"or \\\' quote\"",
				"\"a b\" c\"",
				"\"vla\", \"wa\"",
				" abc\"de\"",
				"\"alpha beta\"",
				"\"a=\\\"A\\\"\"",
				"\"\""
		};
		@SuppressWarnings("unchecked")
		List<String>[] expect = new List[] {
				Arrays.asList("1", "w", "3", "arg 4", "or \\\' quote"),
				Arrays.asList("a b", "c\""),
				Arrays.asList("\"vla\",", "wa"),
				Arrays.asList("abc\"de\""),
				Arrays.asList("alpha beta"),
				Arrays.asList("a=\"A\""),
				Arrays.asList("")
		};
		Check.checkTests(params, expect, "", "", (p) -> ParameterParser.parseParameters(p));
	}


	private static final void testParameterParser() {
		Consumer<String> print = (s) -> { System.out.println(s); };
		String param1 = "\"quoted param one\" second_param third forth's fifth\"with quote\" \"next quote\"with_extra";
		String param2 = "'quoted parameter one' second_param third forth\"s fifth'with quote' 'next quote'with_extra";
		ParameterParser.parseParameters(param1, '"').forEach(print);
		System.out.println("\n----");
		ParameterParser.parseParameters(param2, '\'').forEach(print);

		Function<Integer, Function<Integer, Integer>> g = x -> { return (y -> { return x + y; } ); };
		System.out.println(g.apply(3).apply(3));

		System.out.println("Fields of: " + g.getClass());
		Field[] fields = g.getClass().getDeclaredFields();
		for(Field f : fields) {
			System.out.println(f);
		}
		System.out.println("Methods of: " + g.getClass());
		Method[] methods = g.getClass().getDeclaredMethods();
		for(Method m : methods) {
			System.out.println(m);
		}
	}


	public static void main(String[] args) {
		testParseString();
		String[] argAry = ParameterParser.parseParameters(
				"-help -timeUnit SECONDS -regex -recentPaths \"Java\\projects\\IoUtility\" E:\\stuff\\example"
		).toArray(new String[0]);
		programArgsTest(argAry);

		if(Math.round(2) >= 3) {
			testParameterParser();
		}
	}

}
