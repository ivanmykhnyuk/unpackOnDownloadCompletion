import org.apache.commons.compress.archivers.tar.TarArchiveEntry;
import org.apache.commons.compress.archivers.tar.TarArchiveInputStream;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;

/**
 * Created by i.mikhnyuk on 04.04.14.
 */
public class TarUnpacker implements ArchiveUnpacker {

    public TarUnpacker(String outputDir, byte[] buffer) {
        this.outputDir = outputDir;
        this.buffer = buffer;
    }

    @Override
    public void unpack(File tarArchive) {
        try {
            TarArchiveInputStream inputStream = new TarArchiveInputStream(new FileInputStream(tarArchive));

            TarArchiveEntry entry;
            while ((entry = (TarArchiveEntry) inputStream.getNextEntry()) != null) {
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
