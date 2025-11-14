package com.nexlua;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.res.AssetFileDescriptor;
import android.content.res.AssetManager;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.math.BigInteger;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Enumeration;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;
import java.util.zip.ZipOutputStream;

@SuppressLint("StaticFieldLeak")
public final class LuaUtil {

    private LuaUtil() {
    }

    private static Context contextx;
    private static AssetManager assetManager;

    public static void init(Context context) {
        LuaUtil.contextx = context.getApplicationContext();
        LuaUtil.assetManager = context.getAssets();
    }

    public static Context getContext() {
        return contextx;
    }

    public static AssetManager getAssetManager() {
        return assetManager;
    }

    private static final String ERR_EXCEEDS_SIZE_LIMIT = "File %s size exceeds 2GB limit";
    private static final String ERR_COULD_NOT_READ_FILE = "Could not completely read file: %s";
    private static final String ERR_FILE_IS_NOT_DIRECTORY = "File %s is not a directory";

    public static ByteBuffer wrap(String input) {
        byte[] bytes = input.getBytes(StandardCharsets.UTF_8);
        ByteBuffer directBuffer = ByteBuffer.allocateDirect(bytes.length);
        directBuffer.put(bytes);
        directBuffer.flip();
        return directBuffer;
    }

    // FileUtil
    public static ByteBuffer readFileBuffer(File file) throws IOException {
        try (FileInputStream fis = new FileInputStream(file);
             FileChannel channel = fis.getChannel()) {
            long size = channel.size();
            if (size > Integer.MAX_VALUE) {
                throw new IOException(String.format(ERR_EXCEEDS_SIZE_LIMIT, file.getPath()));
            }
            ByteBuffer buffer = ByteBuffer.allocateDirect((int) size);
            channel.read(buffer);
            buffer.flip();
            return buffer;
        }
    }

    public static byte[] readFileBytes(File file) throws IOException {
        long fileSize = file.length();
        if (fileSize > Integer.MAX_VALUE) {
            throw new IOException(String.format(ERR_EXCEEDS_SIZE_LIMIT, file.getPath()));
        }
        byte[] buffer = new byte[(int) fileSize];
        try (FileInputStream fis = new FileInputStream(file)) {
            int offset = 0;
            int numRead;
            while (offset < buffer.length && (numRead = fis.read(buffer, offset, buffer.length - offset)) >= 0)
                offset += numRead;
            if (offset != buffer.length) {
                throw new IOException(String.format(ERR_COULD_NOT_READ_FILE, file.getPath()));
            }
        }
        return buffer;
    }

    public static String readFile(File file) throws IOException {
        return new String(readFileBytes(file), StandardCharsets.UTF_8);
    }

    public static void rmDir(File file) {
        if (file == null || !file.exists())
            return;
        if (file.isDirectory()) {
            File[] children = file.listFiles();
            if (children != null) {
                for (File child : children) {
                    rmDir(child);
                }
            }
        }
        file.delete();
    }

    public static void copyFile(File srcFile, File destFile) throws IOException {
        File parentDir = destFile.getParentFile();
        if (parentDir != null && !parentDir.exists()) {
            parentDir.mkdirs();
        }
        try (InputStream in = new FileInputStream(srcFile);
             OutputStream out = new FileOutputStream(destFile)) {
            copyStream(in, out);
        }
    }

    public static void copyDir(File srcDir, File destDir) throws IOException {
        if (!srcDir.isDirectory())
            throw new IOException(String.format(ERR_FILE_IS_NOT_DIRECTORY, srcDir.getPath()));
        if (!destDir.exists())
            destDir.mkdirs();
        File[] children = srcDir.listFiles();
        if (children == null) return;
        for (File child : children) {
            File destChild = new File(destDir, child.getName());
            if (child.isDirectory()) {
                copyDir(child, destChild);
            } else {
                copyFile(child, destChild);
            }
        }
    }

    // Assets Utils
    public static String[] listAssets(Context context, String assetPath) throws IOException {
        return context.getAssets().list(assetPath);
    }

    public static boolean isAssetExists(Context context, String assetPath) {
        try (InputStream ignored = context.getAssets().open(assetPath)) {
            return true;
        } catch (IOException e) {
            return false;
        }
    }

    public static ByteBuffer readAssetBuffer(String assetPath) throws IOException {
        AssetManager assetManager = getAssetManager();
        try (AssetFileDescriptor afd = assetManager.openFd(assetPath);
             FileInputStream fis = afd.createInputStream();
             FileChannel channel = fis.getChannel()) {
            long size = afd.getLength();
            if (size > Integer.MAX_VALUE)
                throw new IOException(String.format(ERR_EXCEEDS_SIZE_LIMIT, assetPath));
            ByteBuffer buffer = ByteBuffer.allocateDirect((int) size);
            channel.read(buffer);
            buffer.flip();
            return buffer;
        }
    }

    public static byte[] readAssetBytes(String assetPath) throws IOException {
        try (InputStream in = getAssetManager().open(assetPath);
             ByteArrayOutputStream out = new ByteArrayOutputStream()) {
            copyStream(in, out);
            return out.toByteArray();
        }
    }

    public static String readAsset(String assetPath) throws IOException {
        return new String(readAssetBytes(assetPath), StandardCharsets.UTF_8);
    }

