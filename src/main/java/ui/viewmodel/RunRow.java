package ui.viewmodel;

import domain.Run;

public record RunRow(Run source,
                     long id,
                     String name,
                     String operatorName,
                     String createdAt) {
}
