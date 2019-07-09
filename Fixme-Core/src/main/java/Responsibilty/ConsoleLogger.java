package Responsibilty;

public class ConsoleLogger extends AbstractLogger {
    public static final String ANSI_RESET = "\u001B[0m";
    public static final String ANSI_BLACK = "\u001B[30m";
    public static final String ANSI_RED = "\u001B[31m";
    public static final String ANSI_GREEN = "\u001B[32m";
    public static final String ANSI_YELLOW = "\u001B[33m";
    public static final String ANSI_BLUE = "\u001B[34m";
    public static final String ANSI_PURPLE = "\u001B[35m";
    public static final String ANSI_CYAN = "\u001B[36m";
    public static final String ANSI_WHITE = "\u001B[37m";

    public ConsoleLogger(int level){
        this.level = level;
    }

    @Override
    protected void write(String message) {
        System.out.println("levle"+ this.level);
        switch (this.level)
        {
            case 1 :
                System.out.println("["+ANSI_BLUE+"INFO"+ANSI_RESET +"] " + message);
                break;

            case 2 :
                System.out.println("["+ANSI_CYAN+"DEBUG"+ANSI_RESET +"] " + message);
                break;

            case 3:
                System.out.println("["+ANSI_RED+"ERROR"+ANSI_RESET +"] " + message);
                break;

            default:
                System.out.println("["+ANSI_YELLOW+"WARNING"+ANSI_RESET +"] " + message);
                break;

        }
    }
}