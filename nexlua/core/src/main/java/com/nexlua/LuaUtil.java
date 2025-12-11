package com.nexlua;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.res.AssetFileDescriptor;
import android.content.res.AssetManager;

import java.io.BufferedInputStream;
import java.io.ByteArrayOutputStream;
import java.io.Closeable;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Enumeration;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;
import java.util.zip.ZipOutputStream;

@SuppressLint("StaticFieldLeak")
@SuppressWarnings("IOStreamConstructor")
public final class LuaUtil {

    private LuaUtil() {
    }

    private static Context context;
    private static AssetManager assetManager;
    private static final String FAILED_TO_CREATE_DEST_DIR = "Failed to create destination directory: ";

    public static void init(Context ctx) {
        context = ctx.getApplicationContext();
        assetManager = ctx.getAssets();
    }

    public static Context getContext() {
        return context;
    }

    public static AssetManager getAssetManager() {
        return assetManager;
    }

    public static ByteBuffer wrap(byte[] bytes) {
        ByteBuffer buffer = ByteBuffer.allocateDirect(bytes.length);
        buffer.put(bytes);
        buffer.flip();
        return buffer;
    }

    // Stream Utils
    public static byte[] readStreamBytes(InputStream in) throws IOException {
        ByteArrayOutputStream buffer = new ByteArrayOutputStream(8192);
        byte[] temp = new byte[8192];
        int n;
        while ((n = in.read(temp)) != -1) {
            buffer.write(temp, 0, n);
        }
        return buffer.toByteArray();
    }

    public static ByteBuffer readStreamBuffer(InputStream in, int knownSize) throws IOException {
        ByteBuffer buffer = ByteBuffer.allocateDirect(knownSize);
        byte[] temp = new byte[8192];
        int n;
        while ((n = in.read(temp)) != -1) {
            if (buffer.remaining() < n) {
                throw new IOException("InputStream size exceeds allocated buffer size: " + knownSize);
            }
            buffer.put(temp, 0, n);
        }
        buffer.flip();
        return buffer;
    }

    public static void copyStream(InputStream in, OutputStream out) throws IOException {
        byte[] buffer = new byte[8192];
        int n;
        while ((n = in.read(buffer)) != -1) {
            out.write(buffer, 0, n);
        }
    }

    // Stream Utils
    public static byte[] readStreamBytesWithAutoClose(InputStream in) throws IOException {
        try {
            return readStreamBytes(in);
        } finally {
            closeQuietly(in);
        }
    }

    public static ByteBuffer readStreamBufferWithAutoClose(InputStream in, int knownSize) throws IOException {
        try {
            return readStreamBuffer(in, knownSize);
        } finally {
            closeQuietly(in);
        }
    }

    public static void copyStreamWithAutoClose(InputStream in, OutputStream out) throws IOException {
        try {
            copyStream(in, out);
        } finally {
            closeQuietly(out);
            closeQuietly(in);
        }
    }
    public static ByteBuffer readFileBuffer(File file) throws IOException {
        return readStreamBufferWithAutoClose(new FileInputStream(file), (int) file.length());
    }

    public static byte[] readFileBytes(File file) throws IOException {
        return readStreamBytesWithAutoClose(new FileInputStream(file));
    }

    public static String readFile(File file) throws IOException {
        return new String(readFileBytes(file), StandardCharsets.UTF_8);
    }

    public static boolean rmFile(File file) {
        return file.delete();
    }

    public static boolean rmDir(File file) {
        if (file.isDirectory()) {
            File[] children = file.listFiles();
            if (children != null) {
                for (File child : children) {
                    if (child.isDirectory()) {
                        if (!rmDir(child)) return false;
                    } else {
                        if (!rmFile(child)) return false;
                    }
                }
            }
            return rmFile(file);
        }
        return false;
    }

    public static void copyFile(File src, File dest) throws IOException {
        copyStreamWithAutoClose(new FileInputStream(src), new FileOutputStream(dest));
    }

