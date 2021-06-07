package com.example.testServlet.services;

import java.sql.*;
import java.util.*;

public class DefaultDBService implements DBService {
    String url = "jdbc:postgresql://localhost/AFD_JAVA";

    @Override
    public void fillDB(HashMap<String, HashMap<String, Integer>> wordsPathsCounts) {
        boolean isWordExists, isPathExist, isCountExist;
        Connection con = null;
        PreparedStatement stm = null;
        Properties props = new Properties();
        props.setProperty("user", "postgres");
        props.setProperty("password", "28");
        String SQL_insert_word = "INSERT INTO words(value) " + "VALUES(?)";
        String SQL_insert_file_path = "INSERT INTO files(path) " + "VALUES(?)";
        String SQL_insert_count = "INSERT INTO files_words(count,files_id,words_id) " + "VALUES(?," +
                "(select id from files where path = ?)," +
                "(select id from words where value = ?))";
        String SQL_insert_word_check_exist = "select 1 from words where value=?";
        String SQL_insert_path_check_exist = "select 1 from files where path=?";
        String SQL_insert_count_check_exist = "select 1 from files_words where " +
                "files_id=(select id from files where path = ?)" +
                " and words_id=(select id from words where value = ?)";
        try {
            Class.forName("org.postgresql.Driver");
            con = DriverManager.getConnection (url, props);
            for (Map.Entry<String, HashMap<String, Integer>> wordPathCount : wordsPathsCounts.entrySet()) {
                isWordExists = false;
                // Проверяем, есть ли полученное слово в БД
                try (PreparedStatement ps = con.prepareStatement(SQL_insert_word_check_exist)) {
                    ps.setString(1, wordPathCount.getKey());
                    try (ResultSet rs = ps.executeQuery()) {
                        if (rs.next()) {
                            isWordExists = true;
                        }
                    }
                }
                // Если нет, то добавляем новое слово в таблицу
                if (!isWordExists) {
                    stm = con.prepareStatement(SQL_insert_word);
                    stm.setString(1, wordPathCount.getKey());
                    stm.executeUpdate();
                }
                for (Map.Entry<String, Integer> pathAndCount : wordsPathsCounts.get(wordPathCount.getKey()).entrySet()) {
                    isPathExist = false;
                    // Проверяем, есть ли такой путь в БД
                    try (PreparedStatement ps = con.prepareStatement(SQL_insert_path_check_exist)) {
                        ps.setString(1, pathAndCount.getKey());
                        try (ResultSet rs = ps.executeQuery()) {
                            if (rs.next()) {
                                isPathExist = true;
                            }
                        }
                    }
                    // Если нет, то добавляем новое слово в таблицу
                    if (!isPathExist) {
                        stm = con.prepareStatement(SQL_insert_file_path);
                        stm.setString(1, pathAndCount.getKey());
                        stm.executeUpdate();
                    }

                    isCountExist = false;
                    // Проверяем, есть ли такой путь в БД
                    try (PreparedStatement ps = con.prepareStatement(SQL_insert_count_check_exist)) {
                        ps.setString(1, pathAndCount.getKey());
                        ps.setString(2, wordPathCount.getKey());
                        try (ResultSet rs = ps.executeQuery()) {
                            if (rs.next()) {
                                isCountExist = true;
                            }
                        }
                    }

                    // Заполняем таблицу files_words с числом повторений каждого слова в разных документах
                    if (!isCountExist) {
                        stm = con.prepareStatement(SQL_insert_count);
                        stm.setInt(1, pathAndCount.getValue());
                        stm.setString(2, pathAndCount.getKey());
                        stm.setString(3, wordPathCount.getKey());
                        stm.executeUpdate();
                    }
                }
            }
        } catch (SQLException | ClassNotFoundException e) {
            e.printStackTrace();
        } finally {
            if (stm != null) {
                try {
                    stm.close();
                } catch (SQLException throwables) {
                    throwables.printStackTrace();
                }
            }
            if (con != null) {
                try {
                    con.close();
                } catch (SQLException throwables) {
                    throwables.printStackTrace();
                }
            }
        }
    }

    @Override
    public LinkedHashMap<String, Integer> getWordInformation(String word) {
        Connection con = null;
        PreparedStatement stm = null;
        LinkedHashMap<String, Integer> result = new LinkedHashMap<>();
        Properties props = new Properties();
        props.setProperty("user", "postgres");
        props.setProperty("password", "28");
        String SQL_get_counts_and_path_id = "select count, files_id from files_words where words_id = " +
                "(select id from words where value = ?) order by count DESC";
        String SQL_get_path = "select path from files where id = ?";
        try {
            Class.forName("org.postgresql.Driver");
            con = DriverManager.getConnection(url, props);
            stm = con.prepareStatement(SQL_get_counts_and_path_id);
            stm.setString(1, word);
            ResultSet rs = stm.executeQuery();
            while (rs.next()) {
                stm = con.prepareStatement(SQL_get_path);
                stm.setInt(1, rs.getInt("files_id"));
                ResultSet rs1 = stm.executeQuery();
                rs1.next();
                result.put(rs1.getString("path"), rs.getInt("count"));
            }
        } catch (SQLException | ClassNotFoundException throwables) {
            throwables.printStackTrace();
        } finally {
            if (stm != null) {
                try {
                    stm.close();
                } catch (SQLException throwables) {
                    throwables.printStackTrace();
                }
            }
            if (con != null) {
                try {
                    con.close();
                } catch (SQLException throwables) {
                    throwables.printStackTrace();
                }
            }
        }
        return result;
    }
}
