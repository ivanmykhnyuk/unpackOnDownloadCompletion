import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

/**
 * Created by i.mikhnyuk on 04.04.14.
 */
public class ZipUnpacker implements ArchiveUnpacker {

    public ZipUnpacker(String outputDir, byte [] buffer) {
        this.outputDir = outputDir;
        this.buffer = buffer;

    }

    @Override
    public void unpack(File zipArchive) {
        try {
            ZipInputStream zis = new ZipInputStream(new FileInputStream(zipArchive));

            ZipEntry ze = null;
            while ((ze = zis.getNextEntry()) != null) {
                String fileName = ze.getName();
                File newFile = new File(outputDir + File.separator + fileName);
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
        } catch (IOException ex) {
            ex.printStackTrace();
        }
    }

    private String outputDir;
    private byte [] buffer;
}
