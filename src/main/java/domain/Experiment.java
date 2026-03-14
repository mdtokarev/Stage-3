package domain;

import java.time.Instant;

public final class Experiment {
    // Уникальный номер эксперимента. Программа назначает сама.
    private final long id;
    // Название эксперимента. Нельзя пустое. До 128 символов.
    private String name;
    // Описание (кратко “что делаем”). Можно пусто. До 512 символов.
    private String description;
    // Кто создал (логин). На ранних этапах можно "SYSTEM".
    private String ownerUsername;
    // Когда создан. Программа ставит автоматически.
    private final Instant createdAt;
    // Когда изменяли. Программа обновляет автоматически.
    private Instant updatedAt;

    public Experiment(long id, String name, String description, String ownerUsername, Instant createdAt, Instant updatedAt) {
        this.id = id;
        this.name = name;
        this.description = description;
        this.ownerUsername = ownerUsername;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }

    public long getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getOwnerUsername() {
        return ownerUsername;
    }

    public void setOwnerUsername(String ownerUsername) {
        this.ownerUsername = ownerUsername;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public Instant getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(Instant updatedAt) {
        this.updatedAt = updatedAt;
    }
}


