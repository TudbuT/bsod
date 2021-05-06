package tudbut.bsod;

import de.tudbut.tools.Keyboard;
import de.tudbut.tools.Tools;
import de.tudbut.ui.windowgui.FontRenderer;
import tudbut.parsing.TCN;
import tudbut.parsing.TCNArray;
import tudbut.rendering.Maths2D;
import tudbut.tools.ReflectUtil;

import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.*;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;

public class ScreenBlocker implements KeyListener {
    
    // Aligned variables are bad style, but this would otherwise be unreadable
    public static int       bgc = Integer.parseInt(Config.config.getString("bgc"), 16);
    public static int       fgc = Integer.parseInt(Config.config.getString("fgc"), 16);
    public static TCN       fnt = Config.config.getSub("fnt");
    public static String    sml = Config.config.getString("sml");
    public static String    txt ;
    public static String    hlp ;
    public static String    stc = Config.config.getString("stc");
    public static boolean   sqr = Config.config.getString("sqr").equalsIgnoreCase("y");
    public static boolean   ngt = Config.config.getString("ngt").equalsIgnoreCase("y");
    public static int       max = Config.config.getInteger("max");
    public static int       stp = Config.config.getInteger("stp");
    public static TCNArray  dly = Config.config.getArray("dly");
    // This is part of the variable initializer.
    ; static {
        // Join txt array
        txt = "";
        for (Object o : Config.config.getArray("txt")) {
            txt += o.toString() + "\n";
        }
        // Join hlp array
        hlp = "";
        for (Object o : Config.config.getArray("hlp")) {
            hlp += o.toString() + "\n";
        }
        // Replace $osn
        txt = txt.replaceAll("\\$osn", System.getProperty("os.name"));
        hlp = hlp.replaceAll("\\$osn", System.getProperty("os.name"));
    }
    

    // Instance used for listening for keyboard events
    private static final ScreenBlocker INSTANCE = new ScreenBlocker();
    // Frames that this program has control over
    private static final ArrayList<Window> frames = new ArrayList<>();
    // Robot for moving mouse
    public static final Robot ROBOT;
    // X and Y of grabbed mouse
    private static int grabX = 0;
    private static int grabY = 0;
    // Should grab mouse?
    private static boolean grabMouse = false;

    // Assign robot
    ; static {
        Robot r;
        try {
            r = new Robot();
        }
        catch (Exception ignored) {
            r = null;
        }
        ROBOT = r;
    }

    // Start threads and frame to grab keyboard & mouse
    ; static {
        Keyboard.addListener(INSTANCE);
        JFrame keyboardGrab = new JFrame();
        keyboardGrab.setSize(1,1);
        keyboardGrab.setLocation(0,0);
        keyboardGrab.setUndecorated(true);
        
        // Set opacity to almost invisible, if possible
        try {
            keyboardGrab.setOpacity(0.01f);
        } catch(Exception ignored) {}
        // Hide in taskbar
        keyboardGrab.setType(Window.Type.POPUP);
        
        keyboardGrab.setVisible(true);
        keyboardGrab.setDefaultCloseOperation(WindowConstants.DO_NOTHING_ON_CLOSE);
        
        // We still want events
        Keyboard.startListening(keyboardGrab);
        
        initFrame(keyboardGrab);
        
        // Start grabber thread
        new Thread(() -> {
            try {
                while (Main.run) {
                    if (grabMouse) {
                        // Focus keyboard grab window, otherwise we won't grab keyboard
                        keyboardGrab.setFocusable(true);
                        keyboardGrab.requestFocus();
                        keyboardGrab.setVisible(true);
                        keyboardGrab.setAutoRequestFocus(true);
                        keyboardGrab.requestFocusInWindow();
                        
                        // Grab mouse
                        ROBOT.mouseMove(grabX, grabY);
                    }
                    // Ungrab keyboard if it is still grabbed
                    else
                        keyboardGrab.setVisible(false);
                }
                
                // End
                keyboardGrab.setVisible(false);
                keyboardGrab.dispose();
            } catch(Exception ignore) {
                System.out.println("Couldn't grab mouse.");
            }
        }).start();
    }
    
    // Set invisible cursor
    private static void initFrame(Window frame) {
        BufferedImage cursor = new BufferedImage(1, 1, BufferedImage.TYPE_INT_ARGB);
        cursor.setRGB(0,0, 0x00000000);
        frame.setCursor(Toolkit.getDefaultToolkit().createCustomCursor(cursor, new Point(0, 0), "emptiness"));
    }
    
    // Show BSOD
    public static void blockAll() {
        try {
            grab();
            for (GraphicsDevice it : GraphicsEnvironment.getLocalGraphicsEnvironment().getScreenDevices()) {
                start(it);
            }
        } catch (Exception ignored) {
            System.out.println("Couldn't black screens.");
        }
    }
    
    // Hide BSOD, doesnt ungrab mouse
    public static void unBlockAll() {
        try {
            for (Window it : frames) {
                it.dispose();
                it.setVisible(false);
            }
            frames.clear();
        } catch (Exception ignored) {
            System.out.println("Couldn't unblack screens.");
        }
    }

