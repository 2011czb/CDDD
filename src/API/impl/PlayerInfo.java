package API.impl;

/**
 * 玩家信息类
 */
public class PlayerInfo {
    private String name;
    private int handSize;
    private boolean isHuman;
    private boolean isCurrent;
    private boolean isPassed;


    // Getters and Setters
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public int getHandSize() { return handSize; }
    public void setHandSize(int handSize) { this.handSize = handSize; }

    public boolean isHuman() { return isHuman; }
    public void setHuman(boolean human) { isHuman = human; }

    public boolean isCurrent() { return isCurrent; }
    public void setCurrent(boolean current) { isCurrent = current; }

    public boolean isPassed() { return isPassed; }
    public void setPassed(boolean passed) { isPassed = passed; }

}
