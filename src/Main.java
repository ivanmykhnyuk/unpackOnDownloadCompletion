import name.pachler.nio.file.*;

import java.io.*;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

public class Main {
    private static Path watchedDir;

    private static boolean archiveFileDropped = false;


    public static void main(String[] args) throws IOException {
        WatchService watchService = FileSystems.getDefault().newWatchService();

        watchedDir = Paths.get("D://temp");
        final WatchKey key = watchedDir.register(watchService, StandardWatchEventKind.ENTRY_CREATE);


        Thread pollingThread = new Thread() {
            @Override
            public void run() {
                while (true) {
                    List<name.pachler.nio.file.WatchEvent<?>> list = key.pollEvents();
                    for (WatchEvent watchEvent : list) {
                        archiveFileDropped = true;
                        System.out.print("ololo, event....");

                    }
                }
            }
        };


        Thread unpackingThread = new Thread() {

            @Override
            public void run() {
                ArchiveFiles filter = new ArchiveFiles(new String[] {"zip"});

                while (true) {
                    if (archiveFileDropped) {
                        File dkdfk = new File("D://temp");
                        File [] list = dkdfk.listFiles(filter);
                        for (File l : list) {
                            unZipIt(l);
                        }
                        archiveFileDropped = false;

                    } else {
                        try {
                            sleep(2000);
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                    }
                }
            }
        };


        unpackingThread.start();
        pollingThread.start();
        try {
            unpackingThread.join();
            pollingThread.join();
        } catch (InterruptedException e) {

        }


//        unZipIt(new File(INPUT_ZIP_FILE));
    }

    public static void unZipIt(File zipFile) {

        byte[] buffer = new byte[1024];

        try {
//            String outputFolder = zipFile.getParent();
            String outputFolder = "D://temp";
            //create output directory is not exists
            File folder = new File(outputFolder);
            if (!folder.exists()) {
                folder.mkdir();
            }

            //get the zip file content
            ZipInputStream zis = new ZipInputStream(new FileInputStream(zipFile));
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

            zis.closeEntry();
            zis.close();

            //remove file
            zipFile.delete();
        } catch (IOException ex) {
            ex.printStackTrace();
        }
    }


    // inner class, generic extension filter
    public static class ArchiveFiles implements FilenameFilter {
        private String[] supportedArchiveFormats = {"zip"};

        public ArchiveFiles(String [] ext) {
            this.supportedArchiveFormats = ext;
        }

        public boolean accept(File dir, String name) {
            boolean acceptableFile = false;
            for (String fileFormat : supportedArchiveFormats) {
                if (name.endsWith(fileFormat)) {
                    acceptableFile = true;
                    break;
                }
            }

            return acceptableFile;
        }
    }
}