    public static void copyDir(File srcDir, File destDir) throws IOException {
        if (!destDir.exists() && !destDir.mkdirs()) {
            throw new IOException(FAILED_TO_CREATE_DEST_DIR + destDir);
        }
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
    public static String[] listAssets(String assetPath) throws IOException {
        return assetManager.list(assetPath);
    }

    public static boolean isAssetExists(String assetPath) {
        InputStream in = null;
        try {
            in = assetManager.open(assetPath);
            return true;
        } catch (IOException e) {
            return false;
        } finally {
            closeQuietly(in);
        }
    }

    public static ByteBuffer readAssetBuffer(String assetPath) throws IOException {
        try {
            AssetFileDescriptor fileDescriptor = assetManager.openFd(assetPath);
            try {
                return readStreamBufferWithAutoClose(fileDescriptor.createInputStream(), (int) fileDescriptor.getLength());
            } finally {
                closeQuietly(fileDescriptor);
            }
        } catch (IOException e) {
            return wrap(readAssetBytes(assetPath));
        }
    }

    public static byte[] readAssetBytes(String assetPath) throws IOException {
        return readStreamBytesWithAutoClose(assetManager.open(assetPath));
    }

    public static String readAsset(String assetPath) throws IOException {
        return new String(readAssetBytes(assetPath), StandardCharsets.UTF_8);
    }

    public static void copyAssetsFile(String assetPath, File destFile) throws IOException {
        copyStreamWithAutoClose(assetManager.open(assetPath), new FileOutputStream(destFile));
    }

    public static void copyAssetsDir(String assetPath, File destDir) throws IOException {
        if (!destDir.exists() && !destDir.mkdirs()) {
            throw new IOException(FAILED_TO_CREATE_DEST_DIR + destDir);
        }
        String[] assets = assetManager.list(assetPath);
        if (assets == null) return;
        for (String asset : assets) {
            String newAssetPath = assetPath.isEmpty() ? asset : assetPath + "/" + asset;
            File newDestFile = new File(destDir, asset);
            String[] subAssets = assetManager.list(newAssetPath);
            if (subAssets != null && subAssets.length > 0) {
                copyAssetsDir(newAssetPath, newDestFile);
            } else {
                copyAssetsFile(newAssetPath, newDestFile);
            }
        }
    }

    // Resource Utils
    public static ByteBuffer readRawBuffer(int id) throws IOException {
        try {
            AssetFileDescriptor fileDescriptor = context.getResources().openRawResourceFd(id);
            try {
                return readStreamBufferWithAutoClose(fileDescriptor.createInputStream(), (int) fileDescriptor.getLength());
            } finally {
                closeQuietly(fileDescriptor);
            }
        } catch (Exception e) {
            return wrap(readRawBytes(id));
        }
    }

    public static byte[] readRawBytes(int id) throws IOException {
        return readStreamBytesWithAutoClose(context.getResources().openRawResource(id));
    }

    public static String readRaw(int id) throws IOException {
        return new String(readRawBytes(id), StandardCharsets.UTF_8);
    }

    public static void copyRawFile(int id, File destFile) throws IOException {
        copyStreamWithAutoClose(context.getResources().openRawResource(id), new FileOutputStream(destFile));
    }

    // Zip Utils
    public static void zip(File srcFile, File zipFile) throws IOException {
        ZipOutputStream zipOutputStream = null;
        try {
            zipOutputStream = new ZipOutputStream(new FileOutputStream(zipFile));
            zipInternal(zipOutputStream, srcFile, "");
        } finally {
            closeQuietly(zipOutputStream);
        }
    }

    public static void zipInternal(ZipOutputStream zipOutputStream, File file, String baseName) throws IOException {
        boolean isDir = file.isDirectory();
        String entryName = baseName + file.getName() + (isDir ? "/" : "");
        zipOutputStream.putNextEntry(new ZipEntry(entryName));
        if (isDir) {
            zipOutputStream.closeEntry();
            File[] children = file.listFiles();
            if (children != null) {
                for (File child : children) {
                    zipInternal(zipOutputStream, child, entryName);
                }
            }
        } else {
            copyStreamWithAutoClose(new FileInputStream(file), zipOutputStream);
            zipOutputStream.closeEntry();
        }
    }

    public static void unzip(File zipFile, File destDir) throws IOException {
        if (!destDir.exists() && !destDir.mkdirs())
            throw new IOException(FAILED_TO_CREATE_DEST_DIR + destDir);
        String destPath = destDir.getCanonicalPath() + File.separator;
        ZipFile zip = null;
        try {
            zip = new ZipFile(zipFile);
            Enumeration<? extends ZipEntry> entries = zip.entries();
            while (entries.hasMoreElements()) {
                ZipEntry entry = entries.nextElement();
                File entryFile = new File(destDir, entry.getName());
                if (!entryFile.getCanonicalPath().startsWith(destPath))
                    throw new IOException("Zip entry escapes");
                if (entry.isDirectory()) {
                    entryFile.mkdirs();
                } else {
                    entryFile.getParentFile().mkdirs();
                    copyStreamWithAutoClose(zip.getInputStream(entry), new FileOutputStream(entryFile));
                }
            }
        } finally {
            closeQuietly(zip);
        }
    }

    // Digest Utils
    private static String bytesToHex(byte[] bytes) {
        StringBuilder hex = new StringBuilder(bytes.length * 2);
        for (byte b : bytes) {
            hex.append(String.format("%02x", b));
        }
        return hex.toString();
    }

    public static String getMessageDigest(String message, String algorithm) throws NoSuchAlgorithmException {
        return getMessageDigest(message.getBytes(StandardCharsets.UTF_8), algorithm);
    }

    public static String getMessageDigest(byte[] bytes, String algorithm) throws NoSuchAlgorithmException {
        MessageDigest md = MessageDigest.getInstance(algorithm);
        return bytesToHex(md.digest(bytes));
    }

    public static String getMessageDigest(ByteBuffer buffer, String algorithm) throws NoSuchAlgorithmException {
        MessageDigest md = MessageDigest.getInstance(algorithm);
        md.update(buffer);
        return bytesToHex(md.digest());
    }

    public static String getFileDigest(File file, String algorithm) throws IOException, NoSuchAlgorithmException {
        return getStreamDigestWithAutoClose(new BufferedInputStream(new FileInputStream(file)), algorithm);
    }

    public static String getAssetDigest(String assetPath, String algorithm) throws IOException, NoSuchAlgorithmException {
        return getStreamDigestWithAutoClose(new BufferedInputStream(assetManager.open(assetPath)), algorithm);
    }

    public static String getRawDigest(int id, String algorithm) throws IOException, NoSuchAlgorithmException {
        return getStreamDigestWithAutoClose(new BufferedInputStream(context.getResources().openRawResource(id)), algorithm);
    }

    public static String getStreamDigest(InputStream in, String algorithm) throws IOException, NoSuchAlgorithmException {
        MessageDigest md = MessageDigest.getInstance(algorithm);
        byte[] buffer = new byte[8192];
        int n;
        while ((n = in.read(buffer)) != -1) {
            md.update(buffer, 0, n);
        }
        return bytesToHex(md.digest());
    }

    public static String getStreamDigestWithAutoClose(InputStream in, String algorithm) throws IOException, NoSuchAlgorithmException {
        try {
            return getStreamDigest(in, algorithm);
        } finally {
            closeQuietly(in);
        }
    }

    public static String getParentPath(String path) {
        if (path == null || path.isEmpty()) {
            return "/";
        }
        File file = new File(path);
        String parent = file.getParent();
        return (parent == null || parent.isEmpty()) ? "/" : parent;
    }

    private static void closeQuietly(Closeable closeable) {
        if (closeable != null) {
            try {
                closeable.close();
            } catch (IOException ignored) {
            }
        }
    }
}