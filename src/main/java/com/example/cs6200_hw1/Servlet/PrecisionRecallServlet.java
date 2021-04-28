package com.example.cs6200_hw1.Servlet;

import com.example.cs6200_hw1.Classes.FileConst;
import com.example.cs6200_hw1.Hw1Part2Main;
import com.example.cs6200_hw1.Models.Query;
import com.example.cs6200_hw1.RelevanceEvaluation.PrecisionRecall;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.xml.XmlMapper;

import javax.servlet.*;
import javax.servlet.http.*;
import javax.servlet.annotation.*;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

@WebServlet(name = "PrecisionRecallServlet", value = "/PrecisionRecallServlet")
public class PrecisionRecallServlet extends HttpServlet {
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        getServletContext().getRequestDispatcher("/precisionRecallQuery.jsp").forward(request, response);
        response.setContentType("text/html;charset=UTF-8");

        for (int i = 1; i <= 10; i++){
            PrecisionRecall pr = new PrecisionRecall(i);
            pr.run();
        }

        // double[]: topK, queryNo, precisionAtK, recallAtK, matchedDoc
        Map<String, double[]> precisionRecallMap = new HashMap<>();


    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {

    }
}
