package com.example.testServlet.services;

import java.io.File;
import java.io.FileNotFoundException;
import java.nio.file.Files;
import java.util.*;
import java.util.concurrent.*;

public class DefaultFileService implements FileService {

    @Override
    public List<String> getPathsToTextFiles(String pathToDir) {
        List<String> textFilePaths = new LinkedList<>();
        Stack<File> folderStack = new Stack<>();
        folderStack.push(new File(pathToDir));
        while (!folderStack.isEmpty()) {
            File child = folderStack.pop();
            File[] listFiles = child.listFiles();
            if (listFiles != null) {
                for (File file : listFiles) {
                    try {
                        String fileContentType = Files.probeContentType(file.toPath());
                        if (file.isDirectory()) {
                            folderStack.push(file);
                        } else if (fileContentType != null && fileContentType.startsWith("text/plain") && file.getName().endsWith(".txt")) {
                            textFilePaths.add(file.getCanonicalPath());
                        }
                    } catch (Exception ex) {
                        ex.printStackTrace();
                    }
                }
            }
        }
        return textFilePaths;
    }

    @Override
    public HashMap<String, HashMap<String, Integer>> getAFDofDir(String pathToDir) {
        // Получаем количество логических потоков (пригодится, если захотим использовать newFixedThreadPool())
        int cores = Runtime.getRuntime().availableProcessors();
        // Получаем пути к всем текстовым файлам внутри заданной директории
        List<String> pathsToTextFiles = getPathsToTextFiles(pathToDir);
        // Итоговая структура данных, по сути представляющая собо АЧС
        HashMap<String, HashMap<String, Integer>> wordsPathsCounts = new HashMap<>();
        // Средство управления потоками
        ExecutorService executorService = Executors.newCachedThreadPool();
        try {
            // Список задач, которые и необходимо выполнять в параллельном режиме
            List<AFDofFileParallel> tasks = new ArrayList<>();
            pathsToTextFiles.forEach(path -> tasks.add(new AFDofFileParallel(path)));
            // Запускаем пул потоков и ждем
            List<Future<Map<String, Integer>>> results = executorService.invokeAll(tasks);
            int i = 0;
            while (!results.isEmpty()) {
                Future<Map<String, Integer>> result = results.get(i);
                if (result.isDone()) {
                    for (Map.Entry<String, Integer> wordAndCount : result.get().entrySet()) {
                        HashMap<String, Integer> pathAndCount;
                        if (wordsPathsCounts.containsKey(wordAndCount.getKey())) {
                            pathAndCount = wordsPathsCounts.get(wordAndCount.getKey());
                        } else {
                            pathAndCount = new HashMap<>();
                        }
                        pathAndCount.put(pathsToTextFiles.get(i), wordAndCount.getValue());
                        wordsPathsCounts.put(wordAndCount.getKey(), pathAndCount);
                    }
                    results.remove(i);
                    pathsToTextFiles.remove(i);
                    i = 0;
                } else {
                    i = (i+1)>=results.size()?0:i+1;
                }
            }
        } catch (InterruptedException | ExecutionException e) {
            e.printStackTrace();
        } finally {
            executorService.shutdown();
        }
        return wordsPathsCounts;
    }

    // Класс, реализующий интерфейс Callable – для организации многопоточности
    private static class AFDofFileParallel implements Callable<Map<String, Integer>> {
        String pathToFile;

        public AFDofFileParallel(String pathToFile) {
            this.pathToFile = pathToFile;
        }

        @Override
        public Map<String, Integer> call() throws Exception {
            Map<String, Integer> wordsAndCounts = new HashMap<>();
            Scanner sc = null;
            try {
                sc = new Scanner(new File(pathToFile));
                while (sc.hasNext()) {
                    String word = sc.next();
                    word = word.toLowerCase();
                    while (word.length() > 0 && !Character.isLetter(word.toCharArray()[word.length()-1]))
                        word = word.substring(0, word.length() - 1);
                    if (word.length() > 0)
                        if (!wordsAndCounts.containsKey(word))
                            wordsAndCounts.put(word, 1);
                        else
                            wordsAndCounts.put(word, wordsAndCounts.get(word) + 1);
                }
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            }
            return wordsAndCounts;
        }
    }
}
