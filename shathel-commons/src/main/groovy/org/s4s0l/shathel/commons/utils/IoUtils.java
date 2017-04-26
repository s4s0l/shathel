package org.s4s0l.shathel.commons.utils;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.slf4j.Logger;

import java.io.*;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.concurrent.TimeUnit;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
import java.util.zip.ZipOutputStream;

import static org.slf4j.LoggerFactory.getLogger;

/**
 * tweaked https://www.mkyong.com/java/how-to-decompress-files-from-a-zip-file/
 *
 * @author Marcin Wielgus
 */
public class IoUtils {

    /**
     * @param srcDir source directory
     * @param dstDir target directory
     */
    public static void copyContents(File srcDir, File dstDir) {
        try {
            FileUtils.copyDirectory(srcDir, dstDir);
        } catch (IOException e) {
            throw new RuntimeException("Unable to copy");
        }
    }

    public static void unZipIt(InputStream zipFile, File outputFolder) {
        byte[] buffer = new byte[1024];

        try {

            //create output directory is not isVmPresent
            File folder = outputFolder;
            if (!folder.exists()) {
                folder.mkdirs();
            }

            //get the zip file content
            try (ZipInputStream zis =
                         new ZipInputStream(zipFile)) {
                //get the zipped file list entry
                ZipEntry ze = zis.getNextEntry();
                while (ze != null) {

                    String fileName = ze.getName();
                    File newFile = new File(outputFolder + File.separator + fileName);
                    //create all non isVmPresent folders
                    //else you will hit FileNotFoundException for compressed folder
                    new File(newFile.getParent()).mkdirs();
                    if (ze.isDirectory()) {
                        newFile.mkdirs();
                    } else {
                        try (FileOutputStream fos = new FileOutputStream(newFile)) {
                            int len;
                            while ((len = zis.read(buffer)) > 0) {
                                fos.write(buffer, 0, len);
                            }
                        }
                    }
                    ze = zis.getNextEntry();
                }
            }
        } catch (IOException ex) {
            throw new RuntimeException(ex);
        }
    }

    /**
     * Unzip it
     *
     * @param zipFile      input zip file
     * @param outputFolder zip file output folder
     */
    public static void unZipIt(File zipFile, File outputFolder) {
        try {
            try (FileInputStream fis = new FileInputStream(zipFile)) {
                unZipIt(fis, outputFolder);
            }
        } catch (IOException e) {
            throw new RuntimeException("Unable to unzip " + zipFile + " to " + outputFolder, e);
        }

    }

    /**
     * http://stackoverflow.com/a/32052016
     *
     * @param sourceDirPath path to dir
     * @param zipFilePath   path to zip file
     */
    public static void zipIt(File sourceDirPath, File zipFilePath) {
        Path p = null;
        try {
            p = Files.createFile(zipFilePath.toPath());
            try (OutputStream fos = Files.newOutputStream(p)) {
                zipIt(sourceDirPath, fos);
            }
        } catch (IOException e) {
            throw new RuntimeException("Unable to zip", e);
        }
    }

    public static void zipIt(File sourceDirPath, OutputStream outputStream) {
        zipIt(sourceDirPath, outputStream, x -> true);
    }

    public static void zipIt(File sourceDirPath, OutputStream outputStream, FileFilter ff) {
        try {
            try (ZipOutputStream zs = new ZipOutputStream(outputStream)) {
                Path pp = sourceDirPath.toPath();
                Files.walk(pp)
                        .filter(f -> ff.accept(f.toFile()))
                        .filter(path -> !Files.isDirectory(path))
                        .forEach(path -> {
                            ZipEntry zipEntry = new ZipEntry(pp.relativize(path).toString());
                            try {
                                zs.putNextEntry(zipEntry);
                                try (FileInputStream x = new FileInputStream(path.toFile())) {
                                    IOUtils.copyLarge(x, zs);
                                }
                                zs.closeEntry();
                            } catch (Exception e) {
                                throw new RuntimeException("Unable to zip " + sourceDirPath + " to stream", e);
                            }
                        });
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private static final Logger LOGGER = getLogger(IoUtils.class);

    public static void waitForFile(File f, int maxSeconds, RuntimeException e) {
        for (int i = 0; i < maxSeconds; i++) {
            if (f.exists()) {
                return;
            }
            try {
                Thread.sleep(1000);
            } catch (InterruptedException ex) {
                LOGGER.warn("Interrupted while delaying file presence check", ex);
            }
        }
        throw e;

    }





    public static boolean isSocketOpened(String host, int port, int timeout) {
        final Socket sock = new Socket();
        try {
            sock.connect(new InetSocketAddress(host, port), timeout);
            return true;
        } catch (IOException e1) {
            LOGGER.trace("Exception during waiting for socket - should happen", e1);
        } finally {
            if (sock.isConnected()) {
                try {
                    sock.close();
                } catch (IOException e1) {
                    LOGGER.warn("Closing socket failed", e1);
                }
            }
        }
        return false;
    }

    public static void waitForSocket(String host, int port, int maxSeconds, RuntimeException e) {
        for (int i = 0; i < maxSeconds; i++) {
            final Socket sock = new Socket();
            final int timeOut = (int) TimeUnit.SECONDS.toMillis(maxSeconds); // 5 sec wait period
            try {
                sock.connect(new InetSocketAddress(host, port), timeOut);
                return;
            } catch (IOException e1) {
                LOGGER.trace("Exception during waiting for socket - should happen", e1);
            } finally {
                if (sock.isConnected()) {
                    try {
                        sock.close();
                    } catch (IOException e1) {
                        LOGGER.warn("Closing socket failed", e1);
                    }
                }
            }
            try {
                Thread.sleep(1000);
            } catch (InterruptedException ex) {
                LOGGER.warn("Interrupted while delaying file presence check", ex);
            }
        }
        throw e;

    }


}
