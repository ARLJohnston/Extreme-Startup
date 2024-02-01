package uk.ac.gla.spre.warmup;

import org.springframework.web.bind.annotation.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.util.Map;

@RestController
public class WarmupController {
		private static Logger logger = LoggerFactory.getLogger(WarmupController.class);

		@GetMapping("/")
		public String hello(@RequestParam Map<String,String> allParams) {
				logger.info("GET: " +allParams.entrySet());
				return "Alistair";
		}
}
