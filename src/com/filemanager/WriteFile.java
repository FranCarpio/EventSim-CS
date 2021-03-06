package com.filemanager;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

/**
 * Class to save filemanager in a text file
 *
 * @author Fran Carpio
 */
public class WriteFile {

    protected File file;
    protected FileWriter writer;

    public WriteFile(String fileName, boolean withDate)
            throws IOException {

        SimpleDateFormat MY_FORMAT = new SimpleDateFormat(
                "dd-MM-yyyy HH-mm-ss", Locale.getDefault());
		Date date = new Date();

        String path = WriteFile.class.getProtectionDomain().getCodeSource().getLocation().getPath();
        String folder = path + "/../" + MY_FORMAT.format(date);
        new File(folder).mkdir();

        file = new File(folder + "/" + fileName + ".txt");
        writer = new FileWriter(file, false);
    }

    public void write(String content) {
        try {
            writer = new FileWriter(file, true);
            PrintWriter printer = new PrintWriter(writer);
            printer.write(content);
            printer.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

}
