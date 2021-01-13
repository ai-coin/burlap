package burlap.domain.singleagent.gridworld;

import burlap.behavior.singleagent.learning.tdmethods.QLearning;
import burlap.behavior.valuefunction.QValue;
import static burlap.domain.singleagent.gridworld.GridWorldDomain.*;
import burlap.domain.singleagent.gridworld.state.GridAgent;
import burlap.domain.singleagent.gridworld.state.GridLocation;
import burlap.domain.singleagent.gridworld.state.GridWorldState;
import burlap.mdp.core.Domain;
import burlap.mdp.core.oo.state.OOState;
import burlap.mdp.core.oo.state.ObjectInstance;
import burlap.mdp.core.state.State;
import burlap.statehashing.simple.SimpleHashableStateFactory;
import burlap.visualizer.*;
import java.awt.*;
import java.awt.geom.Ellipse2D;
import java.awt.geom.Rectangle2D;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import javax.imageio.ImageIO;

/**
 * Returns a visualizer for grid worlds in which walls are rendered as black squares or black lines, the agent is a gray circle and the
 * location objects are colored squares. The size of the cells scales to the size of the domain and the size of the canvas.
 *
 * @author James MacGlashan
 *
 */
public class GridWorldVisualizer {

  private static BufferedImage upArrowImage = null;
  private static BufferedImage downArrowImage = null;
  private static BufferedImage rightArrowImage = null;
  private static BufferedImage leftArrowImage = null;
  private static BufferedImage blackUpArrowImage = null;
  private static BufferedImage blackDownArrowImage = null;
  private static BufferedImage blackRightArrowImage = null;
  private static BufferedImage blackLeftArrowImage = null;

  static {
    try {
      upArrowImage = ImageIO.read(new File("data/up-arrow.png"));
      downArrowImage = ImageIO.read(new File("data/down-arrow.png"));
      rightArrowImage = ImageIO.read(new File("data/right-arrow.png"));
      leftArrowImage = ImageIO.read(new File("data/left-arrow.png"));
      blackUpArrowImage = ImageIO.read(new File("data/black-up-arrow.png"));
      blackDownArrowImage = ImageIO.read(new File("data/black-down-arrow.png"));
      blackRightArrowImage = ImageIO.read(new File("data/black-right-arrow.png"));
      blackLeftArrowImage = ImageIO.read(new File("data/black-left-arrow.png"));
    } catch (IOException e) {
      System.out.println(e.getMessage());
    }
  }

  /**
   * Sets the Q learning agent to enable visualization of Q-Values.
   *
   * @param aqLearning the Q learning agent
   */
  public static void setQLearning(final QLearning aqLearning) {
    qLearning = aqLearning;
  }

  // dependency inserted by PlotTest in order to visualize the Q-values
  private static QLearning qLearning;

  private GridWorldVisualizer() {
    // do nothing
  }

  /**
   * Returns visualizer for a grid world domain with the provided wall map. This method has been deprecated because the domain is no longer
   * necessary. Use the {@link #getVisualizer(int[][])} method instead.
   *
   * @param d the domain of the grid world
   * @param map the wall map matrix where 0s indicate it is clear of walls, 1s indicate a full cell wall in that cell, 2s indicate a 1D
   * north wall, 3s indicate a 1D east wall, and 4s indicate a 1D north and east wall.
   * @return a grid world domain visualizer
   */
  @Deprecated
  public static Visualizer getVisualizer(Domain d, int[][] map) {

    StateRenderLayer r = getRenderLayer(d, map);
    Visualizer v = new Visualizer(r);

    return v;
  }

  /**
   * Returns visualizer for a grid world domain with the provided wall map.
   *
   * @param map the wall map matrix where 0s indicate it is clear of walls, 1s indicate a full cell wall in that cell, 2s indicate a 1D
   * north wall, 3s indicate a 1D east wall, and 4s indicate a 1D north and east wall.
   * @return a grid world domain visualizer
   */
  public static Visualizer getVisualizer(int[][] map) {

    StateRenderLayer r = getRenderLayer(map);
    Visualizer v = new Visualizer(r);

    return v;
  }

  /**
   * Returns state render layer for a gird world domain with the provided wall map. This method has been deprecated because the domain
   * object is no longer necessary. Use the {@link #getRenderLayer(int[][])} method instead.
   *
   * @param d the domain of the grid world
   * @param map the wall map matrix where 0s indicate it is clear of walls, 1s indicate a full cell wall in that cell, 2s indicate a 1D
   * north wall, 3s indicate a 1D east wall, and 4s indicate a 1D north and east wall.
   * @return a grid world domain state render layer
   */
  @Deprecated
  public static StateRenderLayer getRenderLayer(Domain d, int[][] map) {

    StateRenderLayer r = new StateRenderLayer();

    r.addStatePainter(new MapPainter(map));
    OOStatePainter oopainter = new OOStatePainter();
    oopainter.addObjectClassPainter(GridWorldDomain.CLASS_LOCATION, new LocationPainter(map));
    oopainter.addObjectClassPainter(GridWorldDomain.CLASS_AGENT, new CellPainter(1, Color.cyan, map));
    r.addStatePainter(oopainter);

    return r;

  }

