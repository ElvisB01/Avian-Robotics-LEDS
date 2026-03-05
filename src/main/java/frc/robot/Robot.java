package frc.robot;

import edu.wpi.first.wpilibj.TimedRobot;
import frc.robot.subsystems.IntakeSubsytem;

public class Robot extends TimedRobot {
  private final Leds leds = new Leds();
  private final IntakeSubsytem intake = new IntakeSubsytem();
  private final ShooterSubsystem shooter = new ShooterSubsystem();


  @Override
  public void robotPeriodic() {
    // TODO: Replace these with your real robot logic
    boolean intaking = intake.isIntaking();
    boolean hasPiece = false;
    boolean scoring  = shooter.isShooting();
    boolean error    = false;

    leds.periodic(intaking, hasPiece, scoring, error);
  }
}
