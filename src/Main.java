import name.pachler.nio.file.*;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;

public class Main {
    public static void main(String[] args) throws IOException {
        unPackers = new HashMap<String, ArchiveUnpacker>();
        monitor = new Object();
        buffer = new byte[1024];

        WatchService watchService = FileSystems.getDefault().newWatchService();

        final String watchedDir = "D:\\temp";


        Path watchedDirPath = Paths.get(watchedDir);
        final WatchKey key = watchedDirPath.register(watchService, StandardWatchEventKind.ENTRY_CREATE);

        Thread unpackingThread = new Thread() {
            @Override
            public void run() {
                String[] supportedArchiveFormats = new String[]{"zip", "tar", "7z", "rar", "gz"};
                ArchiveFiles filter = new ArchiveFiles(supportedArchiveFormats);
                File outputDir = new File(watchedDir);

                while (true) {
                    if (archiveFileDropped) {
                        File[] files = outputDir.listFiles(filter);
                        for (File file : files) {
                            String fileExtention = file.getName();
                            fileExtention = fileExtention.substring(fileExtention.lastIndexOf(".") + 1);

                            //cash unpackers
                            if (!unPackers.containsKey(fileExtention)) {
                                if (fileExtention.equals("zip")) {
                                    unPackers.put(fileExtention, new ZipUnpacker(watchedDir, buffer));
                                } else if (fileExtention.equals("tar")) {
                                    unPackers.put(fileExtention, new TarUnpacker(watchedDir, buffer));
                                } else if (fileExtention.equals("7z")) {
                                    unPackers.put(fileExtention, new SevenZipUnpacker(watchedDir, buffer));
                                } else if (fileExtention.equals("rar")) {
                                    unPackers.put(fileExtention, new RarUnpacker(watchedDir, buffer));
                                } else if (fileExtention.equals("gz")) {
                                    unPackers.put(fileExtention, new GZipUnpacker(watchedDir, buffer));
                                }
                            }

                            unPackers.get(fileExtention).unpack(file);
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

        while (true) {
            List<name.pachler.nio.file.WatchEvent<?>> list = key.pollEvents();
            for (WatchEvent watchEvent : list) {
                archiveFileDropped = true;

                synchronized (monitor) {
                    monitor.notify();
                }
            }
        }
    }

    private static boolean archiveFileDropped;

    private static Object monitor;

    private static byte[] buffer;

    private static HashMap<String, ArchiveUnpacker> unPackers;
}