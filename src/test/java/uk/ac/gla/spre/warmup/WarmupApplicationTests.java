package uk.ac.gla.spre.warmup;

import net.jqwik.api.ForAll;
import net.jqwik.api.Property;
import net.jqwik.api.constraints.Size;
import org.junit.jupiter.api.Test;
import org.springframework.boot.actuate.autoconfigure.security.servlet.ManagementWebSecurityAutoConfiguration;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.security.servlet.SecurityAutoConfiguration;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import net.jqwik.api.*;
import net.jqwik.api.constraints.*;
import net.jqwik.api.lifecycle.*;
import net.jqwik.spring.*;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.*;
import org.springframework.test.context.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;


@JqwikSpringSupport
@SpringBootTest(classes = {WarmupApplication.class}, webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@EnableAutoConfiguration(exclude= {SecurityAutoConfiguration.class, ManagementWebSecurityAutoConfiguration.class})
@ActiveProfiles("test")
@ContextConfiguration(classes = WarmupController.class)
class WarmupApplicationTests {
	@Autowired
	private WarmupController w;

	@Test
	void contextLoads() {
	}

	@Property(tries = 25)
	void divideBySelf(@ForAll int value) {
		if (value != 0) {
			int result = value / value;
			assertEquals(result, 1);
		}
	}


	@Property(tries = 25)
	void nameIsAddedToHello(@ForAll @AlphaChars @StringLength(min = 1, max = 10) String name) {
		Map<String, String> request = new HashMap();
		request.put("q", name);

		String greeting = w.helloGet(request);
		assert(greeting.contains("Alistair"));
	}

	@Property(tries = 25)
	void multiplication(@ForAll @Positive Integer a, @ForAll @Positive Integer b) {
		String question = "abc: what is " + String.valueOf(a) + " multiplied by " + String.valueOf(b);

		Map<String, String> request = new HashMap();
		request.put("q", question);

		String result = w.helloGet(request);
		String want = String.valueOf(a*b);
		assertEquals(want, result);
	}

	@Property(tries = 25)
	void sumSolver(@ForAll @Size(min = 1, max = 100) List<Integer> values){
		String result = w.addition(values); // substitue for your sum method
		String expected = String.valueOf(values.stream().mapToInt(Integer::intValue).sum());
		assertEquals(result, expected);
	}


}
