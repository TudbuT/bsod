package tudbut.bsod;

public class Main {
    
    public static boolean run = true;
    
    public static void main(String[] args) throws InterruptedException {
        ScreenBlocker.blockAll();
        System.out.println("Done");
    }
}
