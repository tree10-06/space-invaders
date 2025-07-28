package gdd;

public class Score {
    private static Score instance;
    private int currentScore;

    private Score() {
        currentScore = 0;
    }

    public static Score getInstance() {
        if (instance == null) {
            instance = new Score();
        }
        return instance;
    }

    public void addScore(int points) {
        currentScore += points;
    }

    public int getScore() {
        return currentScore;
    }

    public void resetScore() {
        currentScore = 0;
    }
}