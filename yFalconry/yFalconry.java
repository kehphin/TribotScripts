package scripts;

import java.awt.Color;
import java.awt.Graphics;

import org.tribot.api.DynamicClicking;
import org.tribot.api.General;
import org.tribot.api.Timing;
import org.tribot.api.input.Mouse;
import org.tribot.api2007.Camera;
import org.tribot.api2007.Inventory;
import org.tribot.api2007.NPCs;
import org.tribot.api2007.Player;
import org.tribot.api2007.Skills;
import org.tribot.api2007.types.RSNPC;
import org.tribot.script.Script;
import org.tribot.script.ScriptManifest;
import org.tribot.script.interfaces.Painting;

@ScriptManifest(authors  =  { "kyang1993" }, 
                category =  "Hunter", 
                name     =  "yFalconry")


public class yFalconry extends Script implements Painting {

    // CONSTANT IDS
    private int startXP;
    private final int[] ITEMS = {1265, 995, 563, 772, 557, 556};
    private final String skill = "Hunter";

    // STATES
    private enum State {
        RELEASING, DROPPING, RETRIEVING
    }
    private State CURRENT_STATE = getState();
    private State getState() {
        if (Inventory.getAll().length > 26) {
            return State.DROPPING;
        } else if (hasCaught()) {
            return State.RETRIEVING;
        } else {
            return State.RELEASING;
        } 
    }

    // MAIN METHOD, PAINT, AND HELPERS
    @Override
    // Runs the script
    public void run() {
        // Initialize
        println("Starting yFalconry!");
        Mouse.setSpeed(General.random(180, 240));
        startXP = Skills.getXP(skill);

        // Main Loop
        while(true) {
            CURRENT_STATE = getState();
            switch(CURRENT_STATE) {
            case DROPPING:
                drop();
            case RELEASING:
                release();
                break;
            case RETRIEVING:
                retrieve();
                break;
            }
            sleep(40,80); 
        } 
    }

    private void drop() {
        Inventory.dropAllExcept(ITEMS);
    }

    private void release() {
        RSNPC[] kebbits = NPCs.findNearest("Spotted kebbit");
        if(kebbits.length > 0) {
            println("found kebbit");
            if(kebbits[0].isOnScreen() && 
               Player.getPosition().distanceTo(kebbits[0].getPosition()) < 8) {
                DynamicClicking.clickRSNPC(kebbits[0], "Catch");
                sleep(1600, 2500);
            } else {
                Camera.turnToTile(kebbits[0].getPosition());
            }

        }
    }

    private void retrieve() {
        RSNPC[] falcons = NPCs.findNearest("Gyr Falcon");
        if(falcons.length > 0) {
            if(falcons[0].isOnScreen()) {
                println("catching");
                falcons[0].click("Retrieve");
                playerMoving();
            } else {
                Camera.turnToTile(falcons[0].getPosition());
            }
        }
    }

    private void playerMoving() {
        sleep(100, 200);
        while (Player.isMoving()) {
            sleep(100, 200);
        }
    }

    private boolean hasCaught() {
        RSNPC[] falcons = NPCs.findNearest("Gyr Falcon");
        return falcons.length > 0;
    }

    @Override
    // PAINT
    public void onPaint(Graphics g) {
        int xpGained = Skills.getXP(skill) - startXP;
        double xpPerMs = (double)xpGained / (double)getRunningTime();
        long timeToLevel = (long)(Skills.getXPToNextLevel(skill) / xpPerMs);

        g.setColor(Color.WHITE);
        g.drawString("yFalconry v1.0 by kyang1993", 10, 70); 
        g.drawString("State: " + CURRENT_STATE, 10, 90);
        g.drawString("Runtime: " + Timing.msToString(getRunningTime()), 10, 110);
        g.drawString("XP Gained: " + xpGained, 10, 130);
        g.drawString("XP/Hour: " + String.valueOf((int)(xpPerMs * 3600000)), 10, 150);
        g.drawString("Next level: " + Timing.msToString(timeToLevel), 10, 170);
        sleep(20);
    }
}
