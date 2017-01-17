package org.s4s0l.shathel.commons.utils;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
/**
 * tweaked https://www.mkyong.com/java/how-to-decompress-files-from-a-zip-file/
 * @author Matcin Wielgus
 */
public class IoUtils
{


    public static void copyContents(File srcDir, File dstDir){

    }

    /**
     * Unzip it
     * @param zipFile input zip file
     * @param outputFolder zip file output folder
     */
    public static void unZipIt(File zipFile, File outputFolder){

        byte[] buffer = new byte[1024];

        try{

            //create output directory is not exists
            File folder = outputFolder;
            if(!folder.exists()){
                folder.mkdirs();
            }

            //get the zip file content
            try(ZipInputStream zis =
                    new ZipInputStream(new FileInputStream(zipFile))) {
                //get the zipped file list entry
                ZipEntry ze = zis.getNextEntry();

                while (ze != null) {

                    String fileName = ze.getName();
                    File newFile = new File(outputFolder + File.separator + fileName);

                    System.out.println("file unzip : " + newFile.getAbsoluteFile());

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

            System.out.println("Done");

        }catch(IOException ex){
            ex.printStackTrace();
        }
    }
}
