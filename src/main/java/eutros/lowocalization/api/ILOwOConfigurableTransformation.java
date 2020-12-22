package eutros.lowocalization.api;

import java.util.Optional;

@FunctionalInterface
public interface ILOwOConfigurableTransformation {

    Optional<ILOwOTransformation> getTransformation(String configuration);

}
