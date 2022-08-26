package pro.velovec.inferno.reborn.common.utils;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;

public class FileUtils {

    public static byte[] toByteArray(File file) throws IOException {
        return Files.readAllBytes(file.toPath());
    }
}
