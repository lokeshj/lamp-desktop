package me.lokesh.lamp.service.search;

import java.nio.file.DirectoryStream;
import java.nio.file.FileSystems;
import java.nio.file.Path;
import java.nio.file.PathMatcher;

/**
 * Created by lokesh.
 */
public class FilterBuilder {
    private FilterBuilder() {}

    public static DirectoryStream.Filter<Path> buildGlobFilter(String pattern) {
        return getPathMatcher("glob:"+pattern)::matches;
    }

    public static DirectoryStream.Filter<Path> buildRegexFilter(String pattern) {
        return getPathMatcher("regex:"+pattern)::matches;
    }


    private static PathMatcher getPathMatcher(String pattern) {
        return FileSystems.getDefault().getPathMatcher(pattern);
    }
}
