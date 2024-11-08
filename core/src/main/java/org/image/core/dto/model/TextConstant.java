package org.image.core.dto.model;

public class TextConstant {
    public static String SUBJECT_DOWNLOAD_IMAGE = "Скачивание изображения";
    public static String SUBJECT_REGISTER_USER = "Регистрация в сервисе";
    public static String SUBJECT_UPLOAD_IMAGE = "Загружены изображения";
    public static String TEXT_MESSAGE_DOWNLOAD = "Было скачано изображение %s и размер %s";
    public static String TEXT_MESSAGE_REGISTER = "Спасибо за регистрацию в сервисе";
    public static String TEXT_MESSAGE_UPLOAD = """
            В хранилище были загружены изображения: %s.
            В хранилище не были загружены изображения: %s.
            Общий объём загруженных изображений: %s MB.""";
    public static String TEXT_NOT_ENOUGH_RIGHT = "Нет прав для доступа";
    public static String TEXT_USER_NOT_FOUND = "Пользователь c ID %s не найден";
    public static String TEXT_IMAGE_NOT_FOUND = "Изображение с id %s не найдено";
}
