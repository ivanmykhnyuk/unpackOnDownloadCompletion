import org.tukaani.xz.XZInputStream;

import java.io.*;
import java.util.zip.GZIPInputStream;

/**
 * Created by i.mikhnyuk on 04.04.14.
 */
public class XZUnpacker implements ArchiveUnpacker {

    public XZUnpacker(String outputDir, byte[] buffer) {
        this.outputDir = outputDir;
        this.buffer = buffer;
    }

    @Override
    public void unpack(File xzArchive) {
        try {
            BufferedInputStream in = new BufferedInputStream(new FileInputStream(xzArchive));
            String fileName = xzArchive.getAbsolutePath();

            fileName = fileName.substring(0, fileName.lastIndexOf("."));

            FileOutputStream out = new FileOutputStream(fileName);
            XZInputStream gzIn = new XZInputStream(in);

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
