package frc.robot;

import static edu.wpi.first.units.Units.Degrees;

import com.ctre.phoenix6.CANBus;
import com.ctre.phoenix6.configs.CANdleConfiguration;
import com.ctre.phoenix6.controls.ColorFlowAnimation;
import com.ctre.phoenix6.controls.EmptyAnimation;
import com.ctre.phoenix6.controls.RainbowAnimation;
import com.ctre.phoenix6.controls.SolidColor;
import com.ctre.phoenix6.controls.StrobeAnimation;
import com.ctre.phoenix6.controls.TwinkleAnimation;
import com.ctre.phoenix6.hardware.CANdle;
import com.ctre.phoenix6.signals.AnimationDirectionValue;
import com.ctre.phoenix6.signals.RGBWColor;
import com.ctre.phoenix6.signals.StatusLedWhenActiveValue;
import com.ctre.phoenix6.signals.StripTypeValue;

import edu.wpi.first.wpilibj.DriverStation;
import edu.wpi.first.wpilibj.Timer;

/**
 * CANdle LED manager (Phoenix 6).
 *
 * External strip is assumed to start at index 8 (0-7 are onboard CANdle LEDs).
 * Update STRIP_LED_COUNT when you know your strip length.
 */
public class Leds {
  // Update later when you know it
  private static final int STRIP_LED_COUNT = 60;

  // CANdle
  private static final int CANDLE_ID = 1;
  private final CANdle candle = new CANdle(CANDLE_ID, CANBus.roboRIO());

  // Indices: 0-7 onboard, 8+ external
  private static final int EXT_START = 8;
  private static final int EXT_END = EXT_START + STRIP_LED_COUNT - 1;

  // Colors (RGBWColor = r,g,b,w)
  private static final RGBWColor GREEN  = new RGBWColor(0, 217, 0, 0);
  private static final RGBWColor RED    = new RGBWColor(217, 0, 0, 0);
  private static final RGBWColor BLUE   = new RGBWColor(0, 0, 255, 0);
  private static final RGBWColor VIOLET = RGBWColor.fromHSV(Degrees.of(270), 0.9, 0.8);
  private static final RGBWColor WHITE  = new RGBWColor(255, 255, 255, 0).scaleBrightness(0.3);

  // One-shot flash settings (when hasPiece rises false->true)
  private static final double ACQUIRE_FLASH_SECONDS = 0.25;

  // ===== SHIFT WARNING SETTINGS =====
  private static final double SHIFT_WARNING_SECONDS = 5.0;

  // Shift start times on match clock (seconds remaining in TELEOP)
  private static final double SHIFT1_START = 130.0; // 2:10
  private static final double SHIFT2_START = 105.0; // 1:45
  private static final double SHIFT3_START = 80.0;  // 1:20
  private static final double SHIFT4_START = 55.0;  // 0:55
  private static final double ENDGAME_START = 30.0; // 0:30
  // ==================================

  // State tracking
  private boolean lastHasPiece = false;
  private double flashUntilTimestamp = 0.0;

  public Leds() {
    var cfg = new CANdleConfiguration();
    cfg.LED.StripType = StripTypeValue.GRB;
    cfg.LED.BrightnessScalar = 0.6;
    cfg.CANdleFeatures.StatusLedWhenActive = StatusLedWhenActiveValue.Disabled;
    candle.getConfigurator().apply(cfg);

    // Clear all animation slots
    for (int i = 0; i < 8; i++) {
      candle.setControl(new EmptyAnimation(i));
    }

    // Optional: onboard LEDs
    candle.setControl(new SolidColor(0, 7).withColor(WHITE));
  }

  // ---------- Shift warning helpers ----------
  private boolean inWarningWindow(double matchTime, double boundaryStart) {
    // clock counts DOWN; warn when (boundaryStart + 5) -> boundaryStart
    return matchTime <= (boundaryStart + SHIFT_WARNING_SECONDS) && matchTime > boundaryStart;
  }

  private boolean shouldShiftWarn() {
    if (!DriverStation.isTeleopEnabled()) return false;

    double t = DriverStation.getMatchTime(); // seconds remaining
    if (t < 0) return false; // often -1 when DS isn't providing match time

    return inWarningWindow(t, SHIFT1_START)
        || inWarningWindow(t, SHIFT2_START)
        || inWarningWindow(t, SHIFT3_START)
        || inWarningWindow(t, SHIFT4_START)
        || inWarningWindow(t, ENDGAME_START);
  }
  // ------------------------------------------

  private void clearExternalSlot0() {
    candle.setControl(new EmptyAnimation(0));
  }

  private void setExternalSolid(RGBWColor color) {
    clearExternalSlot0();
    candle.setControl(new SolidColor(EXT_START, EXT_END).withColor(color));
  }

  private void setExternalTwinkle(RGBWColor color) {
    candle.setControl(new TwinkleAnimation(EXT_START, EXT_END).withSlot(0).withColor(color));
  }

  private void setExternalFlow(RGBWColor color) {
    candle.setControl(
        new ColorFlowAnimation(EXT_START, EXT_END).withSlot(0)
            .withColor(color)
            .withDirection(AnimationDirectionValue.Forward)
    );
  }

  private void setExternalRainbow() {
    candle.setControl(new RainbowAnimation(EXT_START, EXT_END).withSlot(0).withSpeed(0.6));
  }

  private void setExternalStrobe(RGBWColor color, double speedSeconds) {
    candle.setControl(
        new StrobeAnimation(EXT_START, EXT_END).withSlot(0)
            .withColor(color)
            .withSpeed(speedSeconds)
    );
  }

  /**
   * Call every robotPeriodic. Feed your real booleans in.
   *
   * Priority:
   * Error > Disabled > Shift Warning > Scoring > Acquire Flash > HasPiece Solid > Intaking > Idle
   */
  public void periodic(boolean intaking, boolean hasPiece, boolean scoring, boolean error) {
    // Rising edge: just acquired a piece
    if (hasPiece && !lastHasPiece) {
      flashUntilTimestamp = Timer.getFPGATimestamp() + ACQUIRE_FLASH_SECONDS;
    }
    lastHasPiece = hasPiece;

    // 1) Error
    if (error) {
      setExternalStrobe(RED, 0.05);
      return;
    }

    // 2) Disabled
    if (DriverStation.isDisabled()) {
      setExternalTwinkle(VIOLET);
      return;
    }

    // 3) Shift warning (BLUE blink)
    if (shouldShiftWarn()) {
      setExternalStrobe(BLUE, 0.10);
      return;
    }

    // 4) Scoring
    if (scoring) {
      setExternalRainbow();
      return;
    }

    // 5) One-shot acquire flash
    if (Timer.getFPGATimestamp() < flashUntilTimestamp) {
      setExternalStrobe(GREEN, 0.06);
      return;
    }

    // 6) Has piece (solid)
    if (hasPiece) {
      setExternalSolid(GREEN);
      return;
    }

    // 7) Intaking
    if (intaking) {
      setExternalFlow(BLUE);
      return;
    }

    // 8) Idle
    setExternalSolid(VIOLET);
  }
}