package com.andriyklus.dota2.filemanager;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;

public class FileManager {

    private static final String FILE_PATH = "C:\\Users\\пк\\Desktop\\dota2\\dota2\\src\\main\\resources\\LastVideoHeader.txt";

    public static String getLastVideoHeader() {
        StringBuilder content = new StringBuilder();

        try (BufferedReader reader = new BufferedReader(new FileReader(FILE_PATH))) {
            String line;
            while ((line = reader.readLine()) != null) {
                content.append(line).append("\n");
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        if (content.length() > 0 && content.charAt(content.length() - 1) == '\n')
            return content.substring(0, content.length() - 1);
        return content.toString();
    }

    public static void writeLastHeaderToTheFile(String header) {
        try (FileWriter writer = new FileWriter(FILE_PATH)) {
            writer.write(header);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

}
