package com.nexlua;

import android.content.Context;
import android.content.res.AssetManager;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

public class AssetExtractor {
    public interface ExtractCallback {
        void onStart();
        void onSuccess();
        void onError(IOException e);
    }

    public static void extractAssets(final Context context, final ExtractCallback callback) {
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    if (callback != null) {
                        callback.onStart();
                    }
                    List<Pattern> onlyPatterns = compilePatterns(LuaConfig.ONLY_DECOMPRESS);
                    List<Pattern> skipPatterns = compilePatterns(LuaConfig.SKIP_DECOMPRESS);
                    AssetManager assetManager = context.getAssets();
                    List<String> assetFiles = listAssetFiles("", assetManager);
                    File filesDir = context.getFilesDir();
                    for (String assetPath : assetFiles) {
                        if (shouldProcess(assetPath, onlyPatterns, skipPatterns)) {
                            File destFile = new File(filesDir, assetPath);

                            File parentDir = destFile.getParentFile();
                            if (parentDir != null && !parentDir.exists()) {
                                if (!parentDir.mkdirs()) {
                                    throw new IOException("Failed to create directory: " + parentDir.getAbsolutePath());
                                }
                            }

                            copyAsset(assetManager, assetPath, destFile);
                        }
                    }
                    if (callback != null) {
                        callback.onSuccess();
                    }
                } catch (IOException e) {
                    if (callback != null) callback.onError(e);
                }
            }
        }).start();
    }

    public static void extractAssets(final Context context) {
        extractAssets(context, null);
    }

    private static boolean shouldProcess(String assetPath, List<Pattern> onlyPatterns, List<Pattern> skipPatterns) {
        if (!onlyPatterns.isEmpty()) {
            boolean matchesOnly = false;
            for (Pattern pattern : onlyPatterns) {
                if (pattern.matcher(assetPath).matches()) {
                    matchesOnly = true;
                    break;
                }
            }
            return matchesOnly;
        }

        if (!skipPatterns.isEmpty()) {
            for (Pattern pattern : skipPatterns) {
                if (pattern.matcher(assetPath).matches()) {
                    return false;
                }
            }
        }
        return true;
    }

    private static List<String> listAssetFiles(String path, AssetManager assetManager) throws IOException {
        List<String> files = new ArrayList<>();
        String[] assets = assetManager.list(path);

        if (assets == null || assets.length == 0) {
            if (!path.isEmpty()) {
                files.add(path);
            }
        } else {
            for (String asset : assets) {
                String currentPath = path.isEmpty() ? asset : path + "/" + asset;
                files.addAll(listAssetFiles(currentPath, assetManager));
            }
        }
        return files;
    }

    private static void copyAsset(AssetManager assetManager, String assetPath, File destFile) throws IOException {
        InputStream in = null;
        OutputStream out = null;
        try {
            in = assetManager.open(assetPath);
            out = new FileOutputStream(destFile);
            byte[] buffer = new byte[8192];
            int read;
            while ((read = in.read(buffer)) != -1) {
                out.write(buffer, 0, read);
            }
        } finally {
            if (in != null) {
                try {
                    in.close();
                } catch (IOException e) {
                    // Ignored
                }
            }
            if (out != null) {
                try {
                    out.close();
                } catch (IOException e) {
                    // Ignored
                }
            }
        }
    }

    private static List<Pattern> compilePatterns(String[] wildcards) {
        List<Pattern> patterns = new ArrayList<>();
        if (wildcards != null) {
            for (String wildcard : wildcards) {
                patterns.add(Pattern.compile(wildcardToRegex(wildcard)));
            }
        }
        return patterns;
    }

    private static String wildcardToRegex(String wildcard) {
        String[] parts = wildcard.split("\\*", -1);
        StringBuilder regex = new StringBuilder();
        for (int i = 0; i < parts.length; i++) {
            regex.append(Pattern.quote(parts[i]));
            if (i < parts.length - 1) {
                regex.append(".*");
            }
        }
        return regex.toString();
    }
}
