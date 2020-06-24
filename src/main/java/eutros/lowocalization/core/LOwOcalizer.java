package eutros.lowocalization.core;

import eutros.lowocalization.api.LOwOcalizationAPI;
import eutros.lowocalization.api.LOwOcalizationEvent;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.intellij.lang.annotations.RegExp;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.function.Function;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;

public class LOwOcalizer {

    public static final Logger LOGGER = LogManager.getLogger();
    private List<Function<String, String>> mappers = new ArrayList<>();
    private static double stutter = 0.2;

    private void initDefaults() {
        if(!LOwOcalizationAPI.REGISTER_DEFAULTS) return;

        mappers.add(s -> {
            StringBuilder builder = new StringBuilder();
            for(int i = 0; i < s.length(); i++) {
                switch(s.charAt(i)) {
                    case 'l':
                    case 'r':
                        builder.append('w');
                        break;
                    case 'L':
                    case 'R':
                        builder.append('W');
                        break;
                    default:
                        builder.append(s.charAt(i));
                }
            }
            return builder.toString();
        });

        mappers.add(regex("(\\w|^)s+(\\W|$)", 0, "$1th$2"));
        mappers.add(regex("(\\w|^)S+(\\W|$)", 0, "$1TH$2"));
        mappers.add(regex("([nm])([ao])", Pattern.CASE_INSENSITIVE, "$1y$2"));

        Pattern pattern = Pattern.compile("( |^)(\\w)");
        mappers.add(s -> {
            Matcher matcher = pattern.matcher(s);
            if(matcher.find()) {
                Random random = new Random(s.hashCode());
                StringBuffer sb = new StringBuffer();
                do {
                    matcher.appendReplacement(sb, random.nextFloat() > stutter ? matcher.group(0) :
                                                  "$1$2-" + (
                                                          isUpper(matcher.group(0)) ?
                                                          "$2" :
                                                          matcher.group(2).toLowerCase()
                                                  ));
                } while(matcher.find());
                matcher.appendTail(sb);
                return sb.toString();
            }
            return s;
        });
    }

    private static boolean isUpper(String s) {
        return s.equals(s.toUpperCase());
    }

    private static Function<String, String> regex(@RegExp String pattern, int flags, String result) {
        Pattern compile = Pattern.compile(pattern, flags);
        return s -> compile.matcher(s).replaceAll(result);
    }

    public static final LOwOcalizer INSTANCE = new LOwOcalizer();

    private LOwOcalizer() {
        initDefaults();
    }

    public static final Pattern REGEX_PATTERN = Pattern.compile("s(.)(?<pattern>.*?[^\\\\])\\1(?<replace>.*)\\1(?<flags>\\w*)");

    public void configChange() {
        mappers.clear();

        if(LOwOConfig.regExes.isEmpty()) {
            initDefaults();
            return;
        }

        for(String string : LOwOConfig.regExes) {
            Matcher regexMatcher = REGEX_PATTERN.matcher(string);
            if(!regexMatcher.matches()) {
                LOGGER.error(String.format("Couldn't parse Regular Expression: %s. Bad format.", string));
                continue;
            }

            String pattern = regexMatcher.group("pattern");
            String replace = regexMatcher.group("replace");
            String flags = regexMatcher.group("flags");
            boolean global = false;
            try {
                if(!flags.isEmpty()) {
                    if(flags.contains("g")) {
                        flags = flags.replace("g", "");
                        global = true;
                    }
                    pattern = String.format("(?%s)%s", flags, pattern);
                }
                mappers.add(
                        ((Function<String, Matcher>) Pattern.compile(pattern)::matcher)
                                .andThen(
                                        global ?
                                        matcher -> matcher.replaceAll(replace) :
                                        matcher -> matcher.replaceFirst(replace)
                                )
                );
            } catch(PatternSyntaxException e) {
                LOGGER.error("Couldn't parse Regular Expression: " + string, e);
            }
        }
    }

    public void onLOwOcalizationEvent(LOwOcalizationEvent evt) {
        String s = evt.getCurrent();
        for(Function<String, String> mapper : mappers) {
            s = mapper.apply(s);
        }
        evt.setCurrent(s);
    }

}
