package domain;

import java.time.Instant;

public final class Experiment {
    // Уникальный номер эксперимента. Программа назначает сама.
    private long id;
    // Название эксперимента. Нельзя пустое. До 128 символов.
    public String name;
    // Описание (кратко “что делаем”). Можно пусто. До 512 символов.
    public String description;
    // Кто создал (логин). На ранних этапах можно "SYSTEM".
    public String ownerUsername;
    // Когда создан. Программа ставит автоматически.
    private Instant createdAt;
    // Когда изменяли. Программа обновляет автоматически.
    private Instant updatedAt;
    }


