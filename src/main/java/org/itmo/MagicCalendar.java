package org.itmo;

import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class MagicCalendar {
    // Перечисление типов встреч
    public enum MeetingType {
        WORK, PERSONAL
    }

    private final ConcurrentHashMap<String, List<Map.Entry<String, MeetingType>>> storage = new ConcurrentHashMap<>();

    /**
     * Запланировать встречу для пользователя.
     *
     * @param user имя пользователя
     * @param time временной слот (например, "10:00")
     * @param type тип встречи (WORK или PERSONAL)
     * @return true, если встреча успешно запланирована, false если:
     *         - в этот временной слот уже есть встреча, и правило замены не выполняется,
     *         - лимит в 5 встреч в день уже достигнут.
     *
     *
     *
*            Встречи планируются на временные слоты, представленные строками (например, "10:00").
     * Если в указанный временной слот уже запланирована встреча, новая встреча не добавляется, за исключением случая:
     * Если уже запланирована встреча типа WORK, а новая встреча имеет тип PERSONAL, то новая встреча заменяет существующую.
     * Если в указанное время уже находится встреча типа PERSONAL, то попытка добавить встречу любого типа должна быть отклонена.
     *
     */
    public boolean scheduleMeeting(String user, String time, MeetingType type) {
        List<Map.Entry<String, MeetingType>> userTimes = storage.get(user);
        if (userTimes == null) {
            storage.put(user, List.of(Map.entry(time, MeetingType.PERSONAL)));
            return true;
        }
        if (userTimes.size() >= 5) {
            return false;
        }
        Map.Entry<String, MeetingType> userMeetingTime = userTimes.stream()
                .filter(e -> e.getKey().equals(time)).findFirst().orElse(null);
        if (userMeetingTime == null) {
            return false;
        }
        if (userMeetingTime.getValue() == MeetingType.PERSONAL) {
            return false;
        }
        if (type == MeetingType.PERSONAL && userMeetingTime.getValue() == MeetingType.WORK) {
            userMeetingTime.setValue(MeetingType.PERSONAL);
            userTimes.remove(userMeetingTime);
            userTimes.add(Map.entry(time, MeetingType.PERSONAL));
        }

        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("HH:mm");

        LocalTime currentTime = LocalTime.parse(time, formatter);
        LocalTime newTime = currentTime.plusHours(1);

        String resultTime = newTime.format(formatter);

        Map.Entry<String, MeetingType> userNextTime = userTimes.stream().filter(e -> e.getKey().equals(resultTime)).findFirst().orElse(null);
        if (userNextTime != null) {
            return false;
        } else {
            userTimes.add(Map.entry(time, type));
            return true;
        }
    }

    /**
     * Получить список всех встреч пользователя.
     *
     * @param user имя пользователя
     * @return список временных слотов, на которые запланированы встречи.
     */
    public List<String> getMeetings(String user) {
        return storage.get(user).stream().map(Map.Entry::getKey).toList();
    }

    /**
     * Отменить встречу для пользователя по заданному времени.
     *
     * @param user имя пользователя
     * @param time временной слот, который нужно отменить.
     * @return true, если встреча была успешно отменена; false, если:
     *         - встреча в указанное время отсутствует,
     *         - встреча имеет тип PERSONAL (отменять можно только WORK встречу).
     *
     *
     * Отмена встреч
     * Календарь должен позволять отменять встречи с помощью метода cancelMeeting(String user, String time).
     * Отменять можно только встречи типа WORK. Попытка отменить встречу типа PERSONAL должна вернуть false и не изменять расписание.
     * При успешной отмене временной слот освобождается и может быть использован для планирования новой встречи.
     *
     *
     */
    public boolean cancelMeeting(String user, String time) {
        List<Map.Entry<String, MeetingType>> userTimes = storage.get(user);
        Map.Entry<String, MeetingType> userMeetingTime = userTimes.stream()
                .filter(e -> e.getKey().equals(time)).findFirst().orElse(null);

        if (userMeetingTime == null) {
            return false;
        }
        if (userMeetingTime.getValue() == MeetingType.PERSONAL) {
            return false;
        }
        userTimes.remove(userMeetingTime);
        return true;
    }
}
