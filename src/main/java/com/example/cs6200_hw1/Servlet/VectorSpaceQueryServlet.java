package com.example.cs6200_hw1.Servlet;

import com.example.cs6200_hw1.Hw1Part2Main;
import com.example.cs6200_hw2.Hw2Main;

import javax.servlet.*;
import javax.servlet.http.*;
import javax.servlet.annotation.*;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

@MultipartConfig
@WebServlet(name = "VectorSpaceQueryServlet", urlPatterns = "/VectorSpaceQueryServlet")
public class VectorSpaceQueryServlet extends HttpServlet {
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        getServletContext().getRequestDispatcher("/vectorSpaceQuery.jsp").forward(request, response);
        response.setContentType("text/html;charset=UTF-8");

        String query = request.getParameter("query");

        if (query == null) {
            System.out.println("query is not provided");
        } else {
            System.out.println("Query: " + query);
        }
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
         String query = request.getParameter("query");
        List<String> top20docs = new ArrayList<>();
        String error;
         if (query == null){
             error = "Missing query";
             request.setAttribute("error", error);
             doGet(request, response);
         } else {
//             Hw1Part2Main assign1 = new Hw1Part2Main(query, 20);
             Hw2Main hw2 = new Hw2Main(query, 20);
             try {
//                 assign1.run();
                 top20docs = hw2.run();
                 for (String doc : top20docs){
                 }
             } catch (Exception e) {
                 e.printStackTrace();
             }
         }
        response.getWriter().print("query: " + query);
        request.setAttribute("top20docs", top20docs);
        request.getRequestDispatcher("/displayRankElasticsearch.jsp").forward(request, response);
//        getServletContext().getRequestDispatcher("/displayRankVectorSpace.jsp").forward(request, response);
    }
}
