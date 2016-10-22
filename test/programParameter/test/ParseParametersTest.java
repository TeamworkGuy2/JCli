package programParameter.test;

import java.util.Arrays;
import java.util.List;

import org.junit.Test;

import programParameter.ParameterParser;
import checks.CheckTask;

/**
 * @author TeamworkGuy2
 * @since 2014-11-16
 */
public class ParseParametersTest {

	@Test
	public void testParseString() {
		List<String> params = Arrays.asList(
				"1 w 3 \"arg 4\" \"or \\\' quote\"",
				"\"a b\" c\"",
				"\"vla\", \"wa\"",
				" abc\"de\"",
				"\"alpha beta\"",
				"\"a=\\\"A\\\"\"",
				"\"\""
		);
		List<List<String>> expect = Arrays.asList(
				Arrays.asList("1", "w", "3", "arg 4", "or \\\' quote"),
				Arrays.asList("a b", "c\""),
				Arrays.asList("\"vla\",", "wa"),
				Arrays.asList("abc\"de\""),
				Arrays.asList("alpha beta"),
				Arrays.asList("a=\"A\""),
				Arrays.asList("")
		);
		CheckTask.assertTests(params, expect, (p) -> ParameterParser.parseParameters(p));
	}

}
