package uk.ac.gla.spre.warmup;

import org.springframework.web.bind.annotation.*;
import java.util.Map;

@RestController
public class WarmupController {
		@GetMapping("/")
		public String hello(@RequestParam Map<String,String> allParams) {
				System.out.println("Request received! "+allParams.entrySet());
				return "Hello World!";
		}
}
