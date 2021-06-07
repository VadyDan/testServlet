package com.example.testServlet.services;

import java.io.FileNotFoundException;
import java.util.HashMap;
import java.util.List;

public interface FileService {
    List<String> getPathsToTextFiles(String pathToDir);
    HashMap<String, HashMap<String, Integer>> getAFDofDir(String pathToDir);
}
