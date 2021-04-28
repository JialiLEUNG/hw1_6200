package com.example.cs6200_hw1.DataPreProcess;

public class CosineScoredDoc implements Comparable{
    public String id;
    public Double score;
    public CosineScoredDoc(String _id, Double _score)
    {
        id = _id;
        score = _score;
    }

    public String getId() {
        return id;
    }

    public Double getScore() {
        return score;
    }

    @Override
    public int compareTo(Object o) {
        if(!(o instanceof CosineScoredDoc)) return -1;
        double oScore = ((CosineScoredDoc)o).score;
        if(this.score - oScore < 0) return -1;
        else if(this.score - oScore > 0) return 1;
        return 0;
    }

    @Override
    public String toString()
    {
        return "["+id+": "+score+"]";
    }
}
