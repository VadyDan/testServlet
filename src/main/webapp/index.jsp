<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<!DOCTYPE html>
<html>
<head>
    <meta charset="UTF-8">
    <title>JSP - Hello World</title>
</head>
<body>
<h1><%= "Hello World! This is Alphabetical Frequency Dictionary Servlet!" %>
</h1>
<form action="hello-servlet">
    Word: <input name="word" />
    <br><br>
    <input type="submit" value="Find in DataBase" />
</form>
</body>
</html>