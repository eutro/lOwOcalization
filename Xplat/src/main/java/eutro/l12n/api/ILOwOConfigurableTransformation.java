package eutro.l12n.api;

import java.util.stream.Stream;

@FunctionalInterface
public interface ILOwOConfigurableTransformation {

    Stream<ILOwOTransformation> getTransformation(String configuration);

}
