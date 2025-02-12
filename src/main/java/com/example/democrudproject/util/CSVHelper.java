package com.example.democrudproject.util;

import com.example.democrudproject.model.User;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVRecord;
import org.springframework.web.multipart.MultipartFile;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

public class CSVHelper {
    public static List<User> csvToUsers(MultipartFile file) {
        List<User> users = new ArrayList<>();
        try (BufferedReader fileReader = new BufferedReader(
                new InputStreamReader(file.getInputStream(), StandardCharsets.UTF_8));
             CSVParser csvParser = new CSVParser(fileReader,
                     CSVFormat.DEFAULT.withFirstRecordAsHeader().withIgnoreHeaderCase().withTrim())) {

            Iterable<CSVRecord> csvRecords = csvParser.getRecords();
            for (CSVRecord record : csvRecords) {
                User user = new User(
                        record.get("name"),
                        record.get("surname"),
                        record.get("email"),
                        record.get("address")
                );
                users.add(user);
            }
        } catch (Exception e) {
            throw new RuntimeException("Failed to parse CSV file: " + e.getMessage());
        }
        return users;
    }
}

