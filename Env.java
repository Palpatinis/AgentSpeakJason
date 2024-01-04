package example;
import jason.asSyntax.*;
import jason.environment.Environment;
import jason.environment.grid.GridWorldModel;
import jason.environment.grid.GridWorldView;
import jason.environment.grid.Location;
import java.util.ArrayList;
import java.util.List;
import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics;
import java.util.Random;
import java.util.logging.Logger;

public class Env extends Environment {
    Random rand = new Random();
    public static final int GSize = 5; // grid size
    public static final int CUST  = 16; // 
    public static final Term    ns = Literal.parseLiteral("move(custs)");
    public static final Term    pg = Literal.parseLiteral("pick(cust)");
    public static final Term    dg = Literal.parseLiteral("drop(cust)");
    public static final Term    bg = Literal.parseLiteral("burn(cust)");
    public static final Literal g1 = Literal.parseLiteral("customer(r1)");
    public static final Literal g2 = Literal.parseLiteral("customer(r2)");
    public static final Literal g3 = Literal.parseLiteral("customer(r3)");
    public static final Literal g4 = Literal.parseLiteral("customer(r4)");
    public static final Literal g5 = Literal.parseLiteral("customer(r5)");
    static Logger logger = Logger.getLogger(Env.class.getName());
    private Model model;
    private View  view;

    

    @Override
    public void init(String[] args) {
        model = new Model();
        view  = new View(model);
        model.setView(view);
        updatePercepts();
    }

