import de.innosystec.unrar.Archive;
import de.innosystec.unrar.exception.RarException;
import de.innosystec.unrar.rarfile.FileHeader;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

/**
 * Created by i.mikhnyuk on 04.04.14.
 */
public class RarUnpacker implements ArchiveUnpacker {

    public RarUnpacker(String outputDir, byte[] buffer) {
        this.outputDir = outputDir;
        this.buffer = buffer;

    }

    @Override
    public void unpack(File rarArchive) {
        try {
            Archive inputStream = new Archive(rarArchive);

            FileHeader entry = null;
            while ((entry = inputStream.nextFileHeader()) != null) {
                String fileName = entry.getFileNameString().trim();
                File newFile = new File(outputDir + File.separator + fileName);
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

    private String outputDir;
    private byte [] buffer;
}
