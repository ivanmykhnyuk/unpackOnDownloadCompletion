import java.io.File;
import java.io.FilenameFilter;

/**
* Created by i.mikhnyuk on 03.04.14.
*/
public class ArchiveFiles implements FilenameFilter {
    private String[] supportedArchiveFormats;

    public ArchiveFiles(String[] ext) {
        this.supportedArchiveFormats = ext;
    }

    public boolean accept(File dir, String name) {
        boolean acceptableFile = false;
        for (String fileFormat : supportedArchiveFormats) {
            if (name.endsWith(fileFormat)) {
                acceptableFile = true;
                break;
            }
        }

        return acceptableFile;
    }
}
