package com.example.testServlet;

import com.example.testServlet.services.DBService;
import com.example.testServlet.services.DefaultDBService;
import com.example.testServlet.services.DefaultFileService;
import com.example.testServlet.services.FileService;

import java.io.*;
import java.util.Map;
import java.util.SplittableRandom;
import javax.servlet.ServletException;
import javax.servlet.http.*;
import javax.servlet.annotation.*;

@WebServlet(name = "helloServlet", value = "/hello-servlet")
public class HelloServlet extends HttpServlet {

    public void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException {
        String pathToDir = "/Users/mac/IdeaProjects/testServlet/src";
        FileService fileService = new DefaultFileService();
        DBService dbService = new DefaultDBService();
//        dbService.fillDB(fileService.getAFDofDir(pathToDir));
        response.setContentType("text/html");
        PrintWriter writer = response.getWriter();
        writer.println("<html><body>");
        writer.println("<h1><th>Alphabetical Frequency Dictionary<th/></h1>");

        writer.println("<table border=\"1\">\n" +
                "   <caption>Table: word = "+request.getParameter("word")+"</caption>\n" +
                "   <tr>\n" +
                "    <th>Count</th>\n" +
                "    <th>Path</th>\n" +
                "   </tr>");
        for (Map.Entry<String, Integer> countPath : dbService.getWordInformation(request.getParameter("word")).entrySet()) {
            writer.println("<tr><td>" + countPath.getValue() + "</td><td>" + countPath.getKey() + "</td></tr>");
        }

        writer.println("</body><a href=index.jsp>Return</a></html>");
    }

    public void destroy() {
    }

    private void reloadTable(String word) {

    }
}