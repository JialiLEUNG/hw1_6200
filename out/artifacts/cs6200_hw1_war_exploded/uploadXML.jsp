<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<html>
<head>
    <title>Uploading XML File</title>
</head>
<body>
    <h3>File Upload:</h3>
    Select an XML file to upload: <br />
    <form action = "/UploadXmlServlet" method = "post"
      enctype = "multipart/form-data">
        <input type = "file" name = "file" size = "50" />
        <br />
        <input type = "submit" value = "Submit" />
    </form>

</body>
</html>
