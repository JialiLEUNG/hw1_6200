package com.example.cs6200_hw1.Servlet;

import com.example.cs6200_hw1.DataPreProcess.FilePathGenerator;
import com.example.cs6200_hw1.Hw1Part1Main;
import com.example.cs6200_hw1.Hw1Part2Main;

import javax.servlet.*;
import javax.servlet.http.*;
import javax.servlet.annotation.*;
import java.io.*;

/**
 * File upload using jsp and servlet
 */

@MultipartConfig
@WebServlet(name = "UploadXmlServlet", urlPatterns = "/UploadXmlServlet")
public class UploadXmlServlet extends HttpServlet {
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        getServletContext().getRequestDispatcher("/uploadXML.jsp").forward(request, response);
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        response.setContentType("text/html;charset=UTF-8");

        Part filePart = request.getPart("file");
        String fileName = filePart.getSubmittedFileName();
        for (Part part : request.getParts()){
            // save uploaded file to local disk
            FilePathGenerator fpg = new FilePathGenerator(fileName);
            String path = fpg.getPath();
            part.write(path);
            Hw1Part1Main hw1 = new Hw1Part1Main(path);
            try {
                hw1.run();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        response.getWriter().print("File uploaded successfully.");
        getServletContext().getRequestDispatcher("/displayInvertedIndex.jsp").forward(request, response);

    }


    protected void processRequest(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        response.setContentType("text/html;charset=UTF-8");
        try (PrintWriter out = response.getWriter()){
            // fetch data
            Part part = request.getPart("file");
            String fileName = part.getSubmittedFileName();
            System.out.println(fileName);
        }

    }
}