  /**
   * Returns state render layer for a gird world domain with the provided wall map.
   *
   * @param map the wall map matrix where 0s indicate it is clear of walls, 1s indicate a full cell wall in that cell, 2s indicate a 1D
   * north wall, 3s indicate a 1D east wall, and 4s indicate a 1D north and east wall.
   * @return a grid world domain state render layer
   */
  public static StateRenderLayer getRenderLayer(int[][] map) {

    StateRenderLayer r = new StateRenderLayer();

    r.addStatePainter(new MapPainter(map));
    OOStatePainter oopainter = new OOStatePainter();
    oopainter.addObjectClassPainter(GridWorldDomain.CLASS_LOCATION, new LocationPainter(map));
    oopainter.addObjectClassPainter(GridWorldDomain.CLASS_AGENT, new CellPainter(1, Color.cyan, map));
    r.addStatePainter(oopainter);

    return r;

  }

  /**
   * A static painter class for rendering the walls of the grid world as black squares or black lines for 1D walls.
   *
   * @author James MacGlashan
   *
   */
  public static class MapPainter implements StatePainter {

    protected int dwidth;
    protected int dheight;
    protected int[][] map;

    /**
     * Initializes for the domain and wall map
     *
     * @param map the wall map matrix where 1s indicate a wall in that cell and 0s indicate it is clear of walls
     */
    public MapPainter(int[][] map) {
      this.dwidth = map.length;
      this.dheight = map[0].length;
      this.map = map;
    }

    @Override
    public void paint(Graphics2D g2, State state, float cWidth, float cHeight) {

      //draw the walls; make them black
      g2.setColor(Color.black);

      //set stroke for 1d walls
      g2.setStroke(new BasicStroke(4));

      float domainXScale = this.dwidth;
      float domainYScale = this.dheight;

      //determine the normalized width
      float width = (1.0f / domainXScale) * cWidth;
      float height = (1.0f / domainYScale) * cHeight;

      //pass through each cell of the map and if it is a wall, draw it
      for (int i = 0; i < this.dwidth; i++) {
        for (int j = 0; j < this.dheight; j++) {

          boolean drawNorthWall = false;
          boolean drawEastWall = false;

          switch (this.map[i][j]) {
            case 1:
              float rx = i * width;
              float ry = cHeight - height - j * height;
              g2.fill(new Rectangle2D.Float(rx, ry, width, height));
              break;

            case 2:
              drawNorthWall = true;
              break;

            case 3:
              drawEastWall = true;
              break;

            case 4:
              drawNorthWall = true;
              drawEastWall = true;
              break;

            default:
              break;
          }

          int left = (int) (i * width);
          int top = (int) (cHeight - height - j * height);

          if (drawNorthWall) {
            g2.drawLine(left, top, (int) (left + width), top);
          }
          if (drawEastWall) {
            g2.drawLine((int) (left + width), top, (int) (left + width), (int) (top + height));
          }

          if (qLearning != null && this.map[i][j] == 0) {
            double northQValue = 0.0;
            double southQValue = 0.0;
            double eastQValue = 0.0;
            double westQValue = 0.0;

            final State gridState = new GridWorldState(new GridAgent(
                    i, // x
                    j), // y
                    new GridLocation(10, 10, "loc0")); // terminal location

            //  {
            //    agent (agent): {
            //      x: {1}
            //      y: {9}
            //    }
            //    loc0 (location): {
            //      x: {10}
            //      y: {10}
            //      type: {0}
            //    }
            //  }
            //System.out.println("gridState: " + gridState);
            final List<QValue> qValues = qLearning.qValues(gridState);
            for (final QValue qValue : qValues) {
//              if (i > 8 && j > 8) {
//                System.out.println("x: " + i + ", y: " + j + ", action: " + qValue.a + ", " + qValue.q);
//              }
              switch (qValue.a.actionName()) {
                case "north":
                  northQValue = qValue.q;
                  break;

                case "south":
                  southQValue = qValue.q;
                  break;

                case "east":
                  eastQValue = qValue.q;
                  break;

                case "west":
                  westQValue = qValue.q;
                  break;

                default:
                  assert false;
              }
            }
            boolean isNorthArrow = false;
            boolean isSouthArrow = false;
            boolean isEastArrow = false;
            boolean isWestArrow = false;
            if (northQValue > southQValue && northQValue > eastQValue && northQValue > westQValue) {
              isNorthArrow = true;
            } else if (southQValue > northQValue && southQValue > eastQValue && southQValue > westQValue) {
              isSouthArrow = true;
            } else if (eastQValue > northQValue && eastQValue > southQValue && eastQValue > westQValue) {
              isEastArrow = true;
            } else if (westQValue > northQValue && westQValue > southQValue && westQValue > eastQValue) {
              isWestArrow = true;
            }
            if (isNorthArrow) {
//              System.out.println("north arrow, width: " + width + ", height: " + height);
//              for (final QValue qValue : qValues) {
//                System.out.println("  x: " + i + ", y: " + j + ", action: " + qValue.a + ", " + qValue.q);
//              }
              g2.drawImage(
                      upArrowImage, // img
                      null, // op
                      left, // x
                      top); // y

            } else if (isSouthArrow) {
//              System.out.println("south arrow");
//              for (final QValue qValue : qValues) {
//                System.out.println("  x: " + i + ", y: " + j + ", action: " + qValue.a + ", " + qValue.q);
//              }
              g2.drawImage(
                      downArrowImage, // img
                      null, // op
                      left, // x
                      top); // y

            } else if (isEastArrow) {
//              System.out.println("east arrow");
//              for (final QValue qValue : qValues) {
//                System.out.println("  x: " + i + ", y: " + j + ", action: " + qValue.a + ", " + qValue.q);
//              }
              g2.drawImage(
                      rightArrowImage, // img
                      null, // op
                      left, // x
                      top); // y

            } else if (isWestArrow) {
//              System.out.println("west arrow");
//              for (final QValue qValue : qValues) {
//                System.out.println("  x: " + i + ", y: " + j + ", action: " + qValue.a + ", " + qValue.q);
//              }
              g2.drawImage(
                      leftArrowImage, // img
                      null, // op
                      left, // x
                      top); // y
            }
          }

        }
      }

    }

  }

