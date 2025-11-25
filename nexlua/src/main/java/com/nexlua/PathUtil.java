package com.nexlua;

import java.io.File;

public class PathUtil {
    // Path Utils
    public static String getParentPath(String path) {
        if (path == null || path.isEmpty()) {
            return "/";
        }
        File file = new File(path);
        String parent = file.getParent();
        return (parent == null || parent.isEmpty()) ? "/" : parent;
    }

    public static boolean isAbsolutePath(String path) {
        if (path == null || path.isEmpty()) {
            return false;
        }
        File file = new File(path);
        return file.isAbsolute();
    }

    public static boolean isExists(String path) {
        if (path == null || path.isEmpty()) {
            return false;
        }
        File file = new File(path);
        return file.exists();
    }
}
