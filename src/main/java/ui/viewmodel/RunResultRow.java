package ui.viewmodel;

import domain.RunResult;

public record RunResultRow(RunResult source,
                           long id,
                           String param,
                           String value,
                           String unit,
                           String comment) {
}
