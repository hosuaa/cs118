/* In this exercise I altered my explorer class to be able to solve mazes with multiple loops. My previous maze would work well with non loopy mazes, but when a loop was introduced
the controller would start to act weird and would take the wrong turns and sometimes collide with a wall. This was because the algorithm was a depth first algorithm and often when calling
the 'junction' method in Ex1/2 it would simply pick a random direction as there would be no available passage exits as the maze is loopy and so junctions are visited more than once.

I adjusted my explorer class to incorporate Tremaux's algorithm. This included making changes to how the program handles junctions. When exploring a junction, the program would either pick
a random available passage exit or, if the junction has been visited, simply reversing in the opposite direction. This change causes the program to only meet a junction a maximum of 2 times
and so on the second time (in backtrack) the program goes in the opposite direction to when it first entered it (still stored in robotData). This leads to the robot only going over a path between
2 junctions at most 2 times which means the program will reach the end as it passes through the maze.

*/

import uk.ac.warwick.dcs.maze.logic.IRobot;

public class Ex3 {

  private int pollRun = 0;
  private RobotData3 robotData;

  private int explorerMode = 1;
  //1 if exploring, 0 if backtracking

  public void controlRobot(IRobot robot) {
    //check if this is the first run of maze
    if ((robot.getRuns() == 0) && (pollRun == 0)){
      robotData = new RobotData3();
      explorerMode = 1;
    }
    pollRun++;
    if (explorerMode == 1) {
      exploreControl(robot);
    } else {
      backtrackControl(robot);
    }
  }
  public void exploreControl(IRobot robot) {
    byte exits = nonwallExits(robot);
    int direction;
    if (exits < 2){
      explorerMode = 0;
      robot.setHeading(deadend(robot, robot.getHeading()));
      direction = IRobot.AHEAD;
      //backtrack if at deadend

    } else if (exits == 2){
      direction = corridor(robot);
    } else {
        if (beenbeforeExits(robot) <= 1) {
          //if at junction store the location of junction and heading of robot.
          robotData.recordJunction(robot.getLocation().x, robot.getLocation().y, robot.getHeading());
          //print junction to error check
          robotData.printJunction();
          //if at a new junction, pick a random passage.
          do {
            direction = randomDirection(robot);
          } while (robot.look(direction) != IRobot.PASSAGE);
        } else {
          //if at a junction been to before, reverse direction (and go in backtrack).
          explorerMode = 0;
          robot.setHeading(deadend(robot, robot.getHeading()));
          direction = IRobot.AHEAD;
        }
    }
    robot.face(direction);
  }
  public void backtrackControl(IRobot robot) {
    byte exits = nonwallExits(robot);
    int direction = IRobot.CENTRE;
    if (exits > 2){
      if (passageExits(robot) == 0) {
        //if at a fully explored junction (been to twice), exit it in reverse to when first entered.
        robot.setHeading(deadend(robot, robotData.searchJunction(robot.getLocation().x, robot.getLocation().y)));
        direction = IRobot.AHEAD;
      } else {
        //if at junction with passage(s) change to exploring and pick any available passage
        explorerMode = 1;
        do {
          direction = randomDirection(robot);
        } while (robot.look(direction) != IRobot.PASSAGE);
      }
    } else if (exits == 2){
        direction = corridor(robot);
    } else {
      robot.setHeading(deadend(robot, robot.getHeading()));
      direction = IRobot.AHEAD;
    }
    System.out.println(explorerMode);
    robot.face(direction);
  }
  public void reset() {
    //when reset is pressed reset stored junctions and make sure in explorer
    robotData.resetJunctionCounter();
    explorerMode = 1;
  }
  private byte nonwallExits(IRobot robot) {
    byte nonwalls = 0;
    //return every square around the robot that isn't a wall
		for (int i = 0; i<4; i++) {
			if (robot.look(IRobot.AHEAD+i) != IRobot.WALL)
			nonwalls++;
		}
    return nonwalls;
  }
  private int deadend(IRobot robot, int heading){
    //if the robot is at the first square set explorermode to 1 (at the first square sometimes exploererMode is set to 0 which is problematic for the program)
    if (beenbeforeExits(robot) == 0){
      explorerMode = 1;
      for (int i = 0; i<4; i++) {
        robot.setHeading(IRobot.NORTH+i)
  			if (robot.look(IRobot.AHEAD) != IRobot.WALL){
          return IRobot.NORTH+i;
        }
      }
    }
    //deadend adjusted to reverse direction of robot always (instead of checking for walls)
    switch(heading){
      case IRobot.NORTH:
        return IRobot.SOUTH;
      case IRobot.EAST:
        return IRobot.WEST;
      case IRobot.SOUTH:
        return IRobot.NORTH;
      case IRobot.WEST:
       return IRobot.EAST;
      default:
        return 0;
    }
  }
  private int corridor(IRobot robot){
    //checks if robot is at a corner and what direction it must face and if not just go down corridor
    if (robot.look(IRobot.AHEAD) == IRobot.WALL){
      if (robot.look(IRobot.LEFT) == IRobot.WALL){
        return IRobot.RIGHT;
      } else {
        return IRobot.LEFT;
      }
    } else {
      return IRobot.AHEAD;
    }
  }
  private int junction(IRobot robot){
    int passExits = passageExits(robot);
    int direction;
    //if no passage exits pick a random direction
    if (passExits == 0){
      direction = randomDirection(robot);
    } else {
      //if there is at least one pick any of them
      do {
        direction = randomDirection(robot);
      } while (robot.look(direction) != IRobot.PASSAGE);
    }
    return direction;
  }
  private byte passageExits(IRobot robot){
    byte passExits = 0;
    //check every sqaure around robot and return number of passage squares
		for (int i = 0; i<4; i++) {
			if (robot.look(IRobot.AHEAD+i) == IRobot.PASSAGE)
			passExits++;
		}
    return passExits;
  }
  private int randomDirection(IRobot robot){
    int randno;
    int direction;
    //pick a random direction that is not a wall
    do {
				randno = (int) (Math.random()*4);
				if (randno == 0){
					direction = IRobot.LEFT;
				} else if (randno == 1){
					direction = IRobot.RIGHT;
				} else if (randno == 2){
					direction = IRobot.BEHIND;
				} else {
					direction = IRobot.AHEAD;
				}
	  } while (robot.look(direction) == IRobot.WALL);
    return direction;
  }
  private byte beenbeforeExits(IRobot robot){
    byte beenbeforeExits = 0;
    //check every sqaure around robot and return number of beenbefore squares
		for (int i = 0; i<4; i++) {
			if (robot.look(IRobot.AHEAD+i) == IRobot.BEENBEFORE)
			beenbeforeExits++;
		}
    return beenbeforeExits;
  }
}
class RobotData3 {
  private static int maxJunctions = 10000;
  private static int junctionCounter;
  //create an array of junctionRecorder objects with size 10000
  private junctionRecorder3[] junctions = new junctionRecorder3 [maxJunctions];

