package twg2.cli.test;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;
import java.util.function.Function;

import org.junit.Assert;
import org.junit.Test;

import twg2.cli.ParameterBuilder;
import twg2.cli.ParameterData;
import twg2.cli.ParameterParser;
import twg2.cli.ParameterSet;

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


	@Test
	public void parameterParsingTest() {
		String[] argAry = ParameterParser.parseParameters(
				"-name \"args test 1\" -searchPath \"E:/stuff/project\" -loops 3 -timeUnit SECONDS -regex -recentPaths \"Java\\projects\\IoUtility\" E:\\stuff\\example"
		).toArray(new String[0]);
		Params params = programArgsTest(argAry, true);

		Assert.assertEquals("args test 1", params.taskName);
		Assert.assertEquals(Paths.get("E:/stuff/project"), params.searchPath);
		Assert.assertEquals(3, params.loops);
		Assert.assertEquals(TimeUnit.SECONDS, params.timeUnit);
		Assert.assertEquals(true, params.regexSearch);
		Assert.assertEquals(Arrays.asList(Paths.get("Java\\projects\\IoUtility"), Paths.get("E:\\stuff\\example")), params.recentSaved);
	}


	public static Params programArgsTest(String[] args, boolean interactive) {
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

		if(interactive) {
			paramSet.parseInteractive(args, 0, new BufferedReader(new InputStreamReader(System.in)), System.out, "help");
		}
		else {
			paramSet.parse(args, 0, System.out);
		}

		return params;
	}


	private static final void testParameterParser() {
		Consumer<String> print = (s) -> {
			System.out.println(s);
		};
		String param1 = "\"quoted param one\" second_param third forth's fifth\"with quote\" \"next quote\"with_extra";
		String param2 = "'quoted parameter one' second_param third forth\"s fifth'with quote' 'next quote'with_extra";
		ParameterParser.parseParameters(param1, '"').forEach(print);
		System.out.println("\n----");
		ParameterParser.parseParameters(param2, '\'').forEach(print);

		Function<Integer, Function<Integer, Integer>> g = (x) -> (y) -> (x + y);
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

		Params params = programArgsTest(argAry, true);
		System.out.println("params obj: " + params);

		testParameterParser();
	}

}
