package uk.ac.gla.spre.warmup;

import org.junit.jupiter.api.Test;
import org.springframework.boot.actuate.autoconfigure.security.servlet.ManagementWebSecurityAutoConfiguration;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.security.servlet.SecurityAutoConfiguration;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.*;
import org.springframework.test.context.*;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

//Reference imports
import net.jqwik.spring.JqwikSpringSupport;
import net.jqwik.api.Property;
import net.jqwik.api.ForAll;
import net.jqwik.api.constraints.Size;
import net.jqwik.api.constraints.Positive;
import net.jqwik.api.constraints.AlphaChars;
import net.jqwik.api.constraints.StringLength;
import net.jqwik.api.constraints.IntRange;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;


@SpringBootTest(classes = {WarmupApplication.class}, webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@EnableAutoConfiguration(exclude= {SecurityAutoConfiguration.class, ManagementWebSecurityAutoConfiguration.class})
@ActiveProfiles("test")
@JqwikSpringSupport
@ContextConfiguration(classes = WarmupController.class)
class WarmupApplicationTests {
	@Autowired
	private WarmupController w;

	@Test
	void contextLoads() {
	}

	//Either need @Positive (as shown here) or for the solver to handle negative numbers, applies to all with number
	@Property(tries = 25)
	void propTestSum(@ForAll @Size(min = 1, max = 5) List<@Positive Integer> values){
		String result = w.addition(values);
		String expected = String.valueOf(values.stream().mapToInt(Integer::intValue).sum());
		assertEquals(result, expected);
	}

	//Alternative property for if the student had not implemented sum, generates a random string and checks that it is handled
	@Property(tries = 25)
	void propGreetOnFail(@ForAll @AlphaChars @StringLength(min = 1, max = 10) String name) {
		Map<String, String> request = new HashMap();
		request.put("q", name);

		String greeting = w.helloGet(request);
		assert(greeting.contains("Alistair"));
	}

	@Property(tries = 25)
	void propTestMultiplication(@ForAll @Positive Integer a, @ForAll @Positive Integer b) {
		String question = "abc: what is " + String.valueOf(a) + " multiplied by " + String.valueOf(b);

		Map<String, String> request = new HashMap();
		request.put("q", question);

		String result = w.helloGet(request);
		String want = String.valueOf(a*b);
		assertEquals(want, result);
	}

	@Property(tries = 25)
	void propTestMinus(@ForAll @Positive Integer a, @ForAll @Positive Integer b) {
		String question = "abc: what is " + String.valueOf(a) + " minus " + String.valueOf(b);

		Map<String, String> request = new HashMap();
		request.put("q", question);

		String result = w.helloGet(request);
		String want = String.valueOf(a-b);
		assertEquals(want, result);
	}

	@Property(tries = 25)
	void propTestPower(@ForAll @IntRange(max = 100) Integer a, @ForAll @IntRange(max = 100) Integer b) {
		String question = "abc: what is " + String.valueOf(a) + " to the power of " + String.valueOf(b);

		Map<String, String> request = new HashMap();
		request.put("q", question);

		String result = w.helloGet(request);
		String want = String.valueOf((int)Math.floor(Math.pow(a,b)));
		assertEquals(want, result);
	}

	@Property(tries = 25)
	void propTestLargest(@ForAll @Size(min = 1, max = 10) List<@Positive Integer> values) {
		String question = "zed: which of the following numbers is the largest:";
		String nums = String.join(", ", values.toString());

		Map<String, String> request = new HashMap();
		request.put("q", question+nums);

		String result = w.helloGet(request);
		String want = String.valueOf(Collections.max(values));
		assertEquals(want, result);
	}
}
