/* In this exercise I changed the program to not store the location of each junction. This saves alot of space as only the heading needs to be stored which means rather than store an array of
objects for each junction it is simply an array of attributes. This also meant I could remove alot of code such as the junctionRecorder class as well as code in other places which also saves space
like when searching for junctions it is now unecassary so the program became less complex as a for loop turned into a simple return statement.
I accomplished this by having the program use the junctionCounter as a tracker for which junction the robot is at as the code forces robot has to fully explore a path so when a junction is fully
explored  it 'removes' the junction by stepping junctionCounter backwards 1 and the program only has to store the most recent junctions for each path so in turn it has the same effect as Ex1 just
with less code and data storage.
*/
import uk.ac.warwick.dcs.maze.logic.IRobot;

public class Ex2 {

  private int pollRun = 0;
  private RobotData2 robotData;

  private int explorerMode = 1;
  //1 if exploring, 0 if backtracking

  public void controlRobot(IRobot robot) {
    //check if this is the first run of maze
    if ((robot.getRuns() == 0) && (pollRun == 0)){
      robotData = new RobotData2();
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
      direction = deadend(robot);
      //backtrack if at deadend

    } else if (exits == 2){
      direction = corridor(robot);
    } else {
        if (beenbeforeExits(robot) <= 1) {
          //if at junction store the location of junction and heading of robot
          robotData.recordJunction(robot.getHeading());
          //print junction to error check
          robotData.printJunction();
        }
      direction = junction(robot);
    }
    robot.face(direction);
  }
  public void backtrackControl(IRobot robot) {
    byte exits = nonwallExits(robot);
    int direction = IRobot.CENTRE;
    if (exits > 2){
      if (passageExits(robot) == 0) {

        //if at a junction with no passages (all paths fully explored) get the heading the robot entered and leave in the opposite way
        int initialHeading = robotData.searchJunction();
        switch(initialHeading){
          case IRobot.NORTH:
          //robot.setHeading() as can't use absolute directions in robot.look()
            robot.setHeading(IRobot.SOUTH);
            break;
          case IRobot.EAST:
            robot.setHeading(IRobot.WEST);
            break;
          case IRobot.SOUTH:
            robot.setHeading(IRobot.NORTH);
            break;
          case IRobot.WEST:
            robot.setHeading(IRobot.EAST);
        }
        //remove the junction as this junction will never be visited again
        robotData.removeJunction();
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
      direction = deadend(robot);
    }
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
  private int deadend(IRobot robot){
    //if the robot is at the first square set explorermode to 1 (at the first square sometimes exploererMode is set to 0 which is problematic for the program)
    if (beenbeforeExits(robot) == 0){
      explorerMode = 1;
    }
    //check every square around the robot and return the direction with no wall
    for (int i = 0; i<4; i++) {
			if (robot.look(IRobot.AHEAD+i) != IRobot.WALL){
        return IRobot.AHEAD+i;
      }
    }
    //it will never return 0, needed to compile
    return 0;
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
class RobotData2 {
  private static int maxJunctions = 10000;
  private static int junctionCounter;
  //create an array to store the heading the robot went in
  private int[] arrived = new int [maxJunctions];

  public RobotData2() {
    //constructor sets number of junctions to 0
    junctionCounter = 0;
  }

  public void resetJunctionCounter() {
    //needed when reset is pressed
    junctionCounter = 0;
  }
  public void recordJunction(int heading) {
    //store heading in array with index of junction number
    arrived[junctionCounter] = heading;
    //next increment for next junction
    junctionCounter++;
  }
  public void printJunction() {
    System.out.println("Junction " + junctionCounter + " heading " + arrived[junctionCounter-1]);
  }
  public int searchJunction() {
    //no search needed just return the heading for current junction
    return arrived[junctionCounter-1];
  }
  public void removeJunction() {
    //remove junction by stepping junctionCounter back (when it goes back up it will just overwrite the value stored)
    junctionCounter--;
  }
}
