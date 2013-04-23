package scripts;
 
import java.awt.Color;
import java.awt.Graphics;

import org.tribot.api.General;
import org.tribot.api.Timing;
import org.tribot.api.input.Mouse;
import org.tribot.api2007.Banking;
import org.tribot.api2007.Camera;
import org.tribot.api2007.GameTab;
import org.tribot.api2007.Inventory;
import org.tribot.api2007.Objects;
import org.tribot.api2007.Player;
import org.tribot.api2007.types.RSItem;
import org.tribot.api2007.types.RSObject;
import org.tribot.script.Script;
import org.tribot.script.ScriptManifest;
import org.tribot.script.interfaces.Painting;
@ScriptManifest(authors = { "kyang1993" }, 
                category = "Money Making", 
                name = "yChocolate", 
                isEOC = false, 
                version = 1.0,
                description = 
                "Makes chocolate dust from chocolate bars. " + 
                "Have chocolate bars in first line of bank, not on the edge. " +  
                "Start with chocolate bars in inventory and knife in last inventory spot."
               )

public class yChocolate extends Script implements Painting {
    
    private final int[] DONTDROP = {946, 233};
    private final int KNIFE = 946;
    private final int CHOCOLATE_BAR = 1973;
    private final int CHOCOLATE_DUST = 1975;
    private final int[] IDS_BANKBOOTHS = {14367, 25808, 18491};
    private int count = 0;
 
    private State CURRENT_STATE = getState();
    private enum State {
        BANKING, GRINDING;
    }
 
    private State getState() {
        if (isFullOfDust()) {
            return State.BANKING;
        } else {
            return State.GRINDING;
        }
    }
 
    @Override
    public void run() {
        Mouse.setSpeed(General.random(115, 150));
        Camera.setCameraAngle(100);
 
        while (true) {
            CURRENT_STATE = getState();
            switch (CURRENT_STATE) {
                case BANKING:
                    bank();
                    break;
                case GRINDING:
                    grind();
            }
            sleep(40, 80);
        }
    }
 
    private void bank() {
        RSObject[] bankBooths = Objects.findNearest(10, IDS_BANKBOOTHS);
        if (bankBooths.length > 0) {
            bankBooths = Objects.sortByDistance(Player.getPosition(), bankBooths);
            if (bankBooths[0].isOnScreen()) {
                bankBooths[0].click("Bank");
                sleep(200, 500);
                Banking.depositAllExcept(DONTDROP);
                Banking.withdraw(0, CHOCOLATE_BAR);
                count = count + 27;
                Banking.close();
                sleep(500, 1000);
            } else {
                Camera.turnToTile((bankBooths[0].getPosition()));
            }
        } else {
            println("no bank found");
        }
    }
 
    private void grind() {
        GameTab.open(GameTab.TABS.INVENTORY);
        RSItem[] bars = Inventory.find(CHOCOLATE_BAR);
        if (bars.length == 27) {
            while(Inventory.find(CHOCOLATE_BAR).length > 0) {
                Mouse.clickBox(695, 436, 712, 451, 1);
                sleep(30, 70);
                Mouse.clickBox(654, 439, 672, 452, 1);
                sleep(30, 70);
            }
        }
    }

    private boolean isFullOfDust() {
        RSItem[] dusts = Inventory.find(CHOCOLATE_DUST);
        return dusts.length == 27;
    }
 
    // PAINT 
    @Override
    public void onPaint(Graphics g) {
        double dustPerMs = (double)count / (double)getRunningTime();
        
        
        g.setColor(Color.WHITE);
        g.drawString("yChocolate v1.0 by kyang1993", 10, 70); 
        g.drawString("State: " + CURRENT_STATE, 10, 90);
        g.drawString("Runtime: " + Timing.msToString(getRunningTime()), 10, 110);
        g.drawString("Dusts Made: " + count, 10, 130);
        g.drawString("Dusts/Hour: " + String.valueOf((int)(dustPerMs * 3600000)), 10, 150);
        g.drawString("GP/Hour: " + String.valueOf((int)(dustPerMs * 630000000)), 10, 170);
        sleep(20);
    }
}