/*
 * ------------------------------------------------------------------------------------------------
 * Copyright 2014 by Swiss Post, Information Technology Services
 * ------------------------------------------------------------------------------------------------
 * $Id$
 * ------------------------------------------------------------------------------------------------
 */

package ch.post.it.paisa.wikkit.web;

import java.io.File;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import com.fasterxml.jackson.databind.ObjectMapper;

/**
 * Created by niederhauste on 20.10.2016.
 */
public class Csv2Json {
    public static void main(String[] args) throws IOException {
        Map<String, List<Double>> data = new HashMap<>();
        for (String line : Files.readAllLines(new File("C:/Users/niederhauste/Downloads/je-d-21.03.01.csv").toPath(), Charset.forName("iso-8859-1"))) {
            final String[] values = line.split(";");
            List<Double> datum = new ArrayList<>();
            for (int i = 2; i < values.length; i++) {
                datum.add(values[i].equals("*") ? null : Double.parseDouble(values[i].replace("'", "")));
            }
            data.put(values[0], datum);
        }
        final ObjectMapper mapper = new ObjectMapper();
        mapper.writeValue(new File("data.json"), data);
    }
}
