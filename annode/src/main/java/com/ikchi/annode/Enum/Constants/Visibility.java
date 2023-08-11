package com.ikchi.annode.Enum.Constants;

import java.util.Arrays;
import java.util.Optional;

public enum Visibility {
    ALL, FOLLOWER, CROSSFOLLOW;

    public static final String NO_SUCH_VISIBILITY_MSG = "존재하지 않는 Pospace Type입니다";

    public static Optional<Visibility> fromString(String value) {
        String upperCase = value.toUpperCase();
        Visibility[] visibilityList = values();

        Optional<Visibility> resultVisibility = Arrays.stream(visibilityList)
            .filter(v -> v.name().equals(upperCase))
            .findFirst();

        return resultVisibility;

    }
}
