package util;

public class IdGenerator {
    private static long nextId = 1;

    public static long generateId() {
        return nextId++;
    }
//    Создаем переменную nextId и метод, который при каждом вызове возвращает +1 от старой переменной
//    Т.е. 1 вызов = 1, 2 вызов = 2, 3 вызов = 3 и тд
}