  /**
   * A painter for a grid world cell which will fill the cell with a given color and where the cell position is indicated by the x and y
   * attribute for the mapped object instance
   *
   * @author James MacGlashan
   *
   */
  public static class CellPainter implements ObjectPainter {

    protected Color col;
    protected int dwidth;
    protected int dheight;
    protected int[][] map;
    protected int shape = 0; //0 for rectangle 1 for ellipse

    /**
     * Initializes painter for a rectangle shape cell
     *
     * @param col the color to paint the cell
     * @param map the wall map matrix where 1s indicate a wall in that cell and 0s indicate it is clear of walls
     */
    public CellPainter(Color col, int[][] map) {
      this.col = col;
      this.dwidth = map.length;
      this.dheight = map[0].length;
      this.map = map;
    }

    /**
     * Initializes painter with filling the cell with the given shape
     *
     * @param shape the shape with which to fill the cell. 0 for a rectangle, 1 for an ellipse.
     * @param col the color to paint the cell
     * @param map the wall map matrix where 1s indicate a wall in that cell and 0s indicate it is clear of walls
     */
    public CellPainter(int shape, Color col, int[][] map) {
      this.col = col;
      this.dwidth = map.length;
      this.dheight = map[0].length;
      this.map = map;
      this.shape = shape;
    }

    @Override
    public void paintObject(Graphics2D g2, OOState s, ObjectInstance ob, float cWidth, float cHeight) {

      //set the color of the object
      g2.setColor(this.col);

      float domainXScale = this.dwidth;
      float domainYScale = this.dheight;

      //determine then normalized width
      float width = (1.0f / domainXScale) * cWidth;
      float height = (1.0f / domainYScale) * cHeight;

      float rx = (Integer) ob.get(VAR_X) * width;
      float ry = cHeight - height - (Integer) ob.get(VAR_Y) * height;

      if (this.shape == 0) {
        g2.fill(new Rectangle2D.Float(rx, ry, width, height));
      } else {
        g2.fill(new Ellipse2D.Float(rx, ry, width, height));

      }

    }

  }

  /**
   * A painter for location objects which will fill the cell with a given color and where the cell position is indicated by the x and y
   * attribute for the mapped object instance
   *
   * @author James MacGlashan
   *
   */
  public static class LocationPainter implements ObjectPainter {

    protected List<Color> baseColors;
    protected int dwidth;
    protected int dheight;
    protected int[][] map;

    /**
     * Initializes painter
     *
     * @param map the wall map matrix where 1s indicate a wall in that cell and 0s indicate it is clear of walls
     */
    public LocationPainter(int[][] map) {
      this.dwidth = map.length;
      this.dheight = map[0].length;
      this.map = map;
      this.baseColors = new ArrayList<>(5);
      this.baseColors.add(Color.blue);
      this.baseColors.add(Color.red);
      this.baseColors.add(Color.green);
      this.baseColors.add(Color.yellow);
      this.baseColors.add(Color.magenta);
    }

    @Override
    public void paintObject(Graphics2D g2, OOState s, ObjectInstance ob, float cWidth, float cHeight) {

      int type = (Integer) ob.get(VAR_TYPE);
      int multiplier = type / this.baseColors.size();
      int colIndex = type % this.baseColors.size();

      Color col = this.baseColors.get(colIndex);
      for (int i = 0; i < multiplier; i++) {
        col = col.darker();
      }

      //set the color of the object
      g2.setColor(col);

      float domainXScale = this.dwidth;
      float domainYScale = this.dheight;

      //determine then normalized width
      float width = (1.0f / domainXScale) * cWidth;
      float height = (1.0f / domainYScale) * cHeight;

      float rx = (Integer) ob.get(VAR_X) * width;
      float ry = cHeight - height - (Integer) ob.get(VAR_Y) * height;

      g2.fill(new Rectangle2D.Float(rx, ry, width, height));
    }
  }
}