  public RobotData3() {
    //constructor sets number of junctions to 0
    junctionCounter = 0;
  }

  public void resetJunctionCounter() {
    //needed when reset is pressed
    junctionCounter = 0;
  }
  public void recordJunction(int XCoord, int YCoord, int heading) {
    //instantiate a new junctionRecorder object for every junction encountered
    junctions[junctionCounter] = new junctionRecorder3(XCoord, YCoord, heading);
    //next increment for next junction
    junctionCounter++;
  }
  public void printJunction() {
    System.out.println("Junction " + junctionCounter + "X=" + junctions[junctionCounter-1].getjunctionX() + "Y=" + junctions[junctionCounter-1].getjunctionY() + " heading " + junctions[junctionCounter-1].getheading());
  }
  public int searchJunction(int XCoord, int YCoord) {
    for (int i = 0; i < junctions.length; i++) {
      if (junctions[i].getjunctionX() == XCoord) {
        if (junctions[i].getjunctionY() == YCoord) {
          //return heading of robot for junction called for
          return junctions[i].getheading();
        }
      }
    }
    //never returned needed to compile.
    return 0;
  }
}
class junctionRecorder3 {
  private int junctionX, junctionY, arrived;
  //only attributes needed to record for junctions
  public junctionRecorder3(int XCoord, int YCoord, int heading) {
    junctionX = XCoord;
    junctionY = YCoord;
    arrived = heading;
  }
  //getters needed as attributes are private
  public int getjunctionX() {
    return junctionX;
  }
  public int getjunctionY() {
    return junctionY;
  }
  public int getheading() {
    return arrived;
  }
}
