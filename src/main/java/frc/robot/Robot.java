package frc.robot;

import edu.wpi.first.wpilibj.TimedRobot;

public class Robot extends TimedRobot {
  private final Leds leds = new Leds();

  @Override
  public void robotPeriodic() {
    // TODO: Replace these with your real robot logic
    boolean intaking = false;
    boolean hasPiece = false;
    boolean scoring  = false;
    boolean error    = false;

    leds.periodic(intaking, hasPiece, scoring, error);
  }
}
