
import java.io.File;
import java.io.IOException;
import java.nio.file.*;
import java.util.HashMap;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

//TODO create dir if archive is content is not in dir
public class Main {
    public static void main(String[] args) throws IOException {
        unpackers = new HashMap<String, ArchiveUnpacker>();
        monitor = new Object();
        buffer = new byte[1024];
        toBeUnpackedFiles = new HashMap<String, TimerTask>(4);
        modificationEventTimer = new Timer();

        WatchService watchService = FileSystems.getDefault().newWatchService();

        final String watchedDir = "D:\\temp";
        final String[] supportedArchiveFormats = new String[]{"zip", "tar", "7z", "xz", "rar", "gz", "bz2"};


        Path watchedDirPath = Paths.get(watchedDir);
        WatchKey key = watchedDirPath.register(watchService, StandardWatchEventKinds.ENTRY_CREATE, StandardWatchEventKinds.ENTRY_MODIFY);

        Thread unpackingThread = new Thread() {
            @Override
            public void run() {
                while (true) {
                    if (archiveFileDropped) {
                        for (String path : toBeUnpackedFiles.keySet()) {
                            String fileExtention = path.substring(path.lastIndexOf(".") + 1);

                            //cash unpackers
                            if (!unpackers.containsKey(fileExtention)) {
                                if (fileExtention.equals("zip")) {
                                    unpackers.put(fileExtention, new ZipUnpacker(watchedDir, buffer));
                                } else if (fileExtention.equals("tar")) {
                                    unpackers.put(fileExtention, new TarUnpacker(watchedDir, buffer));
                                } else if (fileExtention.equals("7z")) {
                                    unpackers.put(fileExtention, new SevenZipUnpacker(watchedDir, buffer));
                                } else if (fileExtention.equals("xz")) {
                                    unpackers.put(fileExtention, new XZUnpacker(watchedDir, buffer));
                                } else if (fileExtention.equals("rar")) {
                                    unpackers.put(fileExtention, new RarUnpacker(watchedDir, buffer));
                                } else if (fileExtention.equals("gz")) {
                                    unpackers.put(fileExtention, new GZipUnpacker(watchedDir, buffer));
                                } else if (fileExtention.equals("bz2")) {
                                    unpackers.put(fileExtention, new BZ2Unpacker(watchedDir, buffer));
                                }
                            }

                            File file = new File(path);
                            unpackers.get(fileExtention).unpack(file);
                            toBeUnpackedFiles.remove(path);
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
                String fileName = watchedDir + File.separator + path.toString();
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
                        if (toBeUnpackedFiles.containsKey(fileName)) {
                            toBeUnpackedFiles.get(fileName).cancel();
                        }

                        toBeUnpackedFiles.put(fileName, new TimerTask() {
                            @Override
                            public void run() {
                                synchronized (monitor) {
                                    monitor.notify();
                                }
                            }
                        });

                        modificationEventTimer.schedule(toBeUnpackedFiles.get(fileName), 1024);
                    }
                }
            }

            boolean valid = key.reset();
            if (!valid) {
               throw new IllegalStateException("Cannot reset poll key.");
            }
        }
    }

    private static boolean archiveFileDropped;

    private static Object monitor;

    private static byte[] buffer;

    private static HashMap<String, ArchiveUnpacker> unpackers;

    private static HashMap<String, TimerTask> toBeUnpackedFiles;

    private static Timer modificationEventTimer;
}