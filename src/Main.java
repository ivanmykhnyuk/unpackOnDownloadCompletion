import de.innosystec.unrar.Archive;
import de.innosystec.unrar.exception.RarException;
import de.innosystec.unrar.rarfile.FileHeader;
import name.pachler.nio.file.*;
import org.apache.commons.compress.archivers.sevenz.SevenZArchiveEntry;
import org.apache.commons.compress.archivers.sevenz.SevenZFile;
import org.apache.commons.compress.archivers.tar.TarArchiveEntry;
import org.apache.commons.compress.archivers.tar.TarArchiveInputStream;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

public class Main {
    private static String watchedDir;

    private static boolean archiveFileDropped = false;

    static Object monitor = new Object();

    static byte[] buffer = new byte[1024];


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
                ArchiveFiles filter = new ArchiveFiles(new String[]{"zip", "tar", "7z", "rar"});

                while (true) {
                    if (archiveFileDropped) {
                        File outputDir = new File(watchedDir);
                        File[] list = outputDir.listFiles(filter);
                        for (File l : list) {
                            unRar(l);
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

    public static void unZipIt(File zipArchive) {
        try {
            ZipInputStream zis = new ZipInputStream(new FileInputStream(zipArchive));

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
            zipArchive.delete();
        } catch (IOException ex) {
            ex.printStackTrace();
        }
    }

    public static void unTar(File tarArchive) {
        try {
            TarArchiveInputStream inputStream = new TarArchiveInputStream(new FileInputStream(tarArchive));

            TarArchiveEntry entry = null;
            while ((entry = (TarArchiveEntry) inputStream.getNextEntry()) != null) {
                String fileName = entry.getName();
                File newFile = new File(watchedDir + File.separator + fileName);
                if (entry.isDirectory()) {
                    newFile.mkdir();
                } else {
                    FileOutputStream fos = new FileOutputStream(newFile);

                    int len;
                    while ((len = inputStream.read(buffer)) > 0) {
                        fos.write(buffer, 0, len);
                    }
                    fos.close();
                }
            }
            inputStream.close();

            //remove file
            tarArchive.delete();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void unRar(File rarArchive) {
        try {
            Archive inputStream = new Archive(rarArchive);

            FileHeader entry = null;
            while ((entry = inputStream.nextFileHeader()) != null) {
                String fileName = entry.getFileNameString().trim();
                File newFile = new File(watchedDir + File.separator + fileName);
                if (entry.isDirectory()) {
                    newFile.mkdir();
                } else {
                    FileOutputStream fos = new FileOutputStream(newFile);
                    inputStream.extractFile(entry, fos);
                    fos.close();
                }
            }
            inputStream.close();

            //remove file
            rarArchive.delete();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (RarException e) {
            e.printStackTrace();
        }
    }


    //TODO: method throws exception
    public static void un7Zip(File sevenZipArchive) {
        try {
            SevenZFile inputStream = new SevenZFile(sevenZipArchive);

            SevenZArchiveEntry entry = null;
            while ((entry = inputStream.getNextEntry()) != null) {
                String fileName = entry.getName();
                File newFile = new File(watchedDir + File.separator + fileName);
                if (entry.isDirectory()) {
                    newFile.mkdir();
                } else {
                    FileOutputStream fos = new FileOutputStream(newFile);

                    int len;
                    while ((len = inputStream.read(buffer)) > 0) {
                        fos.write(buffer, 0, len);
                    }
                    fos.close();
                }
            }
            inputStream.close();

            //remove file
            sevenZipArchive.delete();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}