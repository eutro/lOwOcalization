package eutro.l12n.core.common;

import com.google.common.collect.ImmutableList;
import eutro.l12n.api.ILOwOConfigurableTransformation;
import eutro.l12n.api.ILOwOTransformation;
import eutro.l12n.api.LOwOcalizationAPI;

import java.util.*;
import java.util.function.Function;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Stream;

public class LOwODefaultTransformations {

    public static List<String> DEFAULT_CONFIG = ImmutableList.of(
            "\"l\"->\"w\"",
            "\"L\"->\"W\"",
            "\"r\"->\"w\"",
            "\"R\"->\"W\"",
            "s/(\\w|^)s+(\\W|$)/$1th$2/g",
            "s/(\\w|^)S+(\\W|$)/$1TH$2/g",
            "s/([NM])([AO])/$1Y$2/g",
            "s/([nm])([ao])/$1y$2/ig",
            "30% of the time, s/(^|\\s)(\\w)/$1$2-$2/g"
    );

    public static List<ILOwOConfigurableTransformation> getAll() {
        LinkedList<ILOwOConfigurableTransformation> defaults = new LinkedList<>();
        defaults.add(patternBased(Pattern.compile("([\"'])(?<target>.+)\\1->([\"'])(?<replacement>.*)\\3"), LOwODefaultTransformations::makeLiteral));
        defaults.add(patternBased(Pattern.compile("s(.)(?<pattern>.*?[^\\\\])\\1(?<replace>.*)\\1(?<flags>[a-z]*)"), LOwODefaultTransformations::makeRegex));
        defaults.add(patternBased(Pattern.compile("(?<chance>\\d+(\\.\\d+)?)% of the time, (?<pat>.+)"), LOwODefaultTransformations::makeChanced));
        return defaults;
    }

    private static ILOwOConfigurableTransformation patternBased(Pattern pattern, Function<Matcher, Stream<ILOwOTransformation>> mapper) {
        return configuration -> Optional.of(pattern.matcher(configuration))
                .filter(Matcher::matches)
                .stream()
                .flatMap(mapper);
    }

    private static Stream<ILOwOTransformation> makeChanced(Matcher matcher) {
        double chance = Double.parseDouble(matcher.group("chance")) / 100.0;
        String pat = matcher.group("pat");
        return LOwOcalizationAPI.lookupTransformations(pat).map(it -> it.withChance(chance));
    }

    private static Stream<ILOwOTransformation> makeLiteral(Matcher matcher) {
        String from = matcher.group("target");
        String to = matcher.group("replacement");
        return Stream.of(new RegexTransformation(
                Pattern.compile(Pattern.quote(from)),
                Matcher.quoteReplacement(to),
                true,
                1.0
        ));
    }

    private static Stream<ILOwOTransformation> makeRegex(Matcher matcher) {
        String pattern = matcher.group("pattern");
        String replace = matcher.group("replace");
        String flags = matcher.group("flags");
        boolean global = flags.contains("g");
        if (!flags.isEmpty()) {
            if (global) flags = flags.replace("g", "");
            pattern = String.format("(?%s)%s", flags, pattern);
        }
        return Stream.of(new RegexTransformation(
                Pattern.compile(pattern),
                replace,
                global,
                1.0
        ));
    }

    private record RegexTransformation(
            Pattern pattern,
            String replacement,
            boolean global,
            double chance
    ) implements ILOwOTransformation {
        @Override
        public String transform(String source) {
            Random rand = chance < 1.0 ? new Random(source.hashCode() ^ System.identityHashCode(this)) : null;
            Matcher m = pattern.matcher(source);
            StringBuilder sb = new StringBuilder();
            if (m.find()) {
                do {
                    boolean doReplace = rand == null || rand.nextDouble() <= chance;
                    m.appendReplacement(sb, doReplace ? replacement : "$0");
                    if (!global) break;
                } while (m.find());
                m.appendTail(sb);
                return sb.toString();
            } else {
                return source;
            }
        }

        @Override
        public ILOwOTransformation withChance(double chance) {
            return new RegexTransformation(pattern, replacement, global, chance * this.chance);
        }
    }

}
