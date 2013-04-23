package scripts;

import java.awt.Color;
import java.awt.Graphics;

import org.tribot.api.General;
import org.tribot.api.Timing;
import org.tribot.api.input.Mouse;
import org.tribot.api2007.GroundItems;
import org.tribot.api2007.Inventory;
import org.tribot.api2007.Objects;
import org.tribot.api2007.Player;
import org.tribot.api2007.Skills;
import org.tribot.api2007.Walking;
import org.tribot.api2007.types.RSGroundItem;
import org.tribot.api2007.types.RSObject;
import org.tribot.api2007.types.RSTile;
import org.tribot.script.Script;
import org.tribot.script.ScriptManifest;
import org.tribot.script.interfaces.Painting;

@ScriptManifest(authors  =  { "kyang1993" }, 
                category =  "Mining", 
                name     =  "yMiner")


public class yMiner extends Script implements Painting {
    
    RSTimer time = new RSTimer(3000);

    // CONSTANT IDS
    private int startXP;
    private final int ROCK_ID = 11962;
    private final int[] PICKAXES = {1265};
    private final int[] AXEHEADS = {480, 482, 484, 486, 488, 490};
    private final RSTile[] FLEE_PATH = {new RSTile(3293, 3376, 0)};
    private final RSTile[] RETURN_PATH = {new RSTile(3286, 3361, 0)};
    private final String skill = "Mining";
    
    // STATES
    private enum State {
        MINING, DROPPING, FINDING, FLEEING
    }
    private State CURRENT_STATE = getState();
    private State getState() {
        if (Player.getRSPlayer().isInCombat()) {
            println("Oh no, we're dying! Run!");
            return State.FLEEING;
        } else if (Inventory.getCount(466) > 0) {
            println("Oh no, we lost our pickaxe head!");
            return State.FINDING;
        } else if (Inventory.isFull()) {
            return State.DROPPING;
        } else {
            return State.MINING;
        }
    }
    
    // MAIN METHOD, PAINT, AND HELPERS
    @Override
    // Runs the script
    public void run() {
        // Initialize
        println("Starting yMiner!");
        Mouse.setSpeed(General.random(180, 240));
        startXP = Skills.getXP(skill);
        
        // Main Loop
        while(true) {
            CURRENT_STATE = getState();
            switch(CURRENT_STATE) {
                case FLEEING:
                    flee();
                    break;
                case FINDING:
                    find();
                    break;
                case DROPPING:
                    drop();
                    break;
                case MINING:
                    mine();
                    break;
            }
            sleep(40,80);
        }
    }
    
    private void drop() {
        Inventory.dropAllExcept(PICKAXES);
    }

    private void find() {
        RSGroundItem[] axehead = GroundItems.findNearest(AXEHEADS);
        axehead[0].click("Take");
        println("We found the pickaxe head!");
        sleep(1000, 3000);  
    }

    private void flee() {
        Walking.walkPath(FLEE_PATH);
        waitNotMoving();
        sleep(3000, 5000);
        Walking.walkPath(RETURN_PATH);
        waitNotMoving();
        sleep(2000, 3000);
        
    }
    
    private void mine() {
        if(Player.getRSPlayer().getAnimation() == -1) {
            RSObject[] rock = Objects.findNearest(10, ROCK_ID);
            if(rock.length > 0) {
                if(rock[0].isOnScreen()) {
                    rock[0].click("mine");
                    time.reset();
                    while(Player.getRSPlayer().getAnimation() == -1 && 
                          time.isRunning()) {
                        sleep(10);
                    }
                }
            }
        }
    }

    private void waitNotMoving() {
        sleep(100, 200);
        while (Player.isMoving()) {
            sleep(50, 100);
        }
    }
    
    @Override
    // PAINT
    public void onPaint(Graphics g) {
        int xpGained = Skills.getXP(skill) - startXP;
        double xpPerMs = (double)xpGained / (double)getRunningTime();
        long timeToLevel = (long)(Skills.getXPToNextLevel(skill) / xpPerMs);
       
        g.setColor(Color.WHITE);
        g.drawString("yMiner v1.0 by kyang1993", 10, 70); 
        g.drawString("State: " + CURRENT_STATE, 10, 90);
        g.drawString("Runtime: " + Timing.msToString(getRunningTime()), 10, 110);
        g.drawString("XP Gained: " + xpGained, 10, 130);
        g.drawString("XP/Hour: " + String.valueOf((int)(xpPerMs * 3600000)), 10, 150);
        g.drawString("Next level: " + Timing.msToString(timeToLevel), 10, 170);
        sleep(20);
    }
}