    @Override
    public boolean executeAction(String ag, Structure action) {
        logger.info(ag+" doing: "+ action);
        int i=0;
        try {
            if (action.equals(ns)) {
                model.move(); 
            }else if (action.getFunctor().equals("move_towards")) {
                int x = (int)((NumberTerm)action.getTerm(0)).solve();
                int y = (int)((NumberTerm)action.getTerm(1)).solve();
                model.moveTowards(x,y);
            } else if (action.equals(pg)) {
                model.pickCust();
            } else if (action.equals(dg)) {
                model.dropCust();
            } else if (action.equals(bg)) {
                model.burnCust();
                i++;
            } else {
                return false;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        updatePercepts();

        try {
            Thread.sleep(200);
        } catch (Exception e) {}
        informAgsEnvironmentChanged();
        return true;
    }

    /** creates the agents perception based on the Model */
    void updatePercepts() {
        clearPercepts();

        Location r1Loc = model.getAgPos(0);
        Location r2Loc = model.getAgPos(1);
        Location r3Loc = model.getAgPos(2);
        Location r4Loc = model.getAgPos(3);
        Location r5Loc = model.getAgPos(4);


        Literal pos1 = Literal.parseLiteral("pos(r1," + r1Loc.x + "," + r1Loc.y + ")");
        Literal pos2 = Literal.parseLiteral("pos(r2," + r2Loc.x + "," + r2Loc.y + ")");
        Literal pos3 = Literal.parseLiteral("pos(r3," + r3Loc.x + "," + r3Loc.y + ")");
        Literal pos4 = Literal.parseLiteral("pos(r4," + r4Loc.x + "," + r4Loc.y + ")");
        Literal pos5 = Literal.parseLiteral("pos(r5," + r5Loc.x + "," + r5Loc.y + ")");
        addPercept(pos1);
        addPercept(pos2);
        addPercept(pos3);
        addPercept(pos4);
        addPercept(pos5);

        if (model.hasObject(CUST, r1Loc)) {
            addPercept(g1);
        }
        if (model.hasObject(CUST, r2Loc)) {
            addPercept(g2);
        }
        if (model.hasObject(CUST, r3Loc)) {
            addPercept(g3);
        }
        if (model.hasObject(CUST, r4Loc)) {
            addPercept(g4);
        }
        if (model.hasObject(CUST, r5Loc)) {
            addPercept(g5);
        }
    }

    class Model extends GridWorldModel {

        public static final int MErr = 2; 
        int nerr; 
        boolean r1HasCust = false; 

        Random random = new Random(System.currentTimeMillis());

        private Model() {
            super(GSize, GSize, 5);

            // initial location of agents
            try {

                int a=rand.nextInt(4);
                int b=rand.nextInt(4);
                setAgPos(0, a, b);

                Location r2Loc = new Location(0,0);
                Location r3Loc = new Location(3,0);
                Location r4Loc = new Location(0,4);
                Location r5Loc = new Location(4,3);
                setAgPos(1, r2Loc);
                setAgPos(2, r3Loc);
                setAgPos(3, r4Loc);
                setAgPos(4, r5Loc);
            } catch (Exception e) {
                e.printStackTrace();
            }
            add(CUST, 3, 0);
            add(CUST, 0,3);
            add(CUST, 3,3);
            add(CUST, 2,2);
        }
        void move() throws Exception {
            Location r1 = getAgPos(0);
        
            // Get the distances to all customer objects using Manhattan distance
            int[] distances = new int[]{
                    calculateManhattanDistance(r1, getCustomerLocation(1)),
                    calculateManhattanDistance(r1, getCustomerLocation(2)),
                    calculateManhattanDistance(r1, getCustomerLocation(3)),
                    calculateManhattanDistance(r1, getCustomerLocation(4)),

            };
        
            // Find the index of the customer with the minimum distance
            int minDistanceIndex = findMinDistanceIndex(distances);
        
            // Move towards the customer with the minimum distance
            Location target = getCustomerLocation(minDistanceIndex + 1);
            moveToTarget(r1, target);
            distances[minDistanceIndex]=100;
            // Update agent positions

            setAgPos(0, r1);
            setAgPos(1, getAgPos(1));
            setAgPos(2, getAgPos(2));
            setAgPos(3, getAgPos(3));
            setAgPos(4, getAgPos(4));
        }
        
        // Helper method to calculate Manhattan distance between two locations
        int calculateManhattanDistance(Location loc1, Location loc2) {
            return Math.abs(loc1.x - loc2.x) + Math.abs(loc1.y - loc2.y);
        }
        
        // Helper method to find the index of the minimum value in an array
        int findMinDistanceIndex(int[] distances) {
            int minIndex = 0;
            for (int i = 1; i < distances.length; i++) {
                if (distances[i] < distances[minIndex]) {
                    minIndex = i;
                }
            }
            return minIndex;
        }
        
        // Helper method to move towards a target location
        void moveToTarget(Location current, Location target) {
            if (current.x < target.x) {
                current.x++;
            } else if (current.x > target.x) {
                current.x--;
            }
            if (current.y < target.y) {
                current.y++;
            } else if (current.y > target.y) {
                current.y--;
            }
        }
        Location getCustomerLocation(int customerIndex) {
            // Choose the location of the customer based on the index
            switch (customerIndex) {
                case 1:
                    return new Location(3, 0);
                case 2:
                    return new Location(0, 3); // Example location for customer 2
                case 3:
                    return new Location(3, 3); // Example location for customer 3
                case 4:
                    return new Location(2, 2); // Example location for customer 4
                default:
                    return new Location(0,0); // Default location (you can modify it accordingly)
            }
        }
        
        
        
        void moveTowards(int x, int y) throws Exception {
            Location r1 = getAgPos(0);
            if (r1.x < x && !((r1.x == 0 && r1.y == 1) || (r1.x == 1 && r1.y == 1) || (r1.x == 3 && r1.y == 0) ||
                 (r1.x == 4 && r1.y == 0) || (r1.x == 3 && r1.y == 2) || (r1.x == 4 && r1.y == 2))) {
            r1.x++;
            } else if (r1.x > x && !((r1.x == 0 && r1.y == 2) || (r1.x == 1 && r1.y == 2) || (r1.x == 3 && r1.y == 1) ||
                          (r1.x == 4 && r1.y == 1) || (r1.x == 3 && r1.y == 3) || (r1.x == 4 && r1.y == 3))) {
            r1.x--;
            }
            if (r1.y < y)
                r1.y++;
            else if (r1.y > y)
                r1.y--;
            setAgPos(0, r1);
            setAgPos(1, getAgPos(1));
            setAgPos(2, getAgPos(2));
            setAgPos(3, getAgPos(3));
            setAgPos(4, getAgPos(4)); // just to draw it in the view
        }

        void pickCust() {
            if (model.hasObject(CUST, getAgPos(0))) {
                // sometimes the "picking" action doesn't work
                // but never more than MErr times
                if (random.nextBoolean() || nerr == MErr) {
                    remove(CUST, getAgPos(0));
                    nerr = 0;
                    r1HasCust = true;
                } else {
                    nerr++;
                }
            }
        }
        void dropCust() {
            if (r1HasCust) {
                r1HasCust = false;
                add(CUST, getAgPos(0));
            }
        }
        void burnCust() {
            if (model.hasObject(CUST, getAgPos(1))) {
                remove(CUST, getAgPos(1));
            }
            else if (model.hasObject(CUST, getAgPos(2))) {
                remove(CUST, getAgPos(2));
            }
            else if (model.hasObject(CUST, getAgPos(3))) {
                remove(CUST, getAgPos(3));
            }
            else if (model.hasObject(CUST, getAgPos(4))) {
                remove(CUST, getAgPos(4));
            }
        }
    }

    class View extends GridWorldView {

        public View(Model model) {
            super(model, " World", 600);
            defaultFont = new Font("Arial", Font.BOLD, 18); // change default font
            setVisible(true);
            repaint();
        }

        /** draw application objects */
        @Override
        public void draw(Graphics g, int x, int y, int object) {
            switch (object) {
            case Env.CUST:
                drawCust(g, x, y);
                break;
            }
        }

        @Override
        public void drawAgent(Graphics g, int x, int y, Color c, int id) {
            String label = "R" + (id + 1);
            Color agentColor;
            switch (id) {
                case 0:
                    agentColor = Color.YELLOW;
                    if (((Model) model).r1HasCust) {
                        label += " - G";
                        agentColor = Color.orange;
                    }
                    break;
                case 1:
                    label ="R";
                    agentColor = Color.red;
                    // Add any additional conditions for agent 1
                    break;
                case 2:
                    label ="Y";
                    agentColor = Color.yellow;
                    // Add any additional conditions for agent 2
                    break;
                case 3:
                    label ="G";
                    agentColor = Color.green;
                    // Add any additional conditions for agent 3
                    break;
                case 4:
                    label ="B";
                    agentColor = Color.blue;
                    // Add any additional conditions for agent 4
                    break;
                default:
                    agentColor = Color.BLACK; // Default color for unknown agents
                    break;
            }
            super.drawAgent(g, x, y, agentColor, -1);
            if (id == 0) {
                g.setColor(Color.black);
            } else {
                g.setColor(Color.white);
            }
            super.drawString(g, x, y, defaultFont, label);
            repaint();
        }

        public void drawCust(Graphics g, int x, int y) {
            super.drawObstacle(g, x, y);
            g.setColor(Color.white);
            drawString(g, x, y, defaultFont, "G");
        }

    }
}