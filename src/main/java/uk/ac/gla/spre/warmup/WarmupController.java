package uk.ac.gla.spre.warmup;

import org.springframework.web.bind.annotation.*;
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
    private static Logger logger = LoggerFactory.getLogger(WarmupController.class);

    private List<Integer> getNum(String input) {
	int startOfQuestion = input.indexOf(':');
        String question = input.substring((startOfQuestion != -1 ? startOfQuestion + 1 : 0));

        Pattern pattern = Pattern.compile("(\\d+)");
        Matcher matcher = pattern.matcher(question);
        List<Integer> numbersList = new ArrayList<>();

        while (matcher.find()) {
            numbersList.add(Integer.parseInt(matcher.group(1)));
            logger.debug("Added" + matcher.group(1));
        }

        return numbersList;
    }

    @PostMapping("/")
    public String helloPost(@RequestParam Map<String, String> allParams) {
        logger.debug("Post: " + allParams.entrySet());
        String question = "";
        for (Map.Entry<String, String> entry : allParams.entrySet()) {
            question = entry.getValue();
        }

        List<Integer> numbers = getNum(question);

        return solve(question, numbers);
    }

    @GetMapping("/")
    public String hello(@RequestParam Map<String, String> allParams) {
        logger.info("GET: " + allParams.entrySet());
        String question = "";
        for (Map.Entry<String, String> entry : allParams.entrySet()) {
            question = entry.getValue();
        }

        List<Integer> numbers = getNum(question);

        return solve(question, numbers);
    }

    //Solvers
    public String solve(String question, List<Integer> numbers) {
        if (!question.isEmpty()) {
            if (question.matches(".*?: what is \\d+ multiplied by \\d+"))
                return multiplication(numbers);
            if (question.matches(".*?: what is \\d+ plus \\d+"))
                return addition(numbers);
            if (question.matches(".*?: which of the following numbers is both a square and a cube.*?"))
                return squareAndCube(numbers);
            if (question.matches(".*?: which of the following numbers is the largest.*?"))
                return largest(numbers);
        }

        return "Alistair";
    }

    public String multiplication(List<Integer> numbers) {
        int total = numbers.get(0);
        for (int num : numbers.subList(1, numbers.size()))
            total *= num;
        logger.info("Answer was: " + Integer.toString(total));
        return Integer.toString(total);
    }

    public String addition(List<Integer> numbers) {
        int sum = numbers.get(0);
        for (int num : numbers.subList(1, numbers.size()))
            sum += num;
        logger.info("Answer was: " + Integer.toString(sum));
        return Integer.toString(sum);
    }

    public String squareAndCube(List<Integer> numbers) {
        for (int num : numbers) {
            if (Math.pow((int) Math.sqrt(num), 2) == num && Math.pow((int) Math.cbrt(num), 3) == num) {
                logger.info("Answer was: " + Integer.toString(num));
                return Integer.toString(num);
            }
        }
        return "";
    }

    public String largest(List<Integer> numbers) {
        String max = Integer.toString(Collections.max(numbers));
        logger.info("Answer was: " + max);
        return max;
    }
}
