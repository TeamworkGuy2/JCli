package programParameter.test;

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
import programParameter.ParameterMetaData;
import programParameter.ParameterParser;
import programParameter.ParameterSet;

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
		ParameterMetaData<String, Integer> loopParam = ParameterBuilder.<String>newInteger(Integer.class)
				.setNameAndAliases("-loopCount", "-loops", "-loop-count")
				.setSetter((arg) -> params.setLoops(arg))
				.setHelpMessage("how many times to run the task")
				.setRequestParameterMessage("enter task run count: ")
				.setRequired(true)
				.build();

		ParameterMetaData<String, TimeUnit> timeUnitParam = ParameterBuilder.<String, TimeUnit>newEnum(TimeUnit.class)
				.setNameAndAliases("-timeUnit")
				.setSetter(params::setTimeUnit)
				.setHelpMessage("the time units of the task")
				.setRequestParameterMessage("please enter task time unit: ")
				.setRequired(true)
				.build();

		ParameterMetaData<String, Path> searchPathParam = ParameterBuilder.<String>newPath(Path.class)
				.setNameAndAliases("-searchPath")
				.setSetter(params::setSearchPath)
				.setHelpMessage("the task search path")
				.setRequestParameterMessage("please enter the search path to search: ")
				.build();

		ParameterMetaData<String, Path[]> recentPathsParam = ParameterBuilder.<String>newPathArray(Path[].class)
				.setNameAndAliases("-recentPaths")
				.setSetter(params::setRecentSaved)
				.setHelpMessage("recently used paths")
				.setRequestParameterMessage("enter a list of recent paths: ")
				.build();

		ParameterMetaData<String, String> taskNameParam = ParameterBuilder.<String>newText(String.class)
				.setNameAndAliases("-name", "-taskName")
				.setSetter(params::setTaskName)
				.setHelpMessage("the task name")
				.setRequestParameterMessage("enter the task name: ")
				.setRequired(true)
				.build();

		ParameterMetaData<String, Boolean> regexSearchParam = ParameterBuilder.<String>newFlag(Boolean.class)
				.setNameAndAliases("-regexSearch", "-useRegex", "-regex")
				.setSetter(params::setRegexSearch)
				.setHelpMessage("flag indicating that the search string is a regex string")
				.setRequestParameterMessage("is this search string a regex string: ")
				.build();

		ParameterSet<String> paramSet = ParameterSet.newParameterSet(Arrays.asList(loopParam, timeUnitParam,
				searchPathParam, recentPathsParam, taskNameParam, regexSearchParam), true, "-help");
		paramSet.parse(args, 0, System.out);
		//paramSet.parseInteractive(args, 0, new BufferedReader(new InputStreamReader(System.in)), System.out, "help");

		System.out.println("params obj: " + params);
	}


	private static final void testParseString() {
		String[] params = new String[] { "arg_one_1 with 3 \"and argument 4\" \"or \\\' quote inside\"" };
		for(String param : params) {
			System.out.println("parameters: " + param  + "\nparsed: " + ParameterParser.parseParameters(param));
		}
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
		String[] argAry = ParameterParser.parseParameters(
				"-help -timeUnit SECONDS -regex -recentPaths \"Java\\projects\\IoUtility\" E:\\stuff\\example"
		).toArray(new String[0]);
		programArgsTest(argAry);
		//testParseString();
	}

}
