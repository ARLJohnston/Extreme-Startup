package uk.ac.gla.spre.warmup;

import org.springframework.web.bind.annotation.*;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.client.RestClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;
import java.util.ArrayList;
import java.util.List;
import java.util.Collections;
import java.util.regex.Matcher;
import java.util.regex.Pattern;


@RestController
public class WarmupController {
    @Value("${spre.delegate.keyword}")
    private String delegationKeyword;

    @Value("${spre.delegate.port}")
    private String delegationPort;

    @Value("${spring.cloud.client.hostname}")
    private String hostname;

    private static Logger logger = LoggerFactory.getLogger(WarmupController.class);

    private static final String alreadyDelegated = "alreadyDelegated";

    private List<Integer> getNum(String input) {
        List<Integer> numbersList = new ArrayList<>();
	if (input != null) {
	    int startOfQuestion = input.indexOf(':');
	    String question = input.substring((startOfQuestion != -1 ? startOfQuestion + 1 : 0));

	    Pattern pattern = Pattern.compile("(\\d+)");
	    Matcher matcher = pattern.matcher(question);

	    while (matcher.find()) {
		numbersList.add(Integer.parseInt(matcher.group(1)));
		logger.debug("Added" + matcher.group(1));
	    }

	}
        return numbersList;
    }

    @PostMapping("/")
    public String helloPost(@RequestParam Map<String, String> allParams) {
        logger.debug("Post: " + allParams.entrySet());
        String question = allParams.get("q");
        List<Integer> numbers = getNum(question);

        return solve(question, numbers);
    }

    @GetMapping("/")
    public String helloGet(@RequestParam Map<String, String> allParams) {
        logger.info("GET: " + allParams.entrySet());
        String question = allParams.get("q");
        if (question != null
            && question.contains(delegationKeyword)
            && !allParams.containsKey(alreadyDelegated)) {
            logger.debug("Delegating " +question+ " to " +"http://" +hostname+
                         ":" +delegationPort);
            try {
                return delegatedGet("http://"+hostname+":"+delegationPort +
                                "/?q="+question+"&"+alreadyDelegated+"=true");
            } catch (Exception e) {
		logger.error("Delegated service error - " + e);
                return "Delegated service was offline";
            }
        }
        List<Integer> numbers = getNum(question);
	logger.debug("Question:" + question);

        return solve(question, numbers);
    }

    private String delegatedGet(String uri) {
        RestClient restClient = RestClient.create();
        String result = restClient.get()
            .uri(uri)
            .retrieve()
            .body(String.class);
        return result;
    }

    //Solvers
    public String solve(String question, List<Integer> numbers) {
        if (question != null) {
            if (question.matches(".*?: what is \\d+ multiplied by \\d+"))
                return multiplication(numbers);
            if (question.matches(".*?: what is \\d+ plus \\d+"))
                return addition(numbers);
            if (question.matches(".*?: which of the following numbers is both a square and a cube.*?"))
                return squareAndCube(numbers);
            if (question.matches(".*?: which of the following numbers is the largest.*?"))
                return largest(numbers);
            if (question.matches(".*?: which of the following numbers are primes.*?"))
                return prime(numbers);
            if (question.matches(".*?: who played James Bond in the film Dr No.*?"))
                return bond();
            if (question.matches(".*?: what is the \\d+.*? in the Fibonnaci sequence"))
                return fibonnaci(numbers);
            if (question.matches(".*?: what is \\d+ minus \\d+.*?"))
                return subtract(numbers);
            if (question.matches(".*?: what colour is a banana.*?"))
                return banana();
            if (question.matches(".*?: which year was Theresa May first elected as the Prime Minister of Great Britain.*?"))
                return may();
            if (question.matches(".*?: which city is the Eiffel tower in.*?"))
                return paris();
            if (question.matches(".*?: what is \\d+ to the power of \\d+.*?"))
                return power(numbers);
        }

        return "Alistair";
    }

    public String multiplication(List<Integer> numbers) {
        int total = numbers.get(0);
        for (int num : numbers.subList(1, numbers.size()))
            total *= num;
        logger.info("Multiplication answer was: " + Integer.toString(total));
        return Integer.toString(total);
    }

    public String addition(List<Integer> numbers) {
        int sum = numbers.get(0);
        for (int num : numbers.subList(1, numbers.size()))
            sum += num;
        logger.info("Addition answer was: " + Integer.toString(sum));
        return Integer.toString(sum);
    }

    public String squareAndCube(List<Integer> numbers) {
        for (int num : numbers) {
            if (Math.pow((int) Math.sqrt(num), 2) == num && Math.pow((int) Math.cbrt(num), 3) == num) {
                logger.info("Square and Cube answer was: " + Integer.toString(num));
                return Integer.toString(num);
            }
        }
        return "";
    }

    public String largest(List<Integer> numbers) {
        String max = Integer.toString(Collections.max(numbers));
        logger.info("Largest answer was: " + max);
        return max;
    }

    public String prime(List<Integer> numbers) {
        logger.info("Calculating Primes");

        for (int num : numbers) {
            boolean prime = true;
            for (int i = 2; i*i < num; i++){
                if (num % i == 0)
                    prime = false;
            }
            if (prime) {
                logger.info("Prime answer was: " + num);
                return Integer.toString(num);
            }
        }

        logger.info("No prime answer");
        return "No number was prime";
    }

    public String bond() {
        logger.info("James Bond");
        return "Sean Connery";
    }

    public String fibonnaci(List<Integer> numbers) {
        int num = numbers.get(0);
        int n1 = 1;
        int n2 = 1;
        int val = 0;

        for (int i = 2; i < num; i++) {
            val = n1;
            int n3 = n1 + n2;
            n1 = n2;
            n2 = n3;
        }

        logger.info("Fibonnaci answer was " + Integer.toString(val));
        return Integer.toString(val);
    }

    public String subtract(List<Integer> numbers) {
        int num = numbers.get(0);
        int num2 = numbers.get(1);

        logger.info("Subtraction answer was " + Integer.toString(num-num2));
        return Integer.toString(num-num2);
    }

    public String banana() {
        logger.info("Banana");
        return "Yellow";
    }

    public String may() {
        logger.info("Theresa");
        return "2016";
    }

    public String paris() {
        logger.info("Rataouille");
        return "Paris";
    }

    public String power(List<Integer> numbers) {
        int num = numbers.get(0);
        int num2 = numbers.get(1);
        int result = (int) Math.pow(num,num2);
        logger.info("Power answer was " + Integer.toString(result));
        return Integer.toString(result);
    }
}
