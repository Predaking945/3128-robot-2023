package frc.team3128.commands;

import edu.wpi.first.math.geometry.Translation2d;
import edu.wpi.first.wpilibj.DriverStation;
import edu.wpi.first.wpilibj.DriverStation.Alliance;
import edu.wpi.first.wpilibj2.command.Commands;
import edu.wpi.first.wpilibj2.command.InstantCommand;
import edu.wpi.first.wpilibj2.command.ParallelDeadlineGroup;
import edu.wpi.first.wpilibj2.command.ProxyCommand;
import edu.wpi.first.wpilibj2.command.ProxyScheduleCommand;
import edu.wpi.first.wpilibj2.command.RunCommand;
import edu.wpi.first.wpilibj2.command.ScheduleCommand;
import edu.wpi.first.wpilibj2.command.SequentialCommandGroup;
import edu.wpi.first.wpilibj2.command.StartEndCommand;
import edu.wpi.first.wpilibj2.command.WaitCommand;
import edu.wpi.first.wpilibj2.command.WaitUntilCommand;
import frc.team3128.RobotContainer;
import frc.team3128.Constants.VisionConstants;
import frc.team3128.Constants.ArmConstants.ArmPosition;
import frc.team3128.subsystems.Led;
import frc.team3128.subsystems.Pivot;
import frc.team3128.subsystems.Swerve;
import frc.team3128.subsystems.Telescope;
import frc.team3128.subsystems.Vision;

public class CmdShelfPickup extends SequentialCommandGroup{
    
    private Telescope telescope;
    private Pivot pivot;
    private Led led;

    public CmdShelfPickup (boolean cone, boolean isReversed) {
        telescope = Telescope.getInstance();
        pivot = Pivot.getInstance();
        led = Led.getInstance();

        addCommands(
            new InstantCommand(()-> led.setPickupColor(cone), led),
            // new InstantCommand(()-> Vision.AUTO_ENABLED = false),
            Commands.parallel(
                new CmdMoveLoading(isReversed, VisionConstants.LOADING_ZONE).asProxy(),
                // Commands.sequence(
                //     new WaitUntilCommand(()-> Vision.AUTO_ENABLED),
                new InstantCommand(() -> pivot.startPID(isReversed ? -ArmPosition.HP_SHELF.pivotAngle : ArmPosition.HP_SHELF.pivotAngle), pivot)
                // )
            ),
            new WaitUntilCommand(()-> pivot.atSetpoint()),
            new InstantCommand(() -> telescope.startPID(ArmPosition.HP_SHELF.teleDist), telescope),
            new WaitUntilCommand(()-> telescope.atSetpoint()),
            new CmdManipGrab(cone),
            new ScheduleCommand(new CmdMoveArm(ArmPosition.NEUTRAL, isReversed)),
            new WaitUntilCommand(()-> telescope.atSetpoint()),
            new ScheduleCommand(new WaitCommand(0.5).deadlineWith(new StartEndCommand(() -> RobotContainer.controller.startVibrate(), () -> RobotContainer.controller.stopVibrate()))),
            new InstantCommand(() -> new InstantCommand(()-> Vision.AUTO_ENABLED = false))
        );
    }
}
