<%@ page import="java.util.List" %>
<%@ page import="java.util.ArrayList" %><%--
  Created by IntelliJ IDEA.
  User: jialiliang
  Date: 3/11/21
  Time: 11:19 AM
  To change this template use File | Settings | File Templates.
--%>
<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<html>
<head>
    <title>Doc Ranking By Elasticsearch</title>
</head>
<body>
<%
    List<String> top20docs = (ArrayList<String>) request.getAttribute("top20docs");
   for (String docId : top20docs)
       out.println("[ " + docId + " ] " + "<br>");


%>
</body>
</html>
