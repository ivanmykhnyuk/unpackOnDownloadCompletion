import org.apache.commons.compress.compressors.bzip2.BZip2CompressorInputStream;

import java.io.*;
import java.util.zip.GZIPInputStream;

/**
 * Created by i.mikhnyuk on 04.04.14.
 */
public class BZ2Unpacker implements ArchiveUnpacker {

    public BZ2Unpacker(String outputDir, byte[] buffer) {
        this.outputDir = outputDir;
        this.buffer = buffer;
    }

    @Override
    public void unpack(File gzipArchive) {
        try {
            BufferedInputStream in = new BufferedInputStream(new FileInputStream(gzipArchive));
            String fileName = gzipArchive.getAbsolutePath();

            fileName = fileName.substring(0, fileName.lastIndexOf("."));

            FileOutputStream out = new FileOutputStream(fileName);
            BZip2CompressorInputStream gzIn = new BZip2CompressorInputStream(in);

            int n;
            while (-1 != (n = gzIn.read(buffer))) {
                out.write(buffer, 0, n);
            }

            out.close();
            gzIn.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private String outputDir;
    private byte [] buffer;
}
