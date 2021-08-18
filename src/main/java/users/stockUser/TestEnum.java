package users.stockUser;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public enum TestEnum {
    A(100),
    B(80),
    C(60),
    D(50),
    E(40),
    F(30),
    G(20);

    public Integer frequency;
    private static final List<TestEnum> VALUES =
            Collections.unmodifiableList(Arrays.asList(values()));

    TestEnum(int frequency) {
        this.frequency = frequency;
    }
}
