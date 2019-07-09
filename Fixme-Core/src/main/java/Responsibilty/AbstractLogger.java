package Responsibilty;


//https://www.tutorialspoint.com/design_pattern/chain_of_responsibility_pattern.htm
public abstract class AbstractLogger {
    public static int INFO = 1;
    public static int DEBUG = 2;
    public static int ERROR = 3;

    protected int level;

    //next element in chain or responsibility
    protected AbstractLogger nextLogger;

    public void setNextLogger(AbstractLogger nextLogger){
        this.nextLogger = nextLogger;
    }

    public void logMessage(int level, String message){

        setLevel(level);
        if(nextLogger !=null){
            nextLogger.logMessage(level, message);
        } else {
            write(message);
        }
    }

    abstract protected void write(String message);

    public AbstractLogger setLevel(int level) {
        this.level = level;
        return this;
    }
}