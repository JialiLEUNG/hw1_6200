<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<html>
<head>
    <title>Query Form</title>
</head>
<body>
<form action = "/VectorSpaceQueryServlet" method = "post"
      enctype = "multipart/form-data">
    <h3>Enter Your Query:</h3>
    <input type="text" name ="query" id="query">
    <br />
    <button type= "submit" value ="Submit" >Submit</button>
</form>

</body>
</html>
