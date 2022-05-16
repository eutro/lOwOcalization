package eutro.l12n.api;

import java.util.Random;

@FunctionalInterface
public interface ILOwOTransformation {

    String transform(String source);

    default ILOwOTransformation withChance(double chance) {
        return src -> new Random().nextDouble() < chance ? this.transform(src) : src;
    }

}
