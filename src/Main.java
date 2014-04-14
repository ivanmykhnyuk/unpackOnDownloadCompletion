
import java.io.File;
import java.io.IOException;
import java.nio.file.*;
import java.util.HashMap;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

public class Main {
    public static void main(String[] args) throws IOException {
        unPackers = new HashMap<String, ArchiveUnpacker>();
        monitor = new Object();
        buffer = new byte[1024];
        toBeUnpackedFiles = new HashMap<Path, TimerTask>(4);
        modificationEventTimer = new Timer();

        WatchService watchService = FileSystems.getDefault().newWatchService();

        final String watchedDir = "D:\\temp";
        final String[] supportedArchiveFormats = new String[]{"zip", "tar", "7z", "rar", "gz", "bz2"};


        Path watchedDirPath = Paths.get(watchedDir);
        WatchKey key = watchedDirPath.register(watchService, StandardWatchEventKinds.ENTRY_CREATE, StandardWatchEventKinds.ENTRY_MODIFY);

        Thread unpackingThread = new Thread() {
            @Override
            public void run() {
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
                                } else if (fileExtention.equals("bz2")) {
                                    unPackers.put(fileExtention, new BZ2Unpacker(watchedDir, buffer));
                                }
                            }

                            unPackers.get(fileExtention).unpack(file);
                            toBeUnpackedFiles.remove(file.toPath());
                            file.delete();
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
            try {
                key = watchService.take();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }

            List<WatchEvent<?>> list = key.pollEvents();
            for (WatchEvent watchEvent : list) {
                Path path = (Path) watchEvent.context();
                String fileName = path.toString();
                int i;
                for (i = 0; i < supportedArchiveFormats.length; ++i) {
                    if (fileName.endsWith(supportedArchiveFormats[i])) {
                        break;
                    }
                }

                if (i != supportedArchiveFormats.length) {
                    WatchEvent.Kind<?> kind = watchEvent.kind();
                    if (kind == StandardWatchEventKinds.ENTRY_CREATE) {
                        archiveFileDropped = true;
                    } else if (kind == StandardWatchEventKinds.ENTRY_MODIFY) {
                        if (toBeUnpackedFiles.containsKey(path)) {
                            toBeUnpackedFiles.get(path).cancel();
                        }

                        toBeUnpackedFiles.put(path, new TimerTask() {
                            @Override
                            public void run() {
                                synchronized (monitor) {
                                    monitor.notify();
                                }
                            }
                        });

                        modificationEventTimer.schedule(toBeUnpackedFiles.get(path), 1024);
                    }
                }
            }

            boolean valid = key.reset();
            if (!valid) {
                System.exit(2);
            }
        }
    }

    private static boolean archiveFileDropped;

    private static Object monitor;

    private static byte[] buffer;

    private static HashMap<String, ArchiveUnpacker> unPackers;

    private static HashMap<Path, TimerTask> toBeUnpackedFiles;

    private static Timer modificationEventTimer;
}