    public static void copyAssetsDir(String assetPath, File destDir) throws IOException {
        AssetManager assetManager = getAssetManager();
        String[] assets = assetManager.list(assetPath);
        if (!destDir.exists()) {
            destDir.mkdirs();
        }
        for (String asset : assets) {
            String newAssetPath = assetPath.isEmpty() ? asset : assetPath + File.separator + asset;
            File newDestFile = new File(destDir, asset);
            String[] subAssets = assetManager.list(newAssetPath);
            if (subAssets.length == 0) {
                copyAssetsFile(newAssetPath, newDestFile);
            } else {
                copyAssetsDir(newAssetPath, newDestFile);
            }
        }
    }

    public static void copyAssetsFile(String assetPath, File destFile) throws IOException {
        File parentDir = destFile.getParentFile();
        if (parentDir != null && !parentDir.exists()) {
            parentDir.mkdirs();
        }
        try (InputStream in = getAssetManager().open(assetPath);
             OutputStream out = new FileOutputStream(destFile)) {
            copyStream(in, out);
        }
    }

    // Stream Utils
    public static void copyStream(InputStream in, OutputStream out) throws IOException {
        byte[] buffer = new byte[8192];
        int bytesRead;
        while ((bytesRead = in.read(buffer)) != -1) {
            out.write(buffer, 0, bytesRead);
        }
    }

    // Zip Utils
    public static void zip(File srcFile, File zipFile) throws IOException {
        try (FileOutputStream fos = new FileOutputStream(zipFile);
             ZipOutputStream zos = new ZipOutputStream(new BufferedOutputStream(fos))) {
            zip(zos, srcFile, "");
        }
    }

    private static void zip(ZipOutputStream zos, File file, String baseName) throws IOException {
        if (file.isDirectory()) {
            String entryName = baseName + file.getName() + "/";
            zos.putNextEntry(new ZipEntry(entryName));
            File[] children = file.listFiles();
            if (children != null) {
                for (File child : children) {
                    zip(zos, child, entryName);
                }
            }
        } else {
            zos.putNextEntry(new ZipEntry(baseName + file.getName()));
            try (FileInputStream fis = new FileInputStream(file)) {
                copyStream(fis, zos);
            }
        }
    }

    public static void unzip(File zipFile, File destDir) throws IOException {
        if (!destDir.exists()) {
            destDir.mkdirs();
        }
        String destDirPath = destDir.getCanonicalPath();
        try (ZipFile zip = new ZipFile(zipFile)) {
            Enumeration<? extends ZipEntry> entries = zip.entries();
            while (entries.hasMoreElements()) {
                ZipEntry entry = entries.nextElement();
                File entryFile = new File(destDir, entry.getName());
                if (!entryFile.getCanonicalPath().startsWith(destDirPath + File.separator)) {
                    throw new IOException("Zip entry is trying to escape destination directory: " + entry.getName());
                }
                if (entry.isDirectory()) {
                    entryFile.mkdirs();
                } else {
                    File parent = entryFile.getParentFile();
                    if (parent != null && !parent.exists()) {
                        parent.mkdirs();
                    }
                    try (InputStream in = zip.getInputStream(entry);
                         OutputStream out = new FileOutputStream(entryFile)) {
                        copyStream(in, out);
                    }
                }
            }
        }
    }
    // endregion

    // Secure Utils
    private static String bytesToHex(byte[] bytes) {
        BigInteger bigInt = new BigInteger(1, bytes);
        String hex = bigInt.toString(16);
        int paddingLength = (bytes.length * 2) - hex.length();
        if (paddingLength > 0) {
            return String.format("%0" + paddingLength + "d", 0) + hex;
        } else {
            return hex;
        }
    }

    public static String getMessageDigest(String message, String algorithm) throws NoSuchAlgorithmException {
        return getMessageDigest(message.getBytes(StandardCharsets.UTF_8), algorithm);
    }

    public static String getMessageDigest(byte[] bytes, String algorithm) throws NoSuchAlgorithmException {
        MessageDigest md = MessageDigest.getInstance(algorithm);
        md.update(bytes);
        return bytesToHex(md.digest());
    }

    public static String getMessageDigest(ByteBuffer buffer, String algorithm) throws NoSuchAlgorithmException {
        MessageDigest md = MessageDigest.getInstance(algorithm);
        md.update(buffer);
        return bytesToHex(md.digest());
    }

    public static String getFileDigest(File file, String algorithm) throws IOException, NoSuchAlgorithmException {
        try (InputStream fis = new FileInputStream(file)) {
            return getStreamDigest(fis, algorithm);
        }
    }

    public static String getAssetDigest(Context context, String assetPath, String algorithm) throws IOException, NoSuchAlgorithmException {
        try (InputStream is = context.getAssets().open(assetPath)) {
            return getStreamDigest(is, algorithm);
        }
    }

    public static String getStreamDigest(InputStream in, String algorithm) throws IOException, NoSuchAlgorithmException {
        MessageDigest md = MessageDigest.getInstance(algorithm);
        try (InputStream bis = new BufferedInputStream(in)) {
            byte[] buffer = new byte[8192];
            int bytesRead;
            while ((bytesRead = bis.read(buffer)) != -1)
                md.update(buffer, 0, bytesRead);
        }
        return bytesToHex(md.digest());
    }
}