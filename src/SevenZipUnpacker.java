import org.apache.commons.compress.archivers.sevenz.SevenZArchiveEntry;
import org.apache.commons.compress.archivers.sevenz.SevenZFile;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

/**
 * Created by i.mikhnyuk on 04.04.14.
 */
public class SevenZipUnpacker implements ArchiveUnpacker {

    public SevenZipUnpacker(String outputDir, byte[] buffer) {
        this.outputDir = outputDir;
        this.buffer = buffer;
    }

    @Override
    public void unpack(File sevenZipArchive) {
        try {
            SevenZFile inputStream = new SevenZFile(sevenZipArchive);

            SevenZArchiveEntry entry = null;
            while ((entry = inputStream.getNextEntry()) != null) {
                String fileName = entry.getName();
                File newFile = new File(outputDir + File.separator + fileName);
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
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private String outputDir;
    private byte [] buffer;
}
