package it.unipi.dii.inginf.lsmsdb.mapsproject;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

public class PropertyPicker {

    Properties properties = new Properties();
    final String fileName = "/application.properties";

    public PropertyPicker() throws FileNotFoundException {

        try (FileInputStream fis = new FileInputStream(fileName)) {
            properties.load(fis);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public String getProperty(String propertyName){
        return this.properties.getProperty(propertyName);
    }
}
