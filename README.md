# CANdle LED Bench Test (WPILib Java + Phoenix 6)

This is a small WPILib Java project that drives a CTRE CANdle + 1 external LED strip with a **one-shot “acquire flash”**
(green strobe for ~0.25s when `hasPiece` becomes true, then solid green).

## What you need installed
- WPILib VS Code **2026.2.1** or newer (recommended) citeturn1view0
- CTRE Phoenix 6 vendor library (Phoenix 6 for FRC 2026 vendordep) citeturn0search4turn0search1
- JDK 17 (this matches WPILib for 2026+) citeturn4search21

## First-time setup steps (inside WPILib VS Code)
1. Open this folder in **WPILib VS Code**.
2. Add CTRE Phoenix 6:
   - `WPILib: Manage Vendor Libraries` → `Install new libraries (online)`
   - Paste this URL:
     - `https://maven.ctr-electronics.com/release/com/ctre/phoenix6/latest/Phoenix6-frc2026-latest.json` citeturn0search4turn0search1
3. Set team number:
   - `WPILib: Set Team Number` (or edit `/.wpilib/wpilib_preferences.json`)

## Where to edit LED count
Open `src/main/java/frc/robot/Leds.java` and set:
- `STRIP_LED_COUNT`

## Quick test toggles
In `Robot.robotPeriodic()`, flip booleans `intaking`, `hasPiece`, `scoring`, `error` to see patterns.

## Bench wiring note
External strips should be powered from a proper 5V supply/buck converter (not the roboRIO 5V), and grounds must be common.
