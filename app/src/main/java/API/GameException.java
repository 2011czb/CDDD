package API;

/**
 * 游戏异常类
 * 用于处理游戏过程中的异常情况
 */
public class GameException extends RuntimeException {
    public GameException(String message) {
        super(message);
    }

    public GameException(String message, Throwable cause) {
        super(message, cause);
    }
}
