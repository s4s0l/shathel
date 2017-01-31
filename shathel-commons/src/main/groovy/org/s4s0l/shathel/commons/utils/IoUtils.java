package org.s4s0l.shathel.commons.utils;

import org.apache.commons.io.FileUtils;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
import java.util.zip.ZipOutputStream;

/**
 * tweaked https://www.mkyong.com/java/how-to-decompress-files-from-a-zip-file/
 *
 * @author Matcin Wielgus
 */
public class IoUtils {


    public static void copyContents(File srcDir, File dstDir) {
        try {
            FileUtils.copyDirectoryToDirectory(srcDir, dstDir);
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
     * @param sourceDirPath
     * @param zipFilePath
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
                                zs.write(Files.readAllBytes(path));
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
}
