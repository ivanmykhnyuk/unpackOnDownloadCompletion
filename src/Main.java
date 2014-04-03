import name.pachler.nio.file.*;

import java.io.*;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

public class Main {
    private static String watchedDir;

    private static boolean archiveFileDropped = false;

    static Object monitor = new Object();


    public static void main(String[] args) throws IOException {
        WatchService watchService = FileSystems.getDefault().newWatchService();

        watchedDir = "D:\\temp";

        Path watchedDirPath = Paths.get(watchedDir);
        final WatchKey key = watchedDirPath.register(watchService, StandardWatchEventKind.ENTRY_CREATE);


        Thread pollingThread = new Thread() {
            @Override
            public void run() {
                while (true) {
                    List<name.pachler.nio.file.WatchEvent<?>> list = key.pollEvents();
                    for (WatchEvent watchEvent : list) {
                        archiveFileDropped = true;

                        synchronized (monitor) {
                            monitor.notify();
                        }

                        System.out.print("ololo, event....");

                    }
                }
            }
        };


        Thread unpackingThread = new Thread() {

            @Override
            public void run() {
                ArchiveFiles filter = new ArchiveFiles(new String[]{"zip"});

                while (true) {
                    if (archiveFileDropped) {
                        File outputDir = new File(watchedDir);
                        File[] list = outputDir.listFiles(filter);
                        for (File l : list) {
                            unZipIt(l);
                        }
                        archiveFileDropped = false;

                    } else {
                        try {
                            synchronized (monitor) {
                                monitor.wait();
                            }
                        } catch (InterruptedException e) {

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
    }

    public static void unZipIt(File zipFile) {

        byte[] buffer = new byte[1024];

        try {
            //get the zip file content
            ZipInputStream zis = new ZipInputStream(new FileInputStream(zipFile));
            //get the zipped file list entry


            ZipEntry ze = null;
            while ((ze = zis.getNextEntry()) != null) {
                String fileName = ze.getName();
                File newFile = new File(watchedDir + File.separator + fileName);
                if (ze.isDirectory()) {
                    newFile.mkdir();
                } else {
                    FileOutputStream fos = new FileOutputStream(newFile);

                    int len;
                    while ((len = zis.read(buffer)) > 0) {
                        fos.write(buffer, 0, len);
                    }
                    fos.close();
                }
            }

            zis.closeEntry();
            zis.close();

            //remove file
            zipFile.delete();
        } catch (IOException ex) {
            ex.printStackTrace();
        }
    }
}