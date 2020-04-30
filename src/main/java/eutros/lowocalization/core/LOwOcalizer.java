package eutros.lowocalization.core;

import eutros.lowocalization.api.LOwOcalizationEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

import javax.annotation.RegEx;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.function.Function;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class LOwOcalizer {

    public static List<Function<String, String>> mappers = new ArrayList<>();
    private static double stutter = 0.2;

    static {
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
                                                  "$1$2-" + (isUpper(matcher.group(0)) ?
                                                             "$2" :
                                                             matcher.group(2).toLowerCase()));
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

    private static Function<String, String> regex(@RegEx String pattern, int flags, String result) {
        Pattern compile = Pattern.compile(pattern, flags);
        return s -> compile.matcher(s).replaceAll(result);
    }

    public static final LOwOcalizer INSTANCE = new LOwOcalizer();

    private LOwOcalizer() {
    }

    @SubscribeEvent
    public void onLOwOcalizationEvent(LOwOcalizationEvent evt) {
        stutter = LOwOConfig.stutter.getDouble(0.3);
        String s = evt.getCurrent();
        for(Function<String, String> mapper : mappers) {
            s = mapper.apply(s);
        }
        evt.setCurrent(s);
    }

}
