package API.impl;

/**
 * 游戏操作响应类
 */
public class GameResponse<T> {
    private final boolean success;
    private final String message;
    private final T data;
    private final boolean gameEnded;
    private final String winner;

    public GameResponse(boolean success, String message, T data, boolean gameEnded, String winner) {
        this.success = success;
        this.message = message;
        this.data = data;
        this.gameEnded = gameEnded;
        this.winner = winner;
    }

    // Getters
    public boolean isSuccess() { return success; }
    public String getMessage() { return message; }
    public T getData() { return data; }
    public boolean isGameEnded() { return gameEnded; }
    public String getWinner() { return winner; }
}
