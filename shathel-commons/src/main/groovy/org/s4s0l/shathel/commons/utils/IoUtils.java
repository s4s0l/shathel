package org.s4s0l.shathel.commons.utils;

import org.apache.commons.io.FileUtils;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
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

    /**
     * Unzip it
     *
     * @param zipFile      input zip file
     * @param outputFolder zip file output folder
     */
    public static void unZipIt(File zipFile, File outputFolder) {

        byte[] buffer = new byte[1024];

        try {

            //create output directory is not exists
            File folder = outputFolder;
            if (!folder.exists()) {
                folder.mkdirs();
            }

            //get the zip file content
            try (ZipInputStream zis =
                         new ZipInputStream(new FileInputStream(zipFile))) {
                //get the zipped file list entry
                ZipEntry ze = zis.getNextEntry();

                while (ze != null) {

                    String fileName = ze.getName();
                    File newFile = new File(outputFolder + File.separator + fileName);
                    //create all non exists folders
                    //else you will hit FileNotFoundException for compressed folder
                    new File(newFile.getParent()).mkdirs();

                    FileOutputStream fos = new FileOutputStream(newFile);

                    int len;
                    while ((len = zis.read(buffer)) > 0) {
                        fos.write(buffer, 0, len);
                    }

                    fos.close();
                    ze = zis.getNextEntry();
                }
            }

        } catch (IOException ex) {
            ex.printStackTrace();
        }
    }

    /**
     * http://stackoverflow.com/a/32052016
     * @param sourceDirPath
     * @param zipFilePath
     * @throws IOException
     */
    public static void zipIt(File sourceDirPath, File zipFilePath) throws IOException {
        Path p = Files.createFile(zipFilePath.toPath());
        try (ZipOutputStream zs = new ZipOutputStream(Files.newOutputStream(p))) {
            Path pp = sourceDirPath.toPath();
            Files.walk(pp)
                    .filter(path -> !Files.isDirectory(path))
                    .forEach(path -> {
                        ZipEntry zipEntry = new ZipEntry(pp.relativize(path).toString());
                        try {
                            zs.putNextEntry(zipEntry);
                            zs.write(Files.readAllBytes(path));
                            zs.closeEntry();
                        } catch (Exception e) {
                            throw new RuntimeException("Unable to zip " + sourceDirPath + " to " + zipFilePath);
                        }
                    });
        }
    }
}
