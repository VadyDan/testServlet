package com.example.testServlet.services;

import java.util.HashMap;
import java.util.LinkedHashMap;

public interface DBService {
    void fillDB(HashMap<String, HashMap<String, Integer>> wordsPathsCounts);
    LinkedHashMap<String, Integer> getWordInformation(String word);
}
