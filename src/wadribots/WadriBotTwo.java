package wadribots;

import java.awt.Color;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import robocode.AdvancedRobot;
import robocode.ScannedRobotEvent;
import robocode.util.Utils;

public class WadriBotTwo extends AdvancedRobot {

    private static final int MAX_HISTORY = 100;
    private static final int POPULATION_SIZE = 50;
    private static final int MAX_GENERATIONS = 100;
    private static final double MUTATION_RATE = 0.1;

    private double battleFieldHeight;
    private double battleFieldWidth;
    private Rectangle2D fieldRect;
    private List<EnemyState> enemyHistory;
    private EnemyState currentEnemyState;

    public void run() {
        setAdjustRadarForRobotTurn(true);
        setAdjustGunForRobotTurn(true);
        setColors(Color.RED, Color.BLACK, Color.blue);
        battleFieldHeight = getBattleFieldHeight();
        battleFieldWidth = getBattleFieldWidth();
        fieldRect = new Rectangle2D.Double(18, 18, battleFieldWidth - 36, battleFieldHeight - 36);
        enemyHistory = new ArrayList<>();
        currentEnemyState = null;

        while (true) {
            setTurnRadarRight(Double.POSITIVE_INFINITY);
            if (currentEnemyState != null) {
                Point2D.Double predictedPosition = predictNextPosition();
                double theta = Utils.normalAbsoluteAngle(Math.atan2(
                        predictedPosition.getX() - getX(), predictedPosition.getY() - getY()));

                setTurnGunRightRadians(Utils.normalRelativeAngle(theta - getGunHeadingRadians()));
                fire(Math.min(4, 1000 / currentEnemyState.getDistance()));
            }
            execute();
        }
    }

    public void onScannedRobot(ScannedRobotEvent e) {
        updateEnemyState(e);
        if (enemyHistory.size() >= MAX_HISTORY) {
            enemyHistory.remove(0);
        }
        enemyHistory.add(new EnemyState(e));
        currentEnemyState = new EnemyState(e);

        setTurnRadarLeft(getRadarTurnRemaining());
    }

    private void updateEnemyState(ScannedRobotEvent e) {
        if (currentEnemyState != null) {
            currentEnemyState.update(e);
        }
    }

    private Point2D.Double predictNextPosition() {
        List<Point2D.Double> population = generatePopulation();
        for (int generation = 0; generation < MAX_GENERATIONS; generation++) {
            List<Point2D.Double> offspring = generateOffspring(population);
            mutateOffspring(offspring);
            population.addAll(offspring);
            Collections.sort(population, (p1, p2) -> Double.compare(
                    fitness(p2), fitness(p1))); // Sort in descending order
            population = population.subList(0, POPULATION_SIZE); // Keep the top individuals

            if (fitness(population.get(0)) < 1) {
                break; // Stop if a good solution is found
            }
        }
        return population.get(0);
    }

    private List<Point2D.Double> generatePopulation() {
        List<Point2D.Double> population = new ArrayList<>();
        for (int i = 0; i < POPULATION_SIZE; i++) {
            population.add(new Point2D.Double(
                    fieldRect.getMinX() + Math.random() * fieldRect.getWidth(),
                    fieldRect.getMinY() + Math.random() * fieldRect.getHeight()));
        }
        return population;
    }

        private List<Point2D.Double> generateOffspring(List<Point2D.Double> population) {
        List<Point2D.Double> offspring = new ArrayList<>();
        for (int i = 0; i < POPULATION_SIZE; i++) {
            int parentIndex1 = (int) (Math.random() * POPULATION_SIZE);
            int parentIndex2 = (int) (Math.random() * POPULATION_SIZE);
            Point2D.Double parent1 = population.get(parentIndex1);
            Point2D.Double parent2 = population.get(parentIndex2);
            Point2D.Double child = crossover(parent1, parent2);
            offspring.add(child);
        }
        return offspring;
    }

    private Point2D.Double crossover(Point2D.Double parent1, Point2D.Double parent2) {
        double x = (parent1.getX() + parent2.getX()) / 2;
        double y = (parent1.getY() + parent2.getY()) / 2;
        return new Point2D.Double(x, y);
    }

    private void mutateOffspring(List<Point2D.Double> offspring) {
        for (Point2D.Double child : offspring) {
            if (Math.random() < MUTATION_RATE) {
                double offsetX = (Math.random() - 0.5) * fieldRect.getWidth() / 10;
                double offsetY = (Math.random() - 0.5) * fieldRect.getHeight() / 10;
                child.setLocation(child.getX() + offsetX, child.getY() + offsetY);
            }
        }
    }

    private double fitness(Point2D.Double position) {
        double fitness = 0;
        for (EnemyState enemyState : enemyHistory) {
            double predictedDistance = enemyState.getDistanceTo(position);
            fitness += Math.abs(enemyState.getDistance() - predictedDistance);
        }
        return fitness;
    }

    private static class EnemyState {
        private double distance;
        private double headingRadians;
        private double velocity;

        public EnemyState(ScannedRobotEvent event) {
            update(event);
        }

        public void update(ScannedRobotEvent event) {
            distance = event.getDistance();
            headingRadians = event.getHeadingRadians();
            velocity = event.getVelocity();
        }

        public double getDistance() {
            return distance;
        }

        public double getHeadingRadians() {
            return headingRadians;
        }

        public double getVelocity() {
            return velocity;
        }

        public double getDistanceTo(Point2D.Double position) {
            return Point2D.distance(position.getX(), position.getY(), position.getX(), position.getY());
        }
    }
}

