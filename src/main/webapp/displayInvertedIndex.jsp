<%@ page import="java.io.BufferedReader" %>
<%@ page import="java.io.FileReader" %>
<%@ page import="com.example.cs6200_hw1.DataPreProcess.FilePathGenerator" %>
<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<html>
<head>
    <meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
    <title>Inverted Index</title>
</head>
<body>
<%
    FilePathGenerator fpg = new FilePathGenerator(".ridx_docId");
    String path = fpg.getPath();
    BufferedReader reader = new BufferedReader(new FileReader(path));
    String line;

    while((line = reader.readLine())!= null){
        out.println(line + "<br>");
    }
    reader.close();
%>


</body>
</html>
