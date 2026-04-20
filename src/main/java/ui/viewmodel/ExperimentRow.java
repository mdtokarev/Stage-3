package ui.viewmodel;

import domain.Experiment;

public record ExperimentRow(Experiment source,
                            long id,
                            String name,
                            String ownerUsername,
                            String createdAt) {
}
