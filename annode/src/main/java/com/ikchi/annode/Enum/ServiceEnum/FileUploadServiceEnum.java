package com.ikchi.annode.Enum.Service;

import java.util.List;


public enum FileUploadServiceEnum {

    INSTANCE;

    private static final String DATE_TIME_FORMAT_PATTERN = "yyyyMMddHHmmssSSS";

    private static final List<String> ALLOWED_FILE_EXTENSIONS = List.of(
        ".jpg", ".jpeg", ".png", ".gif", ".svg", ".webp", ".ico"
    );

    public static String getDateTimeFormatPattern() {
        return DATE_TIME_FORMAT_PATTERN;
    }

    public static List<String> getAllowedFileExtensions() {
        return ALLOWED_FILE_EXTENSIONS;
    }

}
