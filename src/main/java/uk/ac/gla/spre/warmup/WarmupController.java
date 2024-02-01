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

    @GetMapping("/")
    public String hello(@RequestParam Map<String, String> allParams) {
        logger.info("GET: " + allParams.entrySet());

        String question = "";
        List<Integer> numbersList = new ArrayList<>();

        for (Map.Entry<String, String> entry : allParams.entrySet()) {
            question = entry.getValue();
        }

        if (!question.isEmpty()) {
            int startOfQuestion = question.indexOf(':');

            String subStringAfterColon = question.substring((startOfQuestion != -1 ? startOfQuestion + 1 : 0));

            Pattern digits = Pattern.compile("\\d+");
            Matcher matcher = digits.matcher(subStringAfterColon);

            while (matcher.find()) {
                String number = matcher.group();
                numbersList.add(Integer.parseInt(number));
            }

            startOfQuestion = subStringAfterColon.indexOf(':');
            String remainingText = subStringAfterColon.substring(0, (startOfQuestion != -1 ? startOfQuestion : subStringAfterColon.length())).replaceAll("\\d+", "");

            switch (remainingText) {
                case " which of the following numbers is the largest":
                    logger.info("Answer was: " + Integer.toString(Collections.max(numbersList)));
                    return Integer.toString(Collections.max(numbersList));

                case " what is	plus ":
                    int sum = numbersList.get(0);
                    for (int num : numbersList.subList(1, numbersList.size()))
                        sum += num;
                    logger.info("Answer was: " + Integer.toString(sum));
                    return Integer.toString(sum);

                case " which of the following numbers is both a square and a cube":
                    for (int num : numbersList) {
                        if (Math.pow((int) Math.sqrt(num), 2) == num && Math.pow((int) Math.cbrt(num), 3) == num) {
                            logger.info("Answer was: " + Integer.toString(num));
                            return Integer.toString(num);
                        }
                    }
                    return "";
                case " what is	multiplied by ":
                    int total = numbersList.get(0);
                    for (int num : numbersList.subList(1, numbersList.size()))
                        total *= num;
                    logger.info("Answer was: " + Integer.toString(total));
                    return Integer.toString(total);
            }

        }
        return "Alistair";
    }
}
