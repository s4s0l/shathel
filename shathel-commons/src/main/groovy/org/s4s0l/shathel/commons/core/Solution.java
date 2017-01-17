package org.s4s0l.shathel.commons.core;

/**
 * @author Matcin Wielgus
 */
public class Solution {
    private final SolutionDescription solutionDescription;
    private final Environment environment;
    private final StackDescriptionTree descriptionTree;
    private final EnricherProvider enricherProvider;

    public Solution(SolutionDescription solutionDescription, Environment environment,
                    StackDescriptionTree descriptionTree, EnricherProvider enricherProvider) {
        this.solutionDescription = solutionDescription;
        this.environment = environment;
        this.descriptionTree = descriptionTree;
        this.enricherProvider = enricherProvider;
    }

    public StartCommandSchedule createStartCommand() {
        return new CommandScheduleFactory(descriptionTree,
                environment.getIntrospectionProvider(), enricherProvider)
                .createStartSchedule();
    }

    public void run(StartCommandSchedule schedule) {
        new CommandScheduleRunner(environment.getProvisionExecutor(),
                environment.getContainerRunner(),
                environment.getStorage()).run(schedule);
    }

}