    // Shows a BSOD on a screen
    private static void start(GraphicsDevice device) {
        // The frame
        AtomicReference<Window> frame = new AtomicReference<>();
        // Progress display
        AtomicInteger p = new AtomicInteger();
        
        frame.set(new Window(null, null) {
            BufferedImage image;

            @Override
            public void paint(Graphics g) {
                // For faster drawing, always use images
                if(image != null)
                    g.drawImage(image, 0, 0, null);
                // For faster drawing, always use images
                image = new BufferedImage(frame.get().getBounds().width, frame.get().getBounds().height, BufferedImage.TYPE_INT_ARGB);
                Graphics graphics = image.getGraphics();
    
                // X and Y to draw elements at
                int x = 150;
                int y = 125;
                
                // Draw background
                graphics.setColor(new Color(0xff000000 + bgc));
                graphics.fillRect(0, 0, frame.get().getBounds().width, frame.get().getBounds().height);
                
                // Foreground:
                graphics.setColor(new Color(fgc));
                // We need newlines in our text
                FontRenderer renderer = new FontRenderer(18);
                // Hacky way to set the font of a FontRenderer
                ReflectUtil.setPrivateFieldByTypeIndex(FontRenderer.class, renderer, Font.class, 0, new Font(fnt.getSub("sml").getString("ffm"), Font.PLAIN, fnt.getSub("sml").getInteger("siz")));
                // Draw smiley/emoji
                graphics.drawImage(renderer.renderText(sml, 0xff000000 + fgc), x, y, null);
                // Increment y for next draw
                y+=fnt.getSub("sml").getInteger("siz");
                y+=50;
                // Change font
                ReflectUtil.setPrivateFieldByTypeIndex(FontRenderer.class, renderer, Font.class, 0, new Font(fnt.getSub("txt").getString("ffm"), Font.PLAIN, fnt.getSub("txt").getInteger("siz")));
                // Draw main text and progress
                graphics.drawImage(renderer.renderText(
                        "" +
                                txt +
                                "\n" +
                                p + "% complete",
                        0xff000000 + fgc
                ), x, y, null);
                // Increment y for next draw
                y+=(txt.split("\n").length+2)*(fnt.getSub("txt").getInteger("siz") + 5);
                y+=50;
                // Draw qr.png if needed
                if(sqr)
                    try {
                        // Draw qr.png
                        graphics.drawImage(
                                // Scale the image to 100x100
                                Maths2D.distortImage(
                                        ImageIO.read(Objects.requireNonNull(ClassLoader.getSystemResourceAsStream("qr.png"))),
                                        100, 100,
                                        // Multiplier, not needed.
                                        1
                                ), x, y + 5, null
                        );
                        // Increment x for next draw, leave y the same
                        x += 120;
                    }
                    catch (IOException e) {
                        System.out.println("Resource not found");
                    }
                // Change font
                ReflectUtil.setPrivateFieldByTypeIndex(FontRenderer.class, renderer, Font.class, 0, new Font(fnt.getSub("hlp").getString("ffm"), Font.PLAIN, fnt.getSub("hlp").getInteger("siz")));
                // Draw help text
                graphics.drawImage(renderer.renderText(
                        "" +
                                hlp.replaceAll("\\$stc", stc),
                                0xff000000 + fgc
                ), x, y, null);
                
                // Draw full image to screen
                g.drawImage(image, 0, 0, null);
            }
        });
        // Initialization
        frame.get().requestFocus();
        frame.get().setAlwaysOnTop(true);
        frame.get().setBackground(new Color(0xff000000 + bgc));
        initFrame(frame.get());
        frame.get().setVisible(true);
        device.setFullScreenWindow(frame.get());
        
        // Progress counter
        new Thread(() -> {
            for (int i = stp; i <= max; i+=stp) {
                try {
                    Thread.sleep(dly.getInteger(0));
                }
                catch (InterruptedException e) {
                    e.printStackTrace();
                }
                p.set(i);
                frame.get().repaint();
                try {
                    Thread.sleep(dly.getInteger(1));
                }
                catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
            while(ngt && Main.run) {
                try {
                    Thread.sleep(dly.getInteger(2));
                }
                catch (InterruptedException e) {
                    e.printStackTrace();
                }
                p.addAndGet(-stp);
                frame.get().repaint();
                try {
                    Thread.sleep(dly.getInteger(3));
                }
                catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }).start();
        
        frames.add(frame.get());
    }

    // Start grabbing mouse and keyboard
    public static void grab() {
        grabMouse = true;
        grabX = MouseInfo.getPointerInfo().getLocation().x;
        grabY = MouseInfo.getPointerInfo().getLocation().y;
    }
    
    public static void release() {
        grabMouse = false;
    }

    @Override
    public void keyTyped(KeyEvent keyEvent) {}

    @Override
    public void keyPressed(KeyEvent keyEvent) {
        // Unblock hotkey
        if(keyEvent.getKeyCode() == KeyEvent.VK_F10 && Keyboard.isKeyDown(KeyEvent.VK_ESCAPE)) {
            unBlockAll();
            release();
            Main.run = false;
            System.exit(0);
        }
    }

    @Override
    public void keyReleased(KeyEvent keyEvent) {}
}
