package domain;

import util.IdGenerator;
import validation.ValidationException;

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

    private final IdGenerator idGenerator = new IdGenerator();

    public Experiment(String name, String description, String ownerUsername) {
        this.id = idGenerator.generateId();
        this.createdAt = Instant.now();
        this.updatedAt = Instant.now();

        validateName(name);
        validateDescription(description);
        validateOwnerUsername(ownerUsername);

        this.name = name;
        this.description = description;
        this.ownerUsername = ownerUsername;
    }

    static void validateName(String name) {
        if (name == null || name.isBlank())
            throw new ValidationException("Experiment name can't be empty");
        if (name.length() > 128)
            throw new ValidationException("Experiment name too long");
    }

    static void validateDescription(String description) {
        if (description != null && description.length() > 512)
            throw new ValidationException("Description too long");
    }

    static void validateOwnerUsername(String ownerUsername) {
        if (ownerUsername == null || ownerUsername.isBlank())
            throw new ValidationException("OwnerUsername can't be empty");
        if (ownerUsername.length() > 128)
            throw new ValidationException("OwnerUsername too long");
    }

    public void setName(String name) {
        validateName(name);
        this.name = name;
        this.updatedAt = Instant.now();
    }

    public void setDescription(String description) {
        validateDescription(description);
        this.description = description;
        this.updatedAt = Instant.now();
    }

    public void setOwnerUsername(String ownerUsername) {
        validateOwnerUsername(ownerUsername);
        this.ownerUsername = ownerUsername;
        this.updatedAt = Instant.now();
    }

    public long getId() {
        return id;
    }
    public String getName() {
        return name;
    }
    public String getDescription() {
        return description;
    }
    public String getOwnerUsername() {
        return ownerUsername;
    }
    public Instant getCreatedAt() {
        return createdAt;
    }
    public Instant getUpdatedAt() {
        return updatedAt;
    }


